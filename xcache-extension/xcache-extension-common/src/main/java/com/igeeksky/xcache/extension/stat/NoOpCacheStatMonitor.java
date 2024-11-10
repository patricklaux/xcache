package com.igeeksky.xcache.extension.stat;

import com.igeeksky.xcache.props.StoreLevel;

/**
 * 无操作类
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/9
 */
public class NoOpCacheStatMonitor implements CacheStatMonitor {

    private static final NoOpCacheStatMonitor INSTANCE = new NoOpCacheStatMonitor();

    private NoOpCacheStatMonitor() {
    }

    public static NoOpCacheStatMonitor getInstance() {
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
    public CacheStatMessage collect() {
        return null;
    }

    @Override
    public void setCounter(StoreLevel level) {
    }

}