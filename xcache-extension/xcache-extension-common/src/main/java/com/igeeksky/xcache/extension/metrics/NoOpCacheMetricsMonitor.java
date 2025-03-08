package com.igeeksky.xcache.extension.metrics;

import com.igeeksky.xcache.props.StoreLevel;

/**
 * 无操作类
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/9
 */
public class NoOpCacheMetricsMonitor implements CacheMetricsMonitor {

    private static final NoOpCacheMetricsMonitor INSTANCE = new NoOpCacheMetricsMonitor();

    private NoOpCacheMetricsMonitor() {
    }

    public static NoOpCacheMetricsMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    public void incHits(StoreLevel level, long times) {
    }

    @Override
    public void incMisses(StoreLevel level, long times) {
    }

    @Override
    public void incPuts(StoreLevel level, long times) {
    }

    @Override
    public void incHitLoads(long times) {
    }

    @Override
    public void incMissLoads(long times) {
    }

    @Override
    public void incRemovals(StoreLevel level, long times) {
    }

    @Override
    public void incClears(StoreLevel level) {
    }

    @Override
    public CacheMetricsMessage collect() {
        return null;
    }

    @Override
    public void setCounter(StoreLevel level) {
    }

}