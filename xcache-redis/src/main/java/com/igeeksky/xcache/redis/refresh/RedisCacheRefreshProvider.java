package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.xcache.extension.refresh.CacheRefreshProvider;
import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xtool.core.concurrent.VirtualThreadFactory;
import com.igeeksky.xtool.core.io.IOUtils;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Redis 缓存数据刷新工厂类
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/2
 */
public class RedisCacheRefreshProvider implements CacheRefreshProvider {

    private final Map<String, AbstractRedisCacheRefresh> container = new ConcurrentHashMap<>();

    private final RedisOperatorProxy operator;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    public RedisCacheRefreshProvider(RedisOperatorProxy operator, ScheduledExecutorService scheduler) {
        Assert.notNull(operator, "RedisOperatorProxy must not be null");
        Assert.notNull(scheduler, "ScheduledExecutorService must not be null");
        this.operator = operator;
        this.executor = executor();
        this.scheduler = scheduler;
    }

    @Override
    public AbstractRedisCacheRefresh getCacheRefresh(RefreshConfig config) {
        return this.container.computeIfAbsent(config.getName(), name -> {
            if (this.operator.isCluster()) {
                return new RedisClusterCacheRefresh(config, scheduler, executor, operator);
            } else {
                return new RedisCacheRefresh(config, scheduler, executor, operator);
            }
        });
    }

    private static ExecutorService executor() {
        return Executors.newThreadPerTaskExecutor(new VirtualThreadFactory("redis-refresh-thread-"));
    }

    @Override
    public void close() {
        container.forEach((name, refresh) -> IOUtils.closeQuietly(refresh));
    }

}