package com.igeeksky.xcache.core;

import com.igeeksky.xcache.Cache;
import com.igeeksky.xcache.beans.BeanContext;
import com.igeeksky.xcache.config.MultiCacheProperties;
import com.igeeksky.xcache.extension.ExtensionHelper;
import com.igeeksky.xcache.extension.contain.ContainsPredicate;
import com.igeeksky.xcache.extension.contain.ContainsPredicateProvider;
import com.igeeksky.xcache.extension.lock.CacheLock;
import com.igeeksky.xcache.extension.monitor.CacheMonitor;
import com.igeeksky.xcache.extension.serialization.CacheEventSerializer;
import com.igeeksky.xcache.extension.update.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 多级缓存扩展
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-05
 */
public class MultiExtension<K, V> {

    private final Class<K> keyType;

    private final Class<V> valueType;

    private CacheLock<K> cacheLock;

    private ContainsPredicate<K> containsPredicate;

    private final List<CacheMonitor<K, V>> cacheMonitors = new LinkedList<>();

    private MultiExtension(Class<K> keyType, Class<V> valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    public Class<K> getKeyType() {
        return keyType;
    }

    public Class<V> getValueType() {
        return valueType;
    }

    public CacheLock<K> getCacheLock() {
        return cacheLock;
    }

    public ContainsPredicate<K> getContainsPredicate() {
        return containsPredicate;
    }

    public List<CacheMonitor<K, V>> getCacheMonitors() {
        return cacheMonitors;
    }

    private void setCacheLock(CacheLock<K> cacheLock) {
        this.cacheLock = cacheLock;
    }

    private void setContainsPredicate(ContainsPredicate<K> containsPredicate) {
        this.containsPredicate = containsPredicate;
    }

    private void setCacheMonitors(List<CacheMonitor<K, V>> cacheMonitors) {
        if (null != cacheMonitors) {
            this.cacheMonitors.addAll(cacheMonitors);
        }
    }

    private void addToCacheMonitors(CacheMonitor<K, V> cacheMonitor) {
        if (null != cacheMonitor) {
            this.cacheMonitors.add(cacheMonitor);
        }
    }

    public static <K, V> Builder<K, V> builder(Class<K> keyType, Class<V> valueType) {
        return new Builder<>(keyType, valueType);
    }

    public static class Builder<K, V> {

        private final MultiExtension<K, V> multiExtension;

        private final Class<K> keyType;
        private final Class<V> valueType;

        private String storeType;
        private Cache<K, V> firstCache;
        private CacheUpdateType updateType;
        private MultiCacheProperties multiCacheProperties;

        private Builder(Class<K> keyType, Class<V> valueType) {
            this.keyType = keyType;
            this.valueType = valueType;
            this.multiExtension = new MultiExtension<>(keyType, valueType);
        }

        public Builder<K, V> setMultiCacheProperties(MultiCacheProperties multiCacheProperties) {
            this.multiCacheProperties = multiCacheProperties;
            return this;
        }

        public Builder<K, V> setStoreType(String storeType) {
            this.storeType = storeType;
            return this;
        }

        public Builder<K, V> setFirstCache(Cache<K, V> firstCache) {
            this.firstCache = firstCache;
            return this;
        }

        public Builder<K, V> setUpdateType(CacheUpdateType updateType) {
            this.updateType = updateType;
            return this;
        }

        public MultiExtension<K, V> build() {
            cacheLock();
            cacheUpdate();
            cacheStatistics();
            containsPredicate();
            return multiExtension;
        }

        private void cacheLock() {
            String id = multiCacheProperties.getCacheLock();
            BeanContext beanContext = multiCacheProperties.getBeanContext();

            String name = multiCacheProperties.getName();
            Map<String, Object> metadata = multiCacheProperties.getMetadata();
            CacheLock<K> cacheLock = ExtensionHelper.cacheLock(id, beanContext, name, keyType, metadata);

            Objects.requireNonNull(cacheLock, "CacheLock: id=" + id + ", this bean is not predefined");
            multiExtension.setCacheLock(cacheLock);
        }

        private void cacheUpdate() {
            Boolean enableUpdateBroadcast = multiCacheProperties.getEnableUpdateBroadcast();
            Boolean enableUpdateListener = multiCacheProperties.getEnableUpdateListener();
            if (enableUpdateBroadcast || enableUpdateListener) {
                CacheEventSerializer<K, V> serializer = eventSerializer();

                String id = multiCacheProperties.getCacheUpdate();
                BeanContext beanContext = multiCacheProperties.getBeanContext();
                CacheUpdateProvider provider = ExtensionHelper.provider(id, beanContext, CacheUpdateProvider.class);

                if (enableUpdateBroadcast) {
                    addCacheUpdateMonitor(serializer, provider);
                }

                if (enableUpdateListener) {
                    registerCacheUpdatePolicy(serializer, provider);
                }
            }
        }

        private void containsPredicate() {
            String id = multiCacheProperties.getContainsPredicate();
            BeanContext beanContext = multiCacheProperties.getBeanContext();
            ContainsPredicateProvider provider = ExtensionHelper.provider(id, beanContext, ContainsPredicateProvider.class);
            Objects.requireNonNull(provider, "ContainsPredicate: id=" + id + ", this bean is not predefined");

            ContainsPredicate<K> containsPredicate = provider.get(keyType);
            multiExtension.setContainsPredicate(containsPredicate);
        }

        private void cacheStatistics() {
            String id = multiCacheProperties.getStatisticsPublisher();
            BeanContext beanContext = multiCacheProperties.getBeanContext();
            Boolean enableStatistics = multiCacheProperties.getEnableStatistics();

            String name = multiCacheProperties.getName();
            String application = multiCacheProperties.getApplication();
            List<CacheMonitor<K, V>> cacheMonitors = ExtensionHelper.statisticsMonitor(id, beanContext, enableStatistics, name, storeType, application);
            multiExtension.setCacheMonitors(cacheMonitors);
        }

        private CacheEventSerializer<K, V> eventSerializer() {
            String id = multiCacheProperties.getEventSerializer();
            BeanContext beanContext = multiCacheProperties.getBeanContext();
            return ExtensionHelper.eventSerializer(id, beanContext, keyType, valueType);
        }

        private void addCacheUpdateMonitor(CacheEventSerializer<K, V> serializer, CacheUpdateProvider provider) {
            String name = multiCacheProperties.getName();
            String cacheId = multiCacheProperties.getCacheId();

            CacheUpdatePublisher publisher = provider.getPublisher(name);
            CacheUpdateMonitor<K, V> cacheUpdateMonitor = new CacheUpdateMonitor<>(cacheId, serializer, publisher);
            multiExtension.addToCacheMonitors(cacheUpdateMonitor);
        }

        private void registerCacheUpdatePolicy(CacheEventSerializer<K, V> serializer, CacheUpdateProvider provider) {
            CacheUpdatePolicy<K, V> cacheUpdatePolicy = new CacheUpdatePolicy<>(firstCache, updateType, serializer);
            provider.register(multiCacheProperties.getName(), cacheUpdatePolicy);
        }
    }
}