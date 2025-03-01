package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.xcache.extension.refresh.CacheRefresh;
import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xcache.extension.refresh.RefreshHelper;
import com.igeeksky.xredis.common.Limit;
import com.igeeksky.xredis.common.Range;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xredis.common.ScoredValue;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import com.igeeksky.xtool.core.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
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
     * 刷新任务分批的 Future 数组长度
     */
    protected static final int FUTURES_LENGTH = 1024;

    /**
     * 锁：保证刷新任务仅启动一次
     */
    private final Lock lock = new ReentrantLock();

    private final String sid;
    private final String name;
    protected final long syncTimeout;
    private final RedisOperatorProxy operator;
    private final ScheduledExecutorService scheduler;
    private volatile ScheduledFuture<?> scheduledFuture;

    /**
     * 缓存数据刷新周期
     */
    private final int refreshAfterWrite;
    /**
     * 刷新线程运行周期
     */
    private final long refreshThreadPeriod;

    private final byte[][] lockKeys = new byte[1][];
    private final byte[][] lockArgs = new byte[2][];

    /**
     * 任务状态：<br>
     * 0：尝试获取锁，如加锁成功则再次执行刷新任务；<br>
     * (0, unlockState)：已解锁，任务队列已完成；<br>
     * unlockState：需解锁，任务队列已完成；<br>
     * lockState：已加锁，任务队列未完成，锁需要定时续期。
     * <p>
     * 只有当状态递减到 0 时，才再次启动刷新任务。<br>
     * 引入任务状态，是为了将 {@code refreshThreadPeriod} 时间周期切分，一旦检查到任务列表全部完成则解锁，
     * 尽可能缩短锁存续时间，让其它进程实例可以更快地获取锁，从而使得数据刷新过程更为平缓，避免出现大的刷新波峰。
     */
    private final AtomicInteger taskState = new AtomicInteger(0);

    private static final int MIN_STATE = 2;
    private static final int MAX_STATE = 10;

    private final int lockState;
    private final int unlockState;

    /**
     * 刷新任务最大数量
     */
    protected final int refreshTasksSize;

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

    protected final ArrayList<Tuple2<Future<?>[], Integer>> tasksList = new ArrayList<>();

    public AbstractRedisCacheRefresh(RefreshConfig config, ScheduledExecutorService scheduler,
                                     ExecutorService executor, RedisOperatorProxy operator) {
        this.stringCodec = StringCodec.getInstance(config.getCharset());
        this.sid = config.getSid();
        this.name = config.getName();
        this.refreshTasksSize = config.getRefreshTasksSize();
        this.refreshAfterWrite = config.getRefreshAfterWrite();
        this.refreshThreadPeriod = config.getRefreshThreadPeriod();
        this.lockState = (int) Math.min(MAX_STATE, Math.max(MIN_STATE, refreshThreadPeriod / 100));
        this.unlockState = lockState - 1;
        this.operator = operator;
        this.syncTimeout = operator.getTimeout();
        this.executor = executor;
        this.scheduler = scheduler;
        this.lockKeys[0] = stringCodec.encode(config.getRefreshLockKey());
        this.lockArgs[0] = stringCodec.encode(config.getSid());
        this.lockArgs[1] = stringCodec.encode(Long.toString(refreshThreadPeriod));
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
            long delay = Math.max(1, refreshThreadPeriod / lockState);
            this.scheduledFuture = scheduler.scheduleWithFixedDelay(this::refreshTask,
                    delay, delay, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 定时刷新任务
     */
    private void refreshTask() {
        ScheduledFuture<?> extendExpireFuture = null;
        try {
            // 1. 当前进程任务队列是否已全部完成
            if (taskState.get() == lockState && RefreshHelper.tasksUnfinished(tasksList)) {
                this.extendLockExpire();
                return;
            }
            // 2. 如果状态递减到 0，则再次启动刷新任务，否则返回
            int state = taskState.decrementAndGet();
            if (state > 0) {
                if (state == unlockState) {
                    this.unlock();
                }
                return;
            }
            // 2. 如果无其它进程正在执行刷新任务，加锁成功；否则加锁失败，返回
            if (!this.tryLock()) {
                return;
            }
            taskState.set(lockState);
            // 3. 成功加锁后，任务执行期间，锁需要定时续期
            extendExpireFuture = scheduler.scheduleWithFixedDelay(this::extendLockExpire,
                    refreshThreadPeriod, refreshThreadPeriod, TimeUnit.MILLISECONDS);
            // 4. 读取队列，刷新已到刷新时间的数据
            doRefresh();
        } catch (Throwable e) {
            log.error("Cache:{}, CacheRefresh task has error[1]. {}", name, e.getMessage());
        } finally {
            if (extendExpireFuture != null) {
                extendExpireFuture.cancel(true);
            }
        }
    }

    /**
     * 读取队列，刷新已到刷新时间的数据
     */
    protected abstract void doRefresh();

    /**
     * 先解锁后加锁
     *
     * @return true 加锁成功；false 加锁失败
     */
    private boolean tryLock() {
        Boolean locked = this.operator.evalsha(RedisRefreshScript.LOCK, lockKeys, lockArgs);
        return (locked != null && locked);
    }

    private void unlock() {
        this.operator.evalshaAsync(RedisRefreshScript.UNLOCK, lockKeys, lockArgs)
                .whenComplete((locked, t) -> {
                    if (t != null) {
                        log.error("Cache: {}, sid: {}, CacheRefresh unlock failed.", name, sid, t);
                        return;
                    }
                    if (locked == null || !(boolean) locked) {
                        if (log.isWarnEnabled()) {
                            log.warn("Cache: {}, sid: {}, CacheRefresh unlock failed.", name, sid);
                        }
                    }
                });
    }

    private void extendLockExpire() {
        this.operator.evalshaAsync(RedisRefreshScript.LOCK, lockKeys, lockArgs)
                .whenComplete((locked, t) -> {
                    if (t != null) {
                        log.error("Cache: {}, sid: {}, CacheRefresh extend lock expire failed.", name, sid, t);
                        return;
                    }
                    if (locked == null || !(boolean) locked) {
                        if (log.isWarnEnabled()) {
                            log.warn("Cache: {}, sid: {}, CacheRefresh extend lock expire failed.", name, sid);
                        }
                    }
                    log.info("Cache: {}, sid: {}, CacheRefresh extend lock expire result:{}.", name, sid, locked);
                });
    }

    /**
     * 添加成员集合（设置或更新刷新时间）
     *
     * @param refreshKey Redis-SortedSet key
     * @param members    需刷新的键集
     * @return {@code CompletableFuture<Long>} – 新增成员数量
     */
    @SuppressWarnings("unchecked")
    protected CompletableFuture<Long> put(byte[] refreshKey, List<byte[]> members) {
        return this.operator.timeMillisAsync()
                .thenCompose(serverTime -> {
                    int size = members.size();
                    long refreshTime = serverTime + refreshAfterWrite;
                    ScoredValue<byte[]>[] scoredValues = new ScoredValue[size];
                    for (int i = 0; i < size; i++) {
                        scoredValues[i] = ScoredValue.just(refreshTime, members.get(i));
                    }
                    return this.operator.zaddAsync(refreshKey, scoredValues);
                }).whenComplete((num, t) -> {
                    if (t != null) {
                        log.error("Cache: {}, CacheRefresh process put_event failed. {}", name, t.getMessage(), t);
                    }
                });
    }

    /**
     * 添加成员（设置或更新刷新时间）
     *
     * @param refreshKey Redis-SortedSet key
     * @param member     需刷新的成员
     * @return {@code CompletableFuture<Long>} – 新增成员数量
     */
    protected CompletableFuture<Long> put(byte[] refreshKey, byte[] member) {
        return this.operator.timeMillisAsync()
                .thenCompose(serverTime -> {
                    long refreshTime = serverTime + refreshAfterWrite;
                    return this.operator.zaddAsync(refreshKey, refreshTime, member);
                })
                .whenComplete((num, t) -> {
                    if (t != null) {
                        log.error("Cache: {}, CacheRefresh process put_event failed. {}", name, t.getMessage(), t);
                    }
                });
    }

    /**
     * 删除成员集合
     *
     * @param refreshKey Redis-SortedSet key
     * @param members    待删除的成员集合
     * @return {@code CompletableFuture<Long>} – 删除成员数量
     */
    protected CompletableFuture<Long> remove(byte[] refreshKey, byte[]... members) {
        return this.operator.zremAsync(refreshKey, members)
                .whenComplete((num, t) -> {
                    if (t != null) {
                        log.error("Cache: {}, CacheRefresh process remove_event failed. {}", name, t.getMessage(), t);
                    }
                });
    }

    /**
     * 获取待刷新的成员集合
     *
     * @param refreshKey sorted set key
     * @param now        当前时间
     * @param count      获取数量
     * @return 待刷新的键集
     */
    protected List<byte[]> getRefreshMembers(byte[] refreshKey, long now, int count) {
        // TODO 使用脚本获取数据并更新刷新时间，增加偏移？
        Limit limit = Limit.from(count);
        Range<? extends Number> range = Range.closed(0, now);
        return this.operator.zrangebyscore(refreshKey, range, limit);
    }

    /**
     * 获取 RedisServer 当前时间（毫秒）
     *
     * @return {@code UnixTimestamp(millis)}
     */
    protected long getServerTime() {
        return this.operator.timeMillis();
    }

    @Override
    public void close() {
        ScheduledFuture<?> future = this.scheduledFuture;
        if (future != null) {
            future.cancel(false);
            this.scheduledFuture = null;
        }
    }

}