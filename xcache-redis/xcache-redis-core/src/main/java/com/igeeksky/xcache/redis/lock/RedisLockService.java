package com.igeeksky.xcache.redis.lock;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.extension.lock.LockConfig;
import com.igeeksky.xcache.extension.lock.LockService;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/12
 */
public class RedisLockService implements LockService {

    private final Map<String, RedisSpinLock> locks;

    private final String sid;
    private final String prefix;
    private final long leaseTime;

    private final StringCodec codec;

    private final RedisOperator operator;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    public RedisLockService(ScheduledExecutorService scheduler, ExecutorService executor,
                            LockConfig config, RedisOperator operator) {
        this.operator = operator;
        this.executor = executor;
        this.scheduler = scheduler;

        this.codec = StringCodec.getInstance(config.getCharset());
        this.sid = config.getSid();
        this.prefix = config.getPrefix();
        this.leaseTime = config.getLeaseTime();
        this.locks = Maps.newConcurrentHashMap(config.getInitialCapacity());
    }

    @Override
    public RedisSpinLock acquire(String key) {
        return locks.compute(key, (k, lock) -> {
            if (lock == null) {
                // 锁不存在，创建并返回新锁。
                return new RedisSpinLock(prefix + key, sid, leaseTime, codec, operator, scheduler, executor);
            }
            // 锁已存在，增加其使用计数并返回该锁。
            lock.increment();
            return lock;
        });
    }

    @Override
    public void release(String key) {
        locks.computeIfPresent(key, (k, lock) -> {
            if (lock.decrementAndGet() <= 0) {
                lock.stopExpirationTask();
                return null;
            }
            return lock;
        });
    }

    public int size() {
        return locks.size();
    }

}