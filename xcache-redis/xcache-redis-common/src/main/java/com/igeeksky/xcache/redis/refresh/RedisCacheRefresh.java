package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.extension.lock.KeyLock;
import com.igeeksky.xcache.extension.lock.LockService;
import com.igeeksky.xcache.extension.refresh.CacheRefresh;
import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xcache.extension.refresh.RefreshTask;
import com.igeeksky.xtool.core.collection.ConcurrentHashSet;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/29
 */
public class RedisCacheRefresh implements CacheRefresh {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheRefresh.class);

    private final RedisOperator connection;

    private final String name;
    private final long period;
    private final long stopAfterAccess;
    private final byte[] refreshKey;
    private final byte[] refreshPeriodKey;
    private final String refreshLockKey;

    private final LockService cacheLock;
    private Consumer<String> consumer;

    private final StringCodec stringCodec;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    private final AtomicReference<Set<String>> activeDeleted = new AtomicReference<>(new ConcurrentHashSet<>());
    private final AtomicReference<Set<String>> idleDeleted = new AtomicReference<>(new ConcurrentHashSet<>());

    private final AtomicReference<Map<String, Long>> activeAccessed = new AtomicReference<>(new ConcurrentHashMap<>(128));
    private final AtomicReference<Map<String, Long>> idleAccessed = new AtomicReference<>(new ConcurrentHashMap<>(128));

    private final AtomicReference<Future<?>[]> futuresRef = new AtomicReference<>();

    public RedisCacheRefresh(RefreshConfig config, ScheduledExecutorService scheduler, ExecutorService executor, RedisOperator connection) {
        this.name = config.getName();
        this.stringCodec = StringCodec.getInstance(config.getCharset());
        this.period = config.getPeriod();
        this.cacheLock = config.getCacheLock();
        this.refreshKey = stringCodec.encode(config.getRefreshKey());
        this.refreshLockKey = config.getRefreshLockKey();
        this.refreshPeriodKey = stringCodec.encode(config.getRefreshPeriodKey());
        this.stopAfterAccess = config.getStopAfterAccess();
        this.executor = executor;
        this.scheduler = scheduler;
        this.connection = connection;
    }

    @Override
    public void access(String key) {
        this.activeAccessed.get().put(key, getTtl());
    }

    @Override
    public void accessAll(Set<String> keys) {
        long ttl = getTtl();
        Map<String, Long> accessed = this.activeAccessed.get();
        keys.forEach(key -> accessed.put(key, ttl));
    }

    private long getTtl() {
        long ttl = System.currentTimeMillis() + this.stopAfterAccess;
        if (ttl > 0) {
            return ttl;
        }
        return Long.MAX_VALUE;
    }

    @Override
    public void remove(String key) {
        this.activeDeleted.get().add(key);
        this.activeAccessed.get().remove(key);
    }

    @Override
    public void removeAll(Set<String> keys) {
        Set<String> deleted = this.activeDeleted.get();
        Map<String, Long> accessed = this.activeAccessed.get();
        for (String key : keys) {
            deleted.add(key);
            accessed.remove(key);
        }
    }

    @Override
    public void setConsumer(Consumer<String> consumer) {
        this.consumer = consumer;

        // 1. 每秒执行一次数据同步（本地数据 ——> Redis）
        this.scheduler.scheduleWithFixedDelay(this::syncAccessRecord, 1000, 1000, TimeUnit.MILLISECONDS);

        // 2. 获取 Redis 中的所有键集，回源刷新缓存数据
        this.scheduler.scheduleWithFixedDelay(this::syncDataSource, period, period, TimeUnit.MILLISECONDS);
    }

    private void syncAccessRecord() {
        try {
            // 1. 同步删除记录
            Set<String> deleted = this.idleDeleted.get();
            if (!deleted.isEmpty()) {
                int size = deleted.size(), i = 0;
                byte[][] keys = new byte[size][];
                for (String key : deleted) {
                    keys[i++] = this.stringCodec.encode(key);
                }
                this.connection.hdel(this.refreshKey, keys);
                deleted.clear();
            }
            this.idleDeleted.set(this.activeDeleted.getAndSet(deleted));

            // 2. 同步访问记录
            Map<String, Long> accessed = this.idleAccessed.get();
            if (!accessed.isEmpty()) {
                Map<byte[], byte[]> keysTimes = Maps.newHashMap(accessed.size());
                accessed.forEach((k, v) -> {
                    byte[] key = this.stringCodec.encode(k);
                    byte[] time = this.stringCodec.encode(String.valueOf(v));
                    keysTimes.put(key, time);
                });
                this.connection.hmset(refreshKey, keysTimes);
                accessed.clear();
            }
            this.idleAccessed.set(activeAccessed.getAndSet(accessed));
        } catch (Throwable e) {
            log.error("Cache:[{}] syncAccessRecord has error. {}", name, e.getMessage(), e);
        }
    }


    /**
     * <b>同步数据源</b> <p>
     * 1. 从 Redis 获取全部访问记录 <p>
     * 2. 删除超过最大访问时限的记录 <p>
     * 3. 回源查询数据并存入缓存 (consumer) <p>
     * --------------------- <p>
     * <b>是否要加锁执行缓存刷新任务？</b> <p>
     * 1. 如果是分布式缓存，则仅需一个应用实例执行数据刷新即可。<p>
     * 1.1. 使用 CacheLock.tryLock(refreshLockKey) 方法来判断是否有其它实例正在执行数据刷新任务；<p>
     * 1.2. 通过保存刷新时间，当获取锁之后，先判断是否到达刷新时间间隔，再决定是否执行数据刷新任务，避免重复刷新。<p>
     * 2. 如果是本地缓存（每个应用实例均有私有访问的本机 redis），则每个应用实例均需独立刷新缓存，是否加锁没有区别。
     * <p>
     * 因此，加锁、加上时间限定来执行数据刷新任务。 <p>
     */
    private void syncDataSource() {
        try {
            // 1. 执行新任务之前，先判断上次的任务队列是否已经执行完毕
            // 如果未完成则退出当前任务（避免数据源压力过大）
            if (!checkRefreshTasks(futuresRef, period)) {
                log.error("Cache:[{}], Timed out waiting for refreshTasks to complete", name);
                return;
            }

            KeyLock lock = cacheLock.acquire(this.refreshLockKey);
            if (lock.tryLock()) {
                try {
                    // 2. 如未到达刷新周期，结束此次任务
                    if (!isTimeToRefresh()) return;

                    // 3. 从 Redis 获取全部访问记录
                    Map<byte[], byte[]> hgetall = connection.hgetall(refreshKey);
                    if (Maps.isEmpty(hgetall)) {
                        return;
                    }

                    // 保存超过最大访问时限的键集
                    Set<byte[]> deleted = new HashSet<>();

                    // 保存提交的数据刷新任务
                    List<Future<?>> tasks = new ArrayList<>(hgetall.size());

                    // 4. 提交回源查询任务
                    long now = System.currentTimeMillis();
                    for (Map.Entry<byte[], byte[]> entry : hgetall.entrySet()) {
                        long ttl = Long.parseLong(stringCodec.decode(entry.getValue()));
                        if (now > ttl) {
                            deleted.add(entry.getKey());
                            continue;
                        }
                        RefreshTask task = new RefreshTask(stringCodec.decode(entry.getKey()), this.consumer);
                        tasks.add(this.executor.submit(task));
                    }

                    futuresRef.set(tasks.toArray(new Future[0]));

                    // 5. 删除已超过最大访问时限的键集
                    if (!deleted.isEmpty()) {
                        connection.hdel(refreshKey, deleted.toArray(new byte[deleted.size()][]));
                    }

                    // 6. 更新下次数据刷新时间，避免其它应用实例重复执行
                    updateRefreshTime();
                } finally {
                    lock.unlock();
                }
            }
        } catch (Throwable e) {
            log.error("Cache:[{}] syncDataSource has error. {}", name, e.getMessage());
        }
    }

    /**
     * 判断是否已到达数据刷新的计划执行时间
     *
     * @return true 已到计划时间，开始执行数据刷新；false 未到计划时间，取消当次数据刷新
     */
    private boolean isTimeToRefresh() {
        byte[] refreshTime = connection.get(refreshPeriodKey);
        if (refreshTime != null) {
            // 如果当前时间小于计划同步时间，说明已经有其它实例已经先执行数据同步，因此取消同步任务
            return (System.currentTimeMillis() >= Long.parseLong(stringCodec.decode(refreshTime)));
        }
        return true;
    }

    private void updateRefreshTime() {
        long nextTime = System.currentTimeMillis() + period;
        connection.set(refreshPeriodKey, stringCodec.encode(String.valueOf(nextTime)));
    }

}