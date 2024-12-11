package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.redis.sorted.ScoredValue;
import com.igeeksky.xcache.extension.lock.LockService;
import com.igeeksky.xcache.extension.refresh.CacheRefresh;
import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xcache.extension.refresh.RefreshHelper;
import com.igeeksky.xtool.core.function.tuple.Tuple2;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/29
 */
public abstract class AbstractRedisCacheRefresh implements CacheRefresh {

    private static final Logger log = LoggerFactory.getLogger(AbstractRedisCacheRefresh.class);

    /**
     * 单个队列最大刷新任务数量
     */
    protected static final int MAXIMUM = 1000;

    private final Lock lock = new ReentrantLock();

    /**
     * 缓存名称
     */
    private final String name;

    /**
     * 时间偏移量 = 本地时间 - 服务器时间
     */
    private volatile long timeOffset;

    /**
     * 刷新任务线程运行周期
     */
    private final long refreshPeriod;

    /**
     * 刷新任务锁（保证同一时间仅有一个刷新任务在运行）
     */
    private final String refreshLockKey;

    /**
     * 同一个键的刷新时间间隔
     */
    private final long refreshAfterWrite;

    /**
     * 刷新任务队列最大容量
     */
    protected final int maxRefreshTasks;

    protected final RedisOperator operator;

    protected final StringCodec stringCodec;

    /**
     * 刷新任务消费接口（执行刷新逻辑）
     */
    protected volatile Consumer<String> consumer;

    /**
     * 刷新任务过滤接口（判断缓存中是否存在该键）
     */
    protected volatile Predicate<String> predicate;

    protected final ExecutorService executor;

    private final LockService lockService;
    private final ScheduledExecutorService scheduler;

    private volatile ScheduledFuture<?> scheduledFuture;

    protected final ArrayList<Tuple2<Future<?>[], Integer>> tasksList = new ArrayList<>();

    public AbstractRedisCacheRefresh(RefreshConfig config, ScheduledExecutorService scheduler,
                                     ExecutorService executor, RedisOperator operator) {
        this.name = config.getName();
        this.refreshPeriod = config.getRefreshPeriod();
        this.refreshLockKey = config.getRefreshLockKey();
        this.maxRefreshTasks = config.getMaxRefreshTasks();
        this.refreshAfterWrite = config.getRefreshAfterWrite();
        this.operator = operator;
        this.executor = executor;
        this.scheduler = scheduler;
        this.lockService = config.getCacheLock();
        this.stringCodec = StringCodec.getInstance(config.getCharset());
    }

    @Override
    public void startRefresh(Consumer<String> consumer, Predicate<String> predicate) {
        if (this.scheduledFuture != null) {
            throw new IllegalStateException("Cache: " + name + ", CacheRefresh has been started.");
        }
        lock.lock();
        try {
            if (this.scheduledFuture != null) {
                throw new IllegalStateException("Cache: " + name + ", CacheRefresh has been started.");
            }
            this.consumer = consumer;
            this.predicate = predicate;
            this.scheduledFuture = scheduler.scheduleWithFixedDelay(this::refreshTask,
                    refreshPeriod, refreshPeriod, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 启动定时刷新任务
     */
    private void refreshTask() {
        try {
            // 1. 任务队列是否全部完成
            if (RefreshHelper.tasksUnfinished(tasksList)) {
                return;
            }

            // 2. 开始执行定时刷新任务
            Lock lock = lockService.acquire(this.refreshLockKey);
            try {
                if (lock.tryLock()) {
                    try {
                        refreshNow();
                    } finally {
                        lock.unlock();
                    }
                }
            } finally {
                lockService.release(this.refreshLockKey);
            }
        } catch (Throwable e) {
            log.error("Cache:{} ,CacheRefresh refresh task has error. {}", name, e.getMessage());
        }
    }

    protected abstract void refreshNow();

    protected void moveToTail(byte[] refreshKey, List<byte[]> members) {
        long nextRefreshTime = nextRefreshTime();
        List<ScoredValue> scoredValues = new ArrayList<>(members.size());
        for (byte[] member : members) {
            if (member != null) {
                scoredValues.add(ScoredValue.just(member, nextRefreshTime));
            }
        }
        this.operator.zadd(refreshKey, scoredValues.toArray(new ScoredValue[0]));
    }

    protected long now() {
        return System.currentTimeMillis() - timeOffset;
    }

    protected long nextRefreshTime() {
        long nextRefreshTime = now() + refreshAfterWrite;
        if (nextRefreshTime > 0) {
            return nextRefreshTime;
        }
        throw new IllegalArgumentException("Cache: " + name + ", nextRefreshTime:" + nextRefreshTime + " overflow.");
    }

    /**
     * 设置时间偏移量
     *
     * @param timeOffset 时间偏移量（如为正数，则表示本地时间快于服务器时间；如为负数，则表示本地时间慢于服务器时间）
     */
    void setTimeOffset(long timeOffset) {
        this.timeOffset = timeOffset;
    }

    @Override
    public void close() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }

}