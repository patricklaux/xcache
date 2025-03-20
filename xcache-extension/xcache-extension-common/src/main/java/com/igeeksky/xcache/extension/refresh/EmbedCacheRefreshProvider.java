package com.igeeksky.xcache.extension.refresh;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内嵌缓存刷新器工厂
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/28
 */
public class EmbedCacheRefreshProvider implements CacheRefreshProvider {

    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    private final AtomicLong maxShutdownTimeout = new AtomicLong(1);
    private final Map<String, CacheRefresh> container = new ConcurrentHashMap<>();

    public EmbedCacheRefreshProvider(ScheduledExecutorService scheduler) {
        this.executor = Executors.newThreadPerTaskExecutor(RefreshHelper.VIRTUAL_FACTORY);
        this.scheduler = scheduler;
    }

    @Override
    public CacheRefresh getCacheRefresh(RefreshConfig config) {
        RefreshHelper.resetMaxShutdownTimeout(maxShutdownTimeout, config.getShutdownTimeout());
        return container.computeIfAbsent(config.getName(), name -> new EmbedCacheRefresh(config, scheduler, executor));
    }

    @Override
    public void close() {
        RefreshHelper.close(container, maxShutdownTimeout.get());
    }

}