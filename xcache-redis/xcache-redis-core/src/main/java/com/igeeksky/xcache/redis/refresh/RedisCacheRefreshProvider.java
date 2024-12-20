package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.extension.refresh.CacheRefreshProvider;
import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xtool.core.concurrent.VirtualThreadFactory;
import com.igeeksky.xtool.core.io.IOUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Redis 缓存数据刷新工厂类
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/2
 */
public class RedisCacheRefreshProvider implements CacheRefreshProvider {

    private final Map<String, AbstractRedisCacheRefresh> container = new ConcurrentHashMap<>();

    /**
     * 锁对象，用于锁住加载脚本的线程
     */
    private final Lock lock = new ReentrantLock();

    /**
     * 脚本是否加载完成
     */
    private volatile boolean loadedScript = false;

    private final RedisOperator operator;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    public RedisCacheRefreshProvider(ScheduledExecutorService scheduler, RedisOperator operator) {
        this.operator = operator;
        this.executor = executor();
        this.scheduler = scheduler;
    }

    @Override
    public AbstractRedisCacheRefresh getCacheRefresh(RefreshConfig config) {
        if (!this.loadedScript) {
            this.lock.lock();
            try {
                if (!this.loadedScript) {
                    this.operator.scriptLoad(RedisRefreshScript.LOCK);
                    this.operator.scriptLoad(RedisRefreshScript.LOCK_NEW_EXPIRE);
                    this.operator.scriptLoad(RedisRefreshScript.UPDATE_TASK_TIME);
                    this.operator.scriptLoad(RedisRefreshScript.ARRIVED_TASK_TIME);
                    this.operator.scriptLoad(RedisRefreshScript.PUT);
                    this.operator.scriptLoad(RedisRefreshScript.REMOVE);
                    this.loadedScript = true;
                }
            } finally {
                this.lock.unlock();
            }
        }
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