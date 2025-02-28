package com.igeeksky.xcache.extension.refresh;

import com.igeeksky.xtool.core.concurrent.VirtualThreadFactory;
import com.igeeksky.xtool.core.io.IOUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 内嵌缓存刷新器提供者
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/28
 */
public class EmbedCacheRefreshProvider implements CacheRefreshProvider {

    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    private final Map<String, EmbedCacheRefresh> container = new ConcurrentHashMap<>();

    public EmbedCacheRefreshProvider(ScheduledExecutorService scheduler) {
        this.executor = executor();
        this.scheduler = scheduler;
    }

    @Override
    public CacheRefresh getCacheRefresh(RefreshConfig config) {
        return container.computeIfAbsent(config.getName(), name -> new EmbedCacheRefresh(config, scheduler, executor));
    }

    private static ExecutorService executor() {
        return Executors.newThreadPerTaskExecutor(new VirtualThreadFactory("embed-cache-refresh-thread-"));
    }

    @Override
    public void close() {
        container.forEach((name, refresh) -> IOUtils.closeQuietly(refresh));
    }

}