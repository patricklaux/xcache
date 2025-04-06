package com.igeeksky.xcache.redis.lock;


import com.igeeksky.xcache.extension.lock.CacheLockProvider;
import com.igeeksky.xcache.extension.lock.LockConfig;
import com.igeeksky.xredis.common.RedisOperatorProxy;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Redis 锁工厂
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/16
 */
public class RedisLockProvider implements CacheLockProvider {

    private final RedisOperatorProxy operator;
    private final ScheduledExecutorService scheduler;

    public RedisLockProvider(RedisOperatorProxy operator, ScheduledExecutorService scheduler) {
        this.operator = operator;
        this.scheduler = scheduler;
    }

    @Override
    public RedisLockService get(LockConfig config) {
        if (operator.isCompatible()) {
            throw new UnsupportedOperationException("RedisLock doesn't support Redis-Compatible DB.");
        }
        return new RedisLockService(config, operator, scheduler);
    }

}