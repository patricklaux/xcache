package com.igeeksky.xcache.extension.refresh;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 缓存数据刷新（无操作类）
 *
 * @author Patrick.Lau
 * @since 1.0.0
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
    public void shutdown() {
    }

    @Override
    public void shutdown(long quietPeriod, long timeout, TimeUnit unit) {
    }

    @Override
    public CompletableFuture<Void> shutdownAsync() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> shutdownAsync(long quietPeriod, long timeout, TimeUnit unit) {
        return CompletableFuture.completedFuture(null);
    }

}