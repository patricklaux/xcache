package com.igeeksky.xcache.extension.refresh;


import com.igeeksky.xcache.common.CacheRefresh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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
     * 缓存名称
     */
    private final String name;

    /**
     * 刷新间隔周期
     */
    private final long period;

    /**
     * 最后访问之后达到预设时间则停止刷新
     */
    private final long stopAfterAccess;

    private Consumer<String> consumer;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    private final Map<String, Long> accessed = new ConcurrentHashMap<>(128);

    private final AtomicReference<Future<?>[]> futuresRef = new AtomicReference<>();

    public EmbedCacheRefresh(RefreshConfig config, ScheduledExecutorService scheduler, ExecutorService executor) {
        this.name = config.getName();
        this.period = config.getPeriod();
        this.stopAfterAccess = config.getStopAfterAccess();
        this.scheduler = scheduler;
        this.executor = executor;
    }

    @Override
    public void access(String key) {
        accessed.put(key, getTtl());
    }

    @Override
    public void accessAll(Set<String> keys) {
        long ttl = getTtl();
        keys.forEach(key -> accessed.put(key, ttl));
    }

    private long getTtl() {
        long ttl = System.currentTimeMillis() + stopAfterAccess;
        if (ttl > 0) {
            return ttl;
        }
        return Long.MAX_VALUE;
    }

    @Override
    public void remove(String key) {
        accessed.remove(key);
    }

    @Override
    public void removeAll(Set<String> keys) {
        keys.forEach(accessed::remove);
    }

    @Override
    public void setConsumer(Consumer<String> consumer) {
        this.consumer = consumer;
        this.scheduler.scheduleWithFixedDelay(this::syncDataSource, period, period, TimeUnit.MILLISECONDS);
    }

    /**
     * 是否要加锁执行嵌入式的缓存刷新任务？ <p>
     * 采用嵌入式的缓存刷新，应该是只有嵌入式缓存，各应用实例均需独立执行数据刷新任务。 <p>
     * 1. 采用本地锁，无法阻止其它实例同时执行数据刷新任务。 <p>
     * 2. 采用分布式锁，能阻止其它应用实例同时刷新，但需执行 Futures.awaitAll(tasks)，
     * 等待所有任务完成，scheduler 的线程会持续被占用，影响同一应用中其它缓存的数据刷新任务，
     * 严重的情况下可能会导致任务堆积，出现 oom 异常。
     * 更大的问题是，无论是否会执行 Futures.awaitAll(tasks)，
     * 都可能会导致其它实例因为无法获取锁而跳过某次数据刷新任务，从而导致数据过期。
     * <p>
     * 因此，不加锁执行数据刷新任务
     */
    private void syncDataSource() {
        try {
            // 1. 执行新任务之前，先判断上次的任务队列是否已经执行完毕
            // 如果未完成则退出当前任务（数据源压力过大，再执行刷新任务只会堵塞更严重，甚至可能会导致进程崩溃）
            if (!this.checkRefreshTasks(futuresRef, period)) {
                log.error("Cache:[{}], Timed out waiting for refreshTasks to complete", name);
                return;
            }

            // 2. 循环判断每个键是否需要执行数据刷新任务
            List<Future<?>> tasks = new ArrayList<>(accessed.size());

            long now = System.currentTimeMillis();
            for (Map.Entry<String, Long> entry : accessed.entrySet()) {
                if (entry == null) continue;

                // 2.1. 删除超过最大访问时限的键
                if (now > entry.getValue()) {
                    accessed.remove(entry.getKey());
                    continue;
                }
                // 2.2. 回源执行缓存数据刷新任务
                tasks.add(this.executor.submit(new RefreshTask(entry.getKey(), this.consumer)));
            }

            // 3. 保存任务队列，
            this.futuresRef.set(tasks.toArray(new Future<?>[0]));
        } catch (Throwable e) {
            log.error("CacheRefresh has error. {}", e.getMessage());
        }
    }

}