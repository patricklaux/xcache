package com.igeeksky.xcache.config;

import com.igeeksky.xcache.beans.BeanContext;
import com.igeeksky.xcache.beans.BeanDesc;
import com.igeeksky.xcache.beans.BeanParser;
import com.igeeksky.xcache.common.CacheLevel;
import com.igeeksky.xcache.util.CollectionUtils;
import com.igeeksky.xcache.util.StringUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局缓存配置
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-25
 */
public class XcacheProperties {

    private MultiManagerProperties multiCacheManager;

    private final List<BeanDesc> beans = new ArrayList<>();

    private final Map<String, BeanParser> beanParsers = new ConcurrentHashMap<>();

    private Map<String, MultiCacheProperties> multiCaches = new LinkedHashMap<>(16);

    public void prepare(BeanContext beanContext) {
        multiCacheManager.setBeanContext(beanContext);
        multiCaches.forEach((name, multiCacheProperties) -> merge(multiCacheManager, multiCacheProperties));
    }

    public boolean isEnableCacheProxy() {
        return multiCacheManager.isEnableCacheProxy();
    }

    public List<BeanDesc> getBeans() {
        return beans;
    }

    public MultiManagerProperties getMultiCacheManager() {
        return multiCacheManager;
    }

    public void setMultiCacheManager(MultiManagerProperties multiCacheManager) {
        this.multiCacheManager = multiCacheManager;
    }

    public Map<String, MultiCacheProperties> getMultiCaches() {
        return multiCaches;
    }

    public void setMultiCaches(Map<String, MultiCacheProperties> multiCaches) {
        this.multiCaches = multiCaches;
    }

    public void setMultiCaches(List<MultiCacheProperties> multiCaches) {
        if (null != multiCaches) {
            multiCaches.forEach((properties) -> this.multiCaches.put(properties.getName(), properties));
        }
    }

    public MultiCacheProperties getFromMultiCaches(String name) {
        return multiCaches.computeIfAbsent(name, nameKey -> {
            MultiCacheProperties multiCacheProperties = new MultiCacheProperties();
            multiCacheProperties.setName(nameKey);
            merge(multiCacheManager, multiCacheProperties);
            return multiCacheProperties;
        });
    }

    private void merge(MultiManagerProperties multiManagerProperties, MultiCacheProperties multiCacheProperties) {
        String application = multiManagerProperties.getApplication();
        multiCacheProperties.setApplication(application);

        String cacheId = multiManagerProperties.getCacheId();
        multiCacheProperties.setCacheId(cacheId);

        Charset charset = multiCacheProperties.getCharset();
        if (null == charset) {
            charset = multiManagerProperties.getCharset();
        }
        multiCacheProperties.setCharset(charset);

        boolean enableCache = multiManagerProperties.isEnableCache();
        if (null == multiCacheProperties.getEnableCache()) {
            multiCacheProperties.setEnableCache(enableCache);
        }

        // extension----start
        if (null == multiCacheProperties.getEnableUpdateListener()) {
            multiCacheProperties.setEnableUpdateListener(multiManagerProperties.isEnableUpdateListener());
        }

        if (null == multiCacheProperties.getEnableUpdateBroadcast()) {
            multiCacheProperties.setEnableUpdateBroadcast(multiManagerProperties.isEnableUpdateBroadcast());
        }

        if (null == multiCacheProperties.getEnableStatistics()) {
            multiCacheProperties.setEnableStatistics(multiManagerProperties.isEnableStatistics());
        }

        if (null == multiCacheProperties.getCacheLoader()) {
            multiCacheProperties.setCacheLoader(multiManagerProperties.getCacheLoader());
        }

        if (null == multiCacheProperties.getCacheWriter()) {
            multiCacheProperties.setCacheWriter(multiManagerProperties.getCacheWriter());
        }

        if (null == multiCacheProperties.getCacheLock()) {
            multiCacheProperties.setCacheLock(multiManagerProperties.getCacheLock());
        }

        if (null == multiCacheProperties.getCacheUpdate()) {
            multiCacheProperties.setCacheUpdate(multiManagerProperties.getCacheUpdate());
        }

        if (null == multiCacheProperties.getContainsPredicate()) {
            multiCacheProperties.setContainsPredicate(multiManagerProperties.getContainsPredicate());
        }

        if (null == multiCacheProperties.getEventSerializer()) {
            multiCacheProperties.setEventSerializer(multiManagerProperties.getEventSerializer());
        }

        if (null == multiCacheProperties.getStatisticsPublisher()) {
            multiCacheProperties.setStatisticsPublisher(multiManagerProperties.getStatisticsPublisher());
        }

        if (null == multiCacheProperties.getStatisticsSerializer()) {
            multiCacheProperties.setStatisticsSerializer(multiManagerProperties.getStatisticsSerializer());
        }
        // extension----end


        Map<CacheLevel, CacheProperties> cachesMap = multiManagerProperties.getCachesMap();
        List<CacheProperties> caches = multiCacheProperties.getCaches();
        for (CacheProperties cacheProperties : caches) {
            cacheProperties.setApplication(application);
            cacheProperties.setCacheId(cacheId);

            CacheLevel cacheLevel = cacheProperties.getCacheLevel();
            String name = multiCacheProperties.getName();
            CacheProperties cacheManagerProperties = cachesMap.get(cacheLevel);
            if (null == cacheManagerProperties) {
                throw new CacheConfigException("name=" + name + ", CacheLevel=" + cacheLevel + " is undefined");
            }
            merge(cacheManagerProperties, cacheProperties);
        }

        multiCacheProperties.setBeanContext(multiManagerProperties.getBeanContext());

        CollectionUtils.merge(multiManagerProperties.getMetadata(), multiCacheProperties.getMetadata());
    }


