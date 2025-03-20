package com.igeeksky.xcache.extension.refresh;

import com.igeeksky.xtool.core.collection.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 缓存数据刷新代理
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class CacheRefreshProxy implements CacheRefresh {

    private static final Logger log = LoggerFactory.getLogger(CacheRefreshProxy.class);

    private final String name;
    private final CacheRefresh cacheRefresh;

    public CacheRefreshProxy(String name, CacheRefresh cacheRefresh) {
        this.name = name;
        this.cacheRefresh = cacheRefresh;
    }

    @Override
    public void onPut(String key) {
        if (cacheRefresh == null || key == null) {
            return;
        }
        try {
            cacheRefresh.onPut(key);
        } catch (Exception e) {
            log.error("Cache: {} ,CacheRefresh onPut has error. {}", name, e.getMessage(), e);
        }
    }

    @Override
    public void onPutAll(Set<String> keys) {
        if (cacheRefresh == null || CollectionUtils.isEmpty(keys)) {
            return;
        }
        try {
            cacheRefresh.onPutAll(keys);
        } catch (Exception e) {
            log.error("Cache: {} ,CacheRefresh onPutAll has error. {}", name, e.getMessage(), e);
        }
    }

    @Override
    public void onRemove(String key) {
        if (cacheRefresh == null || key == null) {
            return;
        }
        try {
            cacheRefresh.onRemove(key);
        } catch (Exception e) {
            log.error("Cache: {} ,CacheRefresh onRemove has error. {}", name, e.getMessage(), e);
        }
    }

    @Override
    public void onRemoveAll(Set<String> keys) {
        if (cacheRefresh == null || CollectionUtils.isEmpty(keys)) {
            return;
        }
        try {
            cacheRefresh.onRemoveAll(keys);
        } catch (Exception e) {
            log.error("Cache: {} ,CacheRefresh onRemoveAll has error. {}", name, e.getMessage(), e);
        }
    }

    @Override
    public void startRefresh(Consumer<String> consumer, Predicate<String> predicate) {
        if (cacheRefresh != null) {
            cacheRefresh.startRefresh(consumer, predicate);
        }
    }

    @Override
    public void shutdown() {
        if (cacheRefresh != null) {
            cacheRefresh.shutdown();
        }
    }

    @Override
    public void shutdown(long quietPeriod, long timeout, TimeUnit unit) {
        if (cacheRefresh != null) {
            cacheRefresh.shutdown(quietPeriod, timeout, unit);
        }
    }

    @Override
    public CompletableFuture<Void> shutdownAsync() {
        if (cacheRefresh != null) {
            return cacheRefresh.shutdownAsync();
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> shutdownAsync(long quietPeriod, long timeout, TimeUnit unit) {
        if (cacheRefresh != null) {
            return cacheRefresh.shutdownAsync(quietPeriod, timeout, unit);
        }
        return CompletableFuture.completedFuture(null);
    }

}