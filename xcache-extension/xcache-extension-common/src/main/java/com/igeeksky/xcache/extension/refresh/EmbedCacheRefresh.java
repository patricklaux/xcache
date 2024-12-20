package com.igeeksky.xcache.extension.refresh;


import com.igeeksky.xtool.core.function.tuple.Tuple2;
import com.igeeksky.xtool.core.function.tuple.Tuple3;
import com.igeeksky.xtool.core.function.tuple.Tuples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * <b>嵌入式缓存刷新</b>
 * <p>
 * 适用于嵌入式缓存 <p>
 * 1. 每个应用实例缓存均需独立刷新，即使是同一数据，也可能会被不同实例刷新多次。
 * 因此会有较多回源访问次数，数据源需预留好足够的资源余量。<p>
 * 2. 此对象实例内部使用 HashMap 维护所有访问过的 key，因此会占用本机内存空间。
 * <p>
 * 对于外部缓存，如果是 Redis，建议使用 { com.igeeksky.xcache.redis.refresh.RedisCacheRefresh }
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/23
 */
public class EmbedCacheRefresh implements CacheRefresh {

    private static final Logger log = LoggerFactory.getLogger(EmbedCacheRefresh.class);

    /**
     * 刷新任务线程运行周期
     */
    private static final long TASK_DELAY = 1000;

    /**
     * 单个队列最大刷新任务数量
     */
    private static final int MAXIMUM = 1000;

    /**
     * 缓存名称
     */
    private final String name;

    /**
     * 单个周期最大刷新任务数量
     */
    private final int refreshTasksSize;

    /**
     * 键的刷新时间周期
     */
    private final long refreshAfterWrite;

    private Consumer<String> consumer;
    private Predicate<String> predicate;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    /**
     * 刷新队列
     */
    private final LinkedHashMap<String, Long> refreshQueue = new LinkedHashMap<>();

    /**
     * 临时队列
     */
    private final ConcurrentLinkedQueue<Tuple3<String, Long, RefreshEventType>> buffer = new ConcurrentLinkedQueue<>();

    private final Lock lock = new ReentrantLock();
    private volatile ScheduledFuture<?> scheduledFuture;
    private final ArrayList<Tuple2<Future<?>[], Integer>> tasksList = new ArrayList<>();

    public EmbedCacheRefresh(RefreshConfig config, ScheduledExecutorService scheduler, ExecutorService executor) {
        this.name = config.getName();
        this.executor = executor;
        this.scheduler = scheduler;
        this.refreshTasksSize = config.getRefreshTasksSize();
        this.refreshAfterWrite = config.getRefreshAfterWrite();
    }

    @Override
    public void onPut(String key) {
        this.buffer.offer(Tuples.of(key, nextRefreshTime(), RefreshEventType.PUT));
    }

    @Override
    public void onPutAll(Set<String> keys) {
        long nextRefreshTime = nextRefreshTime();
        keys.forEach(key -> this.buffer.offer(Tuples.of(key, nextRefreshTime, RefreshEventType.PUT)));
    }

    @Override
    public void onRemove(String key) {
        this.buffer.offer(Tuples.of(key, 0L, RefreshEventType.REMOVE));
    }

    @Override
    public void onRemoveAll(Set<String> keys) {
        keys.forEach(this::onRemove);
    }

    private long nextRefreshTime() {
        long nextRefreshTime = System.currentTimeMillis() + refreshAfterWrite;
        if (nextRefreshTime > 0) {
            return nextRefreshTime;
        }
        throw new IllegalArgumentException("Cache: " + name + ", nextRefreshTime:" + nextRefreshTime + " overflow.");
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
            this.scheduledFuture = this.scheduler.scheduleWithFixedDelay(this::refreshTask,
                    TASK_DELAY, TASK_DELAY, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 定时刷新任务
     */
    private void refreshTask() {
        try {
            // 1. 任务队列是否已经完成
            if (RefreshHelper.tasksUnfinished(tasksList)) {
                return;
            }

            // 2.临时队列数据迁移至刷新队列
            transferKeys();

            // 3.处理当前时刻需要刷新的数据
            refreshNow();
        } catch (Throwable e) {
            log.error("Cache:{} ,CacheRefresh refresh task has error. {}", name, e.getMessage());
        }
    }

    /**
     * 临时队列数据迁移至刷新队列
     */
    private void transferKeys() {
        Tuple3<String, Long, RefreshEventType> tuple;
        while ((tuple = buffer.poll()) != null) {
            if (tuple.getT3() == RefreshEventType.PUT) {
                refreshQueue.putLast(tuple.getT1(), tuple.getT2());
            } else {
                refreshQueue.remove(tuple.getT1());
            }
        }
    }

    private void refreshNow() {
        long now = System.currentTimeMillis();
        int i = 0, j = 0, maximum = Math.min(MAXIMUM, refreshTasksSize);

        Future<?>[] futures = new Future<?>[maximum];
        tasksList.add(Tuples.of(futures, 0));

        for (Map.Entry<String, Long> entry : refreshQueue.entrySet()) {
            // 3.1.如果当前元素未到刷新时间，或任务数已达单个周期上限，则退出循环
            if (now < entry.getValue() || ++i >= refreshTasksSize) {
                break;
            }
            // 3.2.先移动到队尾，避免重复刷新
            this.onPut(entry.getKey());
            // 3.3.如果刷新任务数量达到单个容器上限，则创建新的容器
            if (j >= maximum) {
                j = 0;
                futures = new Future<?>[maximum];
                tasksList.add(Tuples.of(futures, 0));
            }
            // 3.4.提交刷新任务
            futures[j++] = executor.submit(new RefreshTask(this, entry.getKey(), consumer, predicate));
        }
    }

    @Override
    public void close() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }

}