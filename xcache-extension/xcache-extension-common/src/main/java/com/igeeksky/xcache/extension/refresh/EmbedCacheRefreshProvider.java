package com.igeeksky.xcache.extension.refresh;

import com.igeeksky.xtool.core.concurrent.VirtualThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/28
 */
public class EmbedCacheRefreshProvider implements CacheRefreshProvider {

    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    public EmbedCacheRefreshProvider(ScheduledExecutorService scheduler) {
        this.executor = executor();
        this.scheduler = scheduler;
    }

    @Override
    public CacheRefresh getCacheRefresh(RefreshConfig config) {
        return new EmbedCacheRefresh(config, scheduler, executor);
    }

    private static ExecutorService executor() {
        return Executors.newThreadPerTaskExecutor(new VirtualThreadFactory("embed-cache-refresh-thread-"));
    }

}