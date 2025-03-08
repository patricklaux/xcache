package com.igeeksky.xcache.extension.refresh;


import com.igeeksky.xcache.extension.TasksInfo;
import com.igeeksky.xtool.core.tuple.Tuple3;
import com.igeeksky.xtool.core.tuple.Tuples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * <b>嵌入式缓存刷新</b>
 * <p>
 * 适用于嵌入式缓存 <p>
 * 1. 每个应用实例缓存均需独立刷新，同一数据如果存在于不同实例，那么会被多个实例刷新。
 * 因此会有较多回源访问次数，数据源需预留好足够的资源余量。<p>
 * 2. 此对象实例内部使用 HashMap 维护所有访问过的 key，因此会占用本机内存空间。
 * <p>
 * 对于外部缓存，如果是 Redis，建议使用 {@code com.igeeksky.xcache.redis.refresh.RedisCacheRefresh }
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/23
 */
public class EmbedCacheRefresh implements CacheRefresh {

    private static final Logger log = LoggerFactory.getLogger(EmbedCacheRefresh.class);

    /**
     * 刷新任务分批的 Future 数组长度
     */
    private static final int FUTURES_LENGTH = 1024;

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

    /**
     * 刷新线程运行周期
     */
    private final long refreshThreadPeriod;

    protected volatile boolean shutdown = false;

    private final RefreshConfig config;

    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    private volatile Consumer<String> consumer;
    private volatile Predicate<String> predicate;

    /**
     * 刷新队列（因为只有一个定时任务线程执行读写操作，所以无需考虑线程安全问题）
     */
    private final LinkedHashMap<String, Long> refreshQueue = new LinkedHashMap<>();

    /**
     * 临时队列
     */
    private final ConcurrentLinkedQueue<Tuple3<String, Long, RefreshEventType>> buffer = new ConcurrentLinkedQueue<>();

    private final Lock lock = new ReentrantLock();
    private volatile ScheduledFuture<?> scheduledFuture;
    private final Queue<TasksInfo> tasksQueue = new ConcurrentLinkedQueue<>();

    public EmbedCacheRefresh(RefreshConfig config, ScheduledExecutorService scheduler, ExecutorService executor) {
        this.name = config.getName();
        this.config = config;
        this.executor = executor;
        this.scheduler = scheduler;
        this.refreshTasksSize = config.getRefreshTasksSize();
        this.refreshAfterWrite = config.getRefreshAfterWrite();
        this.refreshThreadPeriod = config.getRefreshThreadPeriod();
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
        return System.currentTimeMillis() + refreshAfterWrite;
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
                    refreshThreadPeriod, refreshThreadPeriod, TimeUnit.MILLISECONDS);
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
            if (RefreshHelper.tasksFinished(tasksQueue)) {
                // 2.临时队列数据迁移至刷新队列
                transferKeys();
                // 3.处理当前时刻需要刷新的数据
                doRefresh();
            }
        } catch (Throwable e) {
            log.error("Cache:{} ,CacheRefresh refresh task has error. {}", name, e.getMessage());
        }
    }

    /**
     * 临时队列数据迁移至刷新队列
     */
    private void transferKeys() {
        if (shutdown) {
            return;
        }
        Tuple3<String, Long, RefreshEventType> tuple;
        while ((tuple = buffer.poll()) != null) {
            if (tuple.getT3() == RefreshEventType.PUT) {
                refreshQueue.putLast(tuple.getT1(), tuple.getT2());
            } else {
                refreshQueue.remove(tuple.getT1());
            }
        }
    }

    private void doRefresh() {
        if (shutdown) {
            return;
        }
        long now = System.currentTimeMillis();
        int i = 0, j = 0, maximum = Math.min(FUTURES_LENGTH, refreshTasksSize);

        Future<?>[] futures = new Future<?>[maximum];
        tasksQueue.add(new TasksInfo(futures));

        for (Map.Entry<String, Long> entry : refreshQueue.entrySet()) {
            // 3.1.如果当前元素未到刷新时间，或任务数已达单个周期上限，则退出循环
            if (shutdown || now < entry.getValue() || ++i >= refreshTasksSize) {
                break;
            }
            // 3.2.执行 put 操作，放入缓冲队列，更新刷新时间，避免下次循环重复刷新（相当于移动 refreshQueue 队尾）
            this.onPut(entry.getKey());
            // 3.3.如果刷新任务数量达到单个容器上限，则创建新的容器
            if (j >= maximum) {
                j = 0;
                futures = new Future<?>[maximum];
                tasksQueue.add(new TasksInfo(futures));
            }
            // 3.4.提交刷新任务
            futures[j++] = executor.submit(new RefreshTask(this, entry.getKey(), consumer, predicate));
        }
    }

    @Override
    public void shutdown() {
        this.shutdown(config.getShutdownQuietPeriod(), config.getShutdownTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown(long quietPeriod, long timeout, TimeUnit unit) {
        try {
            this.shutdownAsync(quietPeriod, timeout, unit).get(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("CacheRefresh:{}, shutdown has error. [{}]", name, "Interrupted", e);
            log.error("Cache:{} ,CacheRefresh close has error. [{}]", name, "Interrupted", e);
        } catch (ExecutionException e) {
            log.error("CacheRefresh:{}, shutdown has error. [{}]", name, e.getMessage(), e.getCause());
        } catch (TimeoutException e) {
            log.error("CacheRefresh:{}, shutdown timeout. wait:[{} {}]", name, timeout, unit.name(), e);
        }
    }

    @Override
    public CompletableFuture<Void> shutdownAsync() {
        return this.shutdownAsync(config.getShutdownQuietPeriod(), config.getShutdownTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public CompletableFuture<Void> shutdownAsync(long quietPeriod, long timeout, TimeUnit unit) {
        if (shutdown) {
            return CompletableFuture.completedFuture(null);
        }
        lock.lock();
        try {
            if (shutdown) {
                return CompletableFuture.completedFuture(null);
            }
            shutdown = true;
            ScheduledFuture<?> future = this.scheduledFuture;
            if (future == null) {
                return CompletableFuture.completedFuture(null);
            }
            this.scheduledFuture = null;
            return RefreshHelper.close(name, future, tasksQueue, quietPeriod, timeout, unit, config.getShutdownBehavior());
        } finally {
            lock.unlock();
        }
    }

}