    private void merge(CacheProperties managerProperties, CacheProperties cacheProperties) {
        String defaultStore = managerProperties.getDefaultStore();
        if (StringUtils.isEmpty(defaultStore)) {
            List<String> stores = managerProperties.getStores();
            if (stores.size() > 1) {
                throw new CacheConfigException("storeUse must not be null");
            }
            defaultStore = stores.get(0);
            managerProperties.setDefaultStore(defaultStore);
        }

        if (null == cacheProperties.getDefaultStore()) {
            cacheProperties.setDefaultStore(defaultStore);
        }


        CacheProperties.Generic generic = managerProperties.getGeneric();
        if (null != generic) {
            CacheProperties.Generic cacheGeneric = cacheProperties.getGeneric();
            if (null == cacheGeneric) {
                cacheGeneric = new CacheProperties.Generic();
                cacheProperties.setGeneric(cacheGeneric);
            }
            mergeGeneric(generic, cacheGeneric);
        }

        CacheProperties.Local local = managerProperties.getLocal();
        if (null != local) {
            CacheProperties.Local cacheLocal = cacheProperties.getLocal();
            if (null == cacheLocal) {
                cacheLocal = new CacheProperties.Local();
                cacheProperties.setLocal(cacheLocal);
            }
            mergeLocal(local, cacheLocal);
        }

        CacheProperties.Remote remote = managerProperties.getRemote();
        if (null != remote) {
            CacheProperties.Remote cacheRemote = cacheProperties.getRemote();
            if (null == cacheRemote) {
                cacheRemote = new CacheProperties.Remote();
                cacheProperties.setRemote(cacheRemote);
            }
            mergeRemote(remote, cacheRemote);
        }

        CacheProperties.Caffeine caffeine = managerProperties.getCaffeine();
        if (null != caffeine) {
            CacheProperties.Caffeine cacheCaffeine = cacheProperties.getCaffeine();
            if (null == cacheCaffeine) {
                cacheCaffeine = new CacheProperties.Caffeine();
                cacheProperties.setCaffeine(cacheCaffeine);
            }
            mergeCaffeine(caffeine, cacheCaffeine);
        }

        CacheProperties.Redis redis = managerProperties.getRedis();
        if (null != redis) {
            CacheProperties.Redis cacheRedis = cacheProperties.getRedis();
            if (null == cacheRedis) {
                cacheRedis = new CacheProperties.Redis();
                cacheProperties.setRedis(cacheRedis);
            }
            mergeRedis(redis, cacheRedis);
        }
    }

    private void mergeGeneric(CacheProperties.Generic generic, CacheProperties.Generic cacheGeneric) {
        if (null == cacheGeneric.getCacheLevel()) {
            cacheGeneric.setCacheLevel(generic.getCacheLevel());
        }

        if (null == cacheGeneric.getEnableNullValue()) {
            cacheGeneric.setEnableNullValue(generic.isEnableNullValue());
        }

        if (null == cacheGeneric.getEnableStatistics()) {
            cacheGeneric.setEnableStatistics(generic.getEnableStatistics());
        }

        if (null == cacheGeneric.getStatisticsPublisher()) {
            cacheGeneric.setStatisticsPublisher(generic.getStatisticsPublisher());
        }

        if (null == cacheGeneric.getStatisticsSerializer()) {
            cacheGeneric.setStatisticsSerializer(generic.getStatisticsSerializer());
        }

        CollectionUtils.merge(generic.getMetadata(), cacheGeneric.getMetadata());
    }


    private void mergeLocal(CacheProperties.Local local, CacheProperties.Local cacheLocal) {
        mergeGeneric(local, cacheLocal);
        if (null == cacheLocal.getEnableSerializeValue()) {
            cacheLocal.setEnableSerializeValue(local.isEnableSerializeValue());
        }

        if (null == cacheLocal.getEnableCompressValue()) {
            cacheLocal.setEnableCompressValue(local.isEnableCompressValue());
        }

        if (null == cacheLocal.getValueSerializer()) {
            cacheLocal.setValueSerializer(local.getValueSerializer());
        }

        if (null == cacheLocal.getValueCompressor()) {
            cacheLocal.setValueCompressor(local.getValueCompressor());
        }
    }


    private void mergeRemote(CacheProperties.Remote remote, CacheProperties.Remote cacheRemote) {
        mergeGeneric(remote, cacheRemote);
        if (null == cacheRemote.getEnableCompressValue()) {
            cacheRemote.setEnableCompressValue(remote.isEnableCompressValue());
        }

        if (null == cacheRemote.getKeySerializer()) {
            cacheRemote.setKeySerializer(remote.getKeySerializer());
        }

        if (null == cacheRemote.getValueSerializer()) {
            cacheRemote.setValueSerializer(remote.getValueSerializer());
        }

        if (null == cacheRemote.getValueCompressor()) {
            cacheRemote.setValueCompressor(remote.getValueCompressor());
        }
    }


    private void mergeCaffeine(CacheProperties.Caffeine caffeine, CacheProperties.Caffeine cacheCaffeine) {
        mergeLocal(caffeine, cacheCaffeine);

        if (null == cacheCaffeine.getInitialCapacity()) {
            cacheCaffeine.setInitialCapacity(caffeine.getInitialCapacity());
        }

        if (null == cacheCaffeine.getMaximumSize()) {
            cacheCaffeine.setMaximumSize(caffeine.getMaximumSize());
        }

        if (null == cacheCaffeine.getMaximumWeight()) {
            cacheCaffeine.setMaximumWeight(caffeine.getMaximumWeight());
        }

        if (null == cacheCaffeine.getWeigher()) {
            cacheCaffeine.setWeigher(caffeine.getWeigher());
        }

        if (null == cacheCaffeine.getKeyStrength()) {
            cacheCaffeine.setKeyStrength(caffeine.getKeyStrength());
        }

        if (null == cacheCaffeine.getValueStrength()) {
            cacheCaffeine.setValueStrength(caffeine.getValueStrength());
        }

        if (null == cacheCaffeine.getExpireAfterWrite()) {
            cacheCaffeine.setExpireAfterWrite(caffeine.getExpireAfterWrite());
        }

        if (null == cacheCaffeine.getExpireAfterAccess()) {
            cacheCaffeine.setExpireAfterAccess(caffeine.getExpireAfterAccess());
        }
    }


    private void mergeRedis(CacheProperties.Redis redis, CacheProperties.Redis cacheRedis) {
        mergeRemote(redis, cacheRedis);
        if (null == cacheRedis.getStoreType()) {
            cacheRedis.setStoreType(redis.getStoreType());
        }

        if (null == cacheRedis.getExpireAfterWrite()) {
            cacheRedis.setExpireAfterWrite(redis.getExpireAfterWrite());
        }

        if (null == cacheRedis.getNamespace()) {
            cacheRedis.setNamespace(redis.getNamespace());
        }

        if (null == cacheRedis.getEnableKeyPrefix()) {
            cacheRedis.setEnableKeyPrefix(redis.isEnableKeyPrefix());
        }
    }
}
