package com.igeeksky.xcache.extension.refresh;

import com.igeeksky.xcache.common.CacheRefresh;

import java.util.Set;
import java.util.function.Consumer;

/**
 * 缓存刷新代理
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/4
 */
public class NoOpCacheRefresh implements CacheRefresh {

    private static final NoOpCacheRefresh cacheRefresh = new NoOpCacheRefresh();

    private NoOpCacheRefresh() {
    }

    public static NoOpCacheRefresh getInstance() {
        return cacheRefresh;
    }

    @Override
    public void access(String key) {
    }

    @Override
    public void accessAll(Set<String> keys) {
    }

    @Override
    public void remove(String key) {
    }

    @Override
    public void removeAll(Set<String> keys) {
    }

    @Override
    public void setConsumer(Consumer<String> consumer) {
    }

}