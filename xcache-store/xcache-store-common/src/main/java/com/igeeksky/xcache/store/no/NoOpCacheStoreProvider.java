package com.igeeksky.xcache.store.no;

import com.igeeksky.xcache.Cache;
import com.igeeksky.xcache.beans.BeanContext;
import com.igeeksky.xcache.config.CacheProperties;
import com.igeeksky.xcache.extension.ExtensionHelper;
import com.igeeksky.xcache.extension.monitor.CacheMonitor;
import com.igeeksky.xcache.store.AbstractCacheStoreProvider;

import java.util.List;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-20
 */
public class NoOpCacheStoreProvider extends AbstractCacheStoreProvider {

    @Override
    public <K, V> Cache<K, V> get(String name, CacheProperties cacheProperties, Class<K> keyType, Class<V> valueType) {
        BeanContext beanContext = cacheProperties.getBeanContext();
        CacheProperties.Generic generic = cacheProperties.getGeneric();

        String id = generic.getStatisticsPublisher();
        boolean enableStatistics = generic.isEnableStatistics();
        String application = cacheProperties.getApplication();

        List<CacheMonitor<K, V>> cacheMonitors = ExtensionHelper.statisticsMonitor(id, beanContext, enableStatistics,
                name, NoOpCacheStore.STORE_TYPE, application);

        return new NoOpCacheStore<>(name, generic, keyType, valueType, cacheMonitors);
    }

}
