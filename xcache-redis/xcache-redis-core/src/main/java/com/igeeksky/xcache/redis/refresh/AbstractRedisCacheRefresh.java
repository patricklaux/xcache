package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.extension.refresh.CacheRefresh;
import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xcache.extension.refresh.RefreshHelper;
import com.igeeksky.xtool.core.function.tuple.Tuple2;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Redis 缓存刷新抽象类
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/29
 */
public abstract class AbstractRedisCacheRefresh implements CacheRefresh {

    private static final Logger log = LoggerFactory.getLogger(AbstractRedisCacheRefresh.class);

    /**
     * 单个队列最大刷新任务数量
     */
    protected static final int FUTURES_LENGTH = 1024;

    private final Lock lock = new ReentrantLock();

    /**
     * 缓存名称
     */
    private final String name;

    /**
     * 刷新任务队列最大容量
     */
    protected final int refreshTasksSize;

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

    private final ScheduledExecutorService scheduler;

    private volatile ScheduledFuture<?> scheduledFuture;

    protected final ArrayList<Tuple2<Future<?>[], Integer>> tasksList = new ArrayList<>();

    /**
     * 刷新线程运行周期
     */
    private final long refreshThreadPeriod;

    private final byte[][] lockKeys = new byte[1][];
    private final byte[][] lockArgs = new byte[2][];

    private final byte[][] threadPeriodKeys = new byte[1][];    // 刷新线程运行周期的键
    private final byte[][] threadPeriodArgs = new byte[1][];    // 刷新线程运行周期的时间参数

    protected final byte[] refreshAfterWrite;   // 数据刷新周期

    public AbstractRedisCacheRefresh(RefreshConfig config, ScheduledExecutorService scheduler,
                                     ExecutorService executor, RedisOperator operator) {
        this.stringCodec = StringCodec.getInstance(config.getCharset());
        this.name = config.getName();
        this.refreshTasksSize = config.getRefreshTasksSize();
        this.refreshThreadPeriod = config.getRefreshThreadPeriod();
        this.operator = operator;
        this.executor = executor;
        this.scheduler = scheduler;
        this.lockKeys[0] = stringCodec.encode(config.getRefreshLockKey());
        this.lockArgs[0] = stringCodec.encode(config.getSid());
        this.lockArgs[1] = stringCodec.encode(Long.toString(refreshThreadPeriod + 5000));
        this.threadPeriodKeys[0] = stringCodec.encode(config.getRefreshPeriodKey());
        this.threadPeriodArgs[0] = stringCodec.encode(Long.toString(refreshThreadPeriod));
        this.refreshAfterWrite = stringCodec.encode(Long.toString(config.getRefreshAfterWrite()));
    }

    @Override
    public void onPut(String key) {
        try {
            this.doPut(key);
        } catch (Exception e) {
            log.error("Cache: {} ,CacheRefresh process put_event has error. {}", name, e.getMessage(), e);
        }
    }

    protected abstract void doPut(String key);

    @Override
    public void onPutAll(Set<String> keys) {
        try {
            this.doPutAll(keys);
        } catch (Exception e) {
            log.error("Cache: {} ,CacheRefresh process put_all_event has error. {}", name, e.getMessage(), e);
        }
    }

    protected abstract void doPutAll(Set<String> keys);

    @Override
    public void onRemove(String key) {
        try {
            this.doRemove(key);
        } catch (Exception e) {
            log.error("Cache: {} ,CacheRefresh process remove_event has error. {}", name, e.getMessage(), e);
        }
    }

    protected abstract void doRemove(String key);

    @Override
    public void onRemoveAll(Set<String> keys) {
        try {
            this.doRemoveAll(keys);
        } catch (Exception e) {
            log.error("Cache: {} ,CacheRefresh process remove_all_event has error. {}", name, e.getMessage(), e);
        }
    }

