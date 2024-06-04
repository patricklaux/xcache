package com.igeeksky.xcache.core;

import com.igeeksky.xcache.Cache;
import com.igeeksky.xcache.CacheManager;
import com.igeeksky.xcache.beans.BeanContext;
import com.igeeksky.xcache.common.CacheInitializationException;
import com.igeeksky.xcache.common.CacheLevel;
import com.igeeksky.xcache.config.CacheProperties;
import com.igeeksky.xcache.config.MultiCacheProperties;
import com.igeeksky.xcache.config.MultiManagerProperties;
import com.igeeksky.xcache.config.XcacheProperties;
import com.igeeksky.xcache.extension.update.CacheUpdateType;
import com.igeeksky.xcache.store.no.NoOpCacheStoreWithoutMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 全局缓存管理
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-10
 */
public class XcacheManager {

    private static final Logger log = LoggerFactory.getLogger(XcacheManager.class);

    private static final int ONE_LEVEL = 1;
    private static final int TWO_LEVEL = 2;
    private static final int THREE_LEVEL = 3;

    private XcacheProperties xcacheProperties;
    private final ConcurrentMap<String, Cache<?, ?>> multiCacheMap = new ConcurrentHashMap<>(16);
    private final ConcurrentMap<CacheLevel, CacheManager> cacheManagerMap = new ConcurrentHashMap<>(16);

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public XcacheManager(XcacheProperties xcacheProperties) {
        this.initialize(xcacheProperties);
    }

    private void initialize(XcacheProperties xcacheProperties) {
        readWriteLock.writeLock().lock();
        try {
            this.xcacheProperties = xcacheProperties;
            BeanContext beanContext = new BeanContext(xcacheProperties.getBeans(), null);
            this.xcacheProperties.prepare(beanContext);
            this.initializeManagers();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new CacheInitializationException(e);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public void reinitialize(XcacheProperties xcacheProperties) {
        boolean useCacheProxyNew = xcacheProperties.isEnableCacheProxy();
        boolean useCacheProxyOld = this.xcacheProperties.isEnableCacheProxy();
        if (!(useCacheProxyOld && useCacheProxyNew)) {
            throw new CacheInitializationException("Reinitialize the cache, \"enableCacheProxy\" must be true");
        }

        readWriteLock.writeLock().lock();
        try {
            this.initialize(xcacheProperties);
            this.reinitializeCaches();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new CacheInitializationException(e);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * 初始化缓存管理器
     */
    private void initializeManagers() {
        cacheManagerMap.clear();
        MultiManagerProperties multiManagerProperties = xcacheProperties.getMultiManagerProperties();
        List<CacheProperties> caches = multiManagerProperties.getCaches();
        for (CacheProperties cacheProperties : caches) {
            if (null != cacheProperties) {
                cacheManagerMap.put(cacheProperties.getCacheLevel(), new CacheStoreManager(cacheProperties));
            }
        }
    }

    private void reinitializeCaches() {
        multiCacheMap.forEach((name, cache) -> {
            if (cache instanceof CacheProxy) {
                CacheProxy<?, ?> proxy = (CacheProxy<?, ?>) cache;
                Class<?> keyType = proxy.getKeyType();
                Class<?> valueType = proxy.getValueType();
                MultiCacheProperties multiCacheProperties = xcacheProperties.getFromMultiCaches(name);
                Cache<?, ?> newCache = createCache(name, multiCacheProperties, keyType, valueType);
                proxy.setCache(newCache);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> get(String name, Class<K> keyType, Class<V> valueType) {
        readWriteLock.readLock().lock();
        try {
            Cache<?, ?> cache = multiCacheMap.get(name);
            if (null != cache) {
                return (Cache<K, V>) cache;
            }
            return (Cache<K, V>) multiCacheMap.computeIfAbsent(name, key -> createCache(name, keyType, valueType));
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private <K, V> Cache<K, V> createCache(String name, Class<K> keyType, Class<V> valueType) {
        MultiCacheProperties multiCacheProperties = xcacheProperties.getFromMultiCaches(name);
        Cache<K, V> multiCache = createCache(name, multiCacheProperties, keyType, valueType);
        if (xcacheProperties.getMultiManagerProperties().isEnableCacheProxy()) {
            return new CacheProxy<>(multiCache);
        }
        return multiCache;
    }

    private <K, V> Cache<K, V> createCache(String name, MultiCacheProperties multiProperties,
                                           Class<K> keyType, Class<V> valueType) {
        Boolean enableCache = multiProperties.getEnableCache();
        if (enableCache) {
            List<Cache<K, V>> cacheList = createCaches(name, multiProperties.getCaches(), keyType, valueType);
            return compositeCache(multiProperties, keyType, valueType, cacheList);
        }

        return new NoOpCacheStoreWithoutMonitor<>(name, keyType, valueType);
    }

    private <K, V> List<Cache<K, V>> createCaches(String name, List<CacheProperties> caches,
                                                  Class<K> keyType, Class<V> valueType) {

        List<Cache<K, V>> cacheList = new ArrayList<>(caches.size());

        for (CacheProperties cacheProperties : caches) {
            CacheManager cacheManager = cacheManagerMap.get(cacheProperties.getCacheLevel());
            Cache<K, V> cache = cacheManager.get(name, cacheProperties, keyType, valueType);
            if (null == cache) {
                throw new CacheInitializationException("can't create cache, name: " + name);
            }
            cacheList.add(cache);
        }

        return cacheList;
    }

    private <K, V> Cache<K, V> compositeCache(MultiCacheProperties multiProperties, Class<K> keyType, Class<V> valueType,
                                              List<Cache<K, V>> cacheList) {

        int size = cacheList.size();
        if (size > THREE_LEVEL) {
            throw new CacheInitializationException("Cache size can't more than three");
        }

        if (size == THREE_LEVEL) {
            Cache<K, V> firstCache = cacheList.get(0);
            Cache<K, V> secondCache = cacheList.get(1);
            Cache<K, V> thirdCache = cacheList.get(2);
            MultiExtension<K, V> multiExtension = MultiExtension.builder(keyType, valueType)
                    .setMultiCacheProperties(multiProperties)
                    .setStoreType(ThreeLevelCache.STORE_TYPE)
                    .setFirstCache(firstCache)
                    .setUpdateType(CacheUpdateType.REMOVE)
                    .build();
            return new ThreeLevelCache<>(multiProperties, multiExtension, firstCache, secondCache, thirdCache);
        }

        if (size == TWO_LEVEL) {
            Cache<K, V> firstCache = cacheList.get(0);
            Cache<K, V> secondCache = cacheList.get(1);
            MultiExtension<K, V> multiExtension = MultiExtension.builder(keyType, valueType)
                    .setMultiCacheProperties(multiProperties)
                    .setStoreType(TwoLevelCache.STORE_TYPE)
                    .setFirstCache(firstCache)
                    .setUpdateType(CacheUpdateType.REMOVE)
                    .build();
            return new TwoLevelCache<>(multiProperties, multiExtension, firstCache, secondCache);
        }

        if (size == ONE_LEVEL) {
            Cache<K, V> firstCache = cacheList.get(0);
            MultiExtension<K, V> multiExtension = MultiExtension.builder(keyType, valueType)
                    .setMultiCacheProperties(multiProperties)
                    .setStoreType(OneLevelCache.STORE_TYPE)
                    .setFirstCache(firstCache)
                    .setUpdateType(CacheUpdateType.PUT)
                    .build();
            return new OneLevelCache<>(multiProperties, multiExtension, firstCache);
        }

        throw new CacheInitializationException();
    }
}
