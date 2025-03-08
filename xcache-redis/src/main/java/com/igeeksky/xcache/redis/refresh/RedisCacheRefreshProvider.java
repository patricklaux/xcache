package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.xcache.extension.refresh.CacheRefreshProvider;
import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xcache.extension.refresh.RefreshHelper;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xtool.core.concurrent.Futures;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis 缓存数据刷新工厂类
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/2
 */
public class RedisCacheRefreshProvider implements CacheRefreshProvider {

    private final ExecutorService executor;
    private final RedisOperatorProxy operator;
    private final ScheduledExecutorService scheduler;

    private final AtomicLong maxShutdownTimeout = new AtomicLong(1);
    private final Map<String, AbstractRedisCacheRefresh> container = new ConcurrentHashMap<>();

    public RedisCacheRefreshProvider(RedisOperatorProxy operator, ScheduledExecutorService scheduler) {
        Assert.notNull(operator, "RedisOperatorProxy must not be null");
        Assert.notNull(scheduler, "ScheduledExecutorService must not be null");
        this.operator = operator;
        this.executor = Executors.newThreadPerTaskExecutor(RefreshHelper.VIRTUAL_FACTORY);
        this.scheduler = scheduler;
    }

    @Override
    public AbstractRedisCacheRefresh getCacheRefresh(RefreshConfig config) {
        RefreshHelper.resetMaxShutdownTimeout(maxShutdownTimeout, config.getShutdownTimeout());
        return this.container.computeIfAbsent(config.getName(), name -> {
            if (this.operator.isCluster()) {
                return new RedisClusterCacheRefresh(config, scheduler, executor, operator);
            } else {
                return new RedisCacheRefresh(config, scheduler, executor, operator);
            }
        });
    }

    @Override
    public void close() {
        ArrayList<Future<?>> futures = new ArrayList<>(container.size());
        container.forEach((name, refresh) -> {
            try {
                futures.add(refresh.shutdownAsync());
            } catch (Exception ignored) {
            }
        });
        Futures.awaitAll(futures, maxShutdownTimeout.get(), TimeUnit.MILLISECONDS);
    }

}