package com.igeeksky.xcache.extension.refresh;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
    public void onPut(String key) {
    }

    @Override
    public void onPutAll(Set<String> keys) {
    }

    @Override
    public void onRemove(String key) {
    }

    @Override
    public void onRemoveAll(Set<String> keys) {
    }

    @Override
    public void startRefresh(Consumer<String> consumer, Predicate<String> predicate) {
    }

    @Override
    public void close() throws Exception {
    }
}