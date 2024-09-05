package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.CacheRefresh;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/4
 */
public class CacheRefreshProxy implements CacheRefresh {

    private final CacheRefresh cacheRefresh;

    public CacheRefreshProxy(CacheRefresh cacheRefresh, Consumer<String> consumer) {
        this.cacheRefresh = cacheRefresh;
        this.setConsumer(consumer);
    }

    @Override
    public void access(String key) {
        if (cacheRefresh != null) {
            cacheRefresh.access(key);
        }
    }

    @Override
    public void accessAll(Set<String> keys) {
        if (cacheRefresh != null) {
            cacheRefresh.accessAll(keys);
        }
    }

    @Override
    public void remove(String key) {
        if (cacheRefresh != null) {
            cacheRefresh.remove(key);
        }
    }


    @Override
    public void removeAll(Set<String> keys) {
        if (cacheRefresh != null) {
            cacheRefresh.removeAll(keys);
        }
    }

    @Override
    public void setConsumer(Consumer<String> consumer) {
        if (cacheRefresh != null) {
            cacheRefresh.setConsumer(consumer);
        }
    }

    @Override
    public boolean checkRefreshTasks(AtomicReference<Future<?>[]> futuresRef, long timeout) {
        if (cacheRefresh != null) {
            return cacheRefresh.checkRefreshTasks(futuresRef, timeout);
        }
        return CacheRefresh.super.checkRefreshTasks(futuresRef, timeout);
    }

}