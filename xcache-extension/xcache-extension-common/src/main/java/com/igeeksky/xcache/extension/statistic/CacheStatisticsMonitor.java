package com.igeeksky.xcache.extension.statistic;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.extension.monitor.CacheMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 缓存指标统计实例的Holder
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-11
 */
public class CacheStatisticsMonitor<K, V> implements CacheMonitor<K, V> {

    private final Logger logger = LoggerFactory.getLogger(CacheStatisticsMonitor.class);

    private final String name;
    private final String storeType;
    private final String application;

    private final AtomicReference<CacheStatisticsCounter> reference = new AtomicReference<>(new CacheStatisticsCounter());

    public CacheStatisticsMonitor(String name, String storeType, String application) {
        this.name = name;
        this.storeType = storeType;
        this.application = application;
    }

    @Override
    public void afterGet(K key, CacheValue<V> cacheValue) {
        if (null == cacheValue) {
            reference.get().incMisses();
            return;
        }
        if (cacheValue.hasValue()) {
            reference.get().incNotNullHits();
        } else {
            reference.get().incNullHits();
        }
    }

    @Override
    public void afterGetAll(Collection<KeyValue<K, CacheValue<V>>> results) {
        results.forEach(kv -> afterGet(kv.getKey(), kv.getValue()));
    }

    @Override
    public void afterLoad(K key, V value) {
        if (null != value) {
            reference.get().incNotNullLoads();
        } else {
            reference.get().incNullLoads();
        }
    }

    @Override
    public void afterLoadAll(Map<? extends K, ? extends V> keyValues) {
        keyValues.forEach(this::afterLoad);
    }

    @Override
    public void afterPut(K key, V value) {
        reference.get().incPuts(1L);
    }

    @Override
    public void afterPutAll(Map<? extends K, ? extends V> keyValues) {
        reference.get().incPuts(keyValues.size());
    }

    @Override
    public void afterRemove(K key) {
        reference.get().incRemovals(1L);
    }

    @Override
    public void afterRemoveAll(Set<? extends K> keys) {
        reference.get().incRemovals(keys.size());
    }

    @Override
    public void afterClear() {
        reference.get().incClears();
    }

    public CacheStatisticsMessage collect() {
        CacheStatisticsCounter counter = reference.getAndSet(new CacheStatisticsCounter());
        CacheStatisticsMessage message = new CacheStatisticsMessage(name, storeType, application, counter);
        if (logger.isDebugEnabled()) {
            logger.debug(message.toString());
        }
        return message;
    }

}
