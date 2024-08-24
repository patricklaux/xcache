package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.extension.refresh.CacheRefresh;
import com.igeeksky.xcache.extension.refresh.CacheRefreshProvider;
import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xtool.core.concurrent.VirtualThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/2
 */
public class RedisCacheRefreshProvider implements CacheRefreshProvider {

    private final ExecutorService executor;
    private final RedisOperator connection;
    private final ScheduledExecutorService scheduler;

    public RedisCacheRefreshProvider(ScheduledExecutorService scheduler, RedisOperator connection) {
        this.executor = executor();
        this.scheduler = scheduler;
        this.connection = connection;
    }

    @Override
    public CacheRefresh getCacheRefresh(RefreshConfig config) {
        return new RedisCacheRefresh(config, scheduler, executor, connection);
    }

    private static ExecutorService executor() {
        return Executors.newThreadPerTaskExecutor(new VirtualThreadFactory("redis-refresh-thread-"));
    }

}