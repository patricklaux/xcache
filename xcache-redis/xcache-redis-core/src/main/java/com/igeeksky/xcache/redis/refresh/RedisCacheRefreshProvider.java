package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.extension.refresh.CacheRefreshProvider;
import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xtool.core.concurrent.VirtualThreadFactory;
import com.igeeksky.xtool.core.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/2
 */
public class RedisCacheRefreshProvider implements CacheRefreshProvider {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheRefreshProvider.class);

    private final Map<String, AbstractRedisCacheRefresh> container = new ConcurrentHashMap<>();

    /**
     * 锁对象，用于锁住加载脚本的线程
     */
    private final Lock lock = new ReentrantLock();

    /**
     * 脚本是否加载完成
     */
    private volatile boolean loadedScript = false;

    private volatile long timeOffset;

    private final RedisOperator operator;
    private final ExecutorService executor;
    private final ScheduledFuture<?> timeFuture;
    private final ScheduledExecutorService scheduler;

    public RedisCacheRefreshProvider(ScheduledExecutorService scheduler, RedisOperator operator) {
        this.operator = operator;
        this.executor = executor();
        this.scheduler = scheduler;
        this.timeFuture = this.scheduler.scheduleAtFixedRate(this::getTimeOffset, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public AbstractRedisCacheRefresh getCacheRefresh(RefreshConfig config) {
        if (!this.loadedScript) {
            this.lock.lock();
            try {
                if (!this.loadedScript) {
                    this.operator.scriptLoad(RedisRefreshScript.LOCK);
                    // this.operator.scriptLoad(RedisRefreshScript.UNLOCK_SCRIPT);
                    this.operator.scriptLoad(RedisRefreshScript.LOCK_NEW_EXPIRE);
                    this.loadedScript = true;
                }
            } finally {
                this.lock.unlock();
            }
        }
        return this.container.computeIfAbsent(config.getName(), name -> {
            AbstractRedisCacheRefresh refresh;
            if (this.operator.isCluster()) {
                refresh = new RedisClusterCacheRefresh(config, scheduler, executor, operator);
            } else {
                refresh = new RedisCacheRefresh(config, scheduler, executor, operator);
            }
            refresh.setTimeOffset(timeOffset);
            return refresh;
        });
    }

    /**
     * 获取 redis 服务器时间与本地时间差值，单位：毫秒
     */
    private void getTimeOffset() {
        try {
            long serverTime = this.operator.timeMillis();
            this.timeOffset = System.currentTimeMillis() - serverTime;
            this.container.forEach((name, refresh) -> refresh.setTimeOffset(timeOffset));
            if (this.timeOffset > 1000) {
                if (log.isWarnEnabled()) {
                    log.warn("TimeOffset:{}\tThe difference between local time and " +
                            "redis server time exceeds 1000ms.", timeOffset);
                }
            }
        } catch (Exception e) {
            log.error("RedisCacheRefreshProvider getTimeOffset error. {}", e.getMessage(), e);
        }
    }

    private static ExecutorService executor() {
        return Executors.newThreadPerTaskExecutor(new VirtualThreadFactory("redis-refresh-thread-"));
    }

    @Override
    public void close() {
        container.forEach((name, refresh) -> IOUtils.closeQuietly(refresh));
        timeFuture.cancel(false);
    }

}