    protected abstract void doRemoveAll(Set<String> keys);

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
                    refreshThreadPeriod, refreshThreadPeriod, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 启动定时刷新任务
     */
    private void refreshTask() {
        ScheduledFuture<?> newExpireFuture = null;
        try {
            // 1. 当前进程任务队列是否全部完成
            if (RefreshHelper.tasksUnfinished(tasksList)) {
                this.lockNewExpire();
                return;
            }
            // 2. 其它进程是否有在执行刷新任务，如果没有则加锁
            if (!this.lock()) {
                return;
            }
            // 提交周期任务：执行锁续期
            newExpireFuture = scheduler.scheduleWithFixedDelay(this::lockNewExpire,
                    refreshThreadPeriod, refreshThreadPeriod, TimeUnit.MILLISECONDS);
            // 3. 判断是否未到达刷新任务的执行时间
            if (!arrivedNextTaskTime()) {
                return;
            }
            // 4. 读取队列，刷新已到刷新时间的数据
            refresh();
            // 5. 更新刷新任务的下次执行时间
            updateNextTaskTime();
            // 6. 延长锁的过期时间
            lockNewExpire();
        } catch (Throwable e) {
            log.error("Cache:{} ,CacheRefresh refresh task has error. {}", name, e.getMessage());
        } finally {
            if (newExpireFuture != null) {
                newExpireFuture.cancel(true);
            }
        }
    }

    /**
     * 读取队列，刷新已到刷新时间的数据
     */
    protected abstract void refresh();

    /**
     * 先解锁后加锁
     * <p>
     * 为避免线程启动之前锁已过期，锁的过期时间必须大于刷新线程运行周期。<br>
     * 锁存续时间 = refreshThreadPeriod + 5000ms
     *
     * @return true 加锁成功；false 加锁失败
     */
    private boolean lock() {
        Boolean locked = this.operator.evalsha(RedisRefreshScript.LOCK, lockKeys, lockArgs);
        return locked != null && locked;
    }

    private void lockNewExpire() {
        this.operator.evalsha(RedisRefreshScript.LOCK_NEW_EXPIRE, lockKeys, lockArgs);
    }

    /**
     * 判断是否未到达数据刷新的计划执行时间
     *
     * @return true 已到计划时间；false 未到计划时间
     */
    private boolean arrivedNextTaskTime() {
        return this.operator.evalshaReadOnly(RedisRefreshScript.ARRIVED_TASK_TIME, threadPeriodKeys);
    }

    /**
     * 更新刷新任务的下次执行时间
     */
    private void updateNextTaskTime() {
        long nextTaskTime = this.operator.evalsha(RedisRefreshScript.UPDATE_TASK_TIME, threadPeriodKeys, threadPeriodArgs);
        if (nextTaskTime <= 0) {
            log.error("Cache: {} ,CacheRefresh thread next start time must be greater than 0. refreshThreadPeriod: {}, nextTime: {}",
                    name, refreshThreadPeriod, nextTaskTime);
        }
    }

    /**
     * 添加或更新下次刷新时间
     *
     * @param keys keys[0] refreshKey
     * @param args args[0] refreshAfterWrite args[1~n] members
     */
    protected void put(byte[][] keys, byte[][] args) {
        long nextRefreshTime = this.operator.evalsha(RedisRefreshScript.PUT, keys, args);
        if (nextRefreshTime <= 0) {
            log.error("Cache: {} ,CacheRefresh next refresh time must be greater than 0. refreshAfterWrite: {}, nextTime: {}",
                    name, refreshAfterWrite, nextRefreshTime);
        }
    }

    /**
     * 删除下次刷新时间
     *
     * @param keys keys[0] refreshKey
     * @param args args[0] refreshAfterWrite args[1~n] members
     */
    protected void remove(byte[][] keys, byte[][] args) {
        this.operator.evalsha(RedisRefreshScript.REMOVE, keys, args);
    }

    /**
     * 获取待刷新的 keys
     *
     * @param refreshKey 刷新key
     * @param now        当前时间
     * @param count      获取数量
     * @return 待刷新的keys
     */
    protected List<byte[]> getRefreshMembers(byte[] refreshKey, long now, int count) {
        return this.operator.zrangebyscore(refreshKey, 0, now, 0, count);
    }

    /**
     * 获取当前服务器时间
     *
     * @return 当前服务器时间（单位：毫秒）
     */
    protected long getServerTime() {
        return this.operator.timeMillis();
    }

    @Override
    public void close() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }

}