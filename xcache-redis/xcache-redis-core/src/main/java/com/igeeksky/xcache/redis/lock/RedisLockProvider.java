package com.igeeksky.xcache.redis.lock;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.redis.RedisOperatorFactory;
import com.igeeksky.xcache.extension.lock.CacheLockProvider;
import com.igeeksky.xcache.extension.lock.LockConfig;
import com.igeeksky.xtool.core.concurrent.VirtualThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Redis 锁的工厂类
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/16
 */
public class RedisLockProvider implements CacheLockProvider {

    /**
     * 锁对象，用于锁住加载脚本的线程
     */
    private final Lock lock = new ReentrantLock();

    /**
     * 脚本是否加载完成
     */
    private volatile boolean loadedScript = false;

    private final ScheduledExecutorService scheduler;
    private final ExecutorService executor;
    private final RedisOperator operator;

    public RedisLockProvider(ScheduledExecutorService scheduler, RedisOperatorFactory factory) {
        this.scheduler = scheduler;
        this.executor = executor();
        this.operator = factory.getRedisOperator();
    }

    @Override
    public RedisLockService get(LockConfig config) {
        if (!loadedScript) {
            lock.lock();
            try {
                if (!loadedScript) {
                    operator.scriptLoad(RedisLockScript.LOCK_SCRIPT);
                    operator.scriptLoad(RedisLockScript.UNLOCK_SCRIPT);
                    operator.scriptLoad(RedisLockScript.NEW_EXPIRE_SCRIPT);
                    loadedScript = true;
                }
            } finally {
                lock.unlock();
            }
        }

        return new RedisLockService(scheduler, executor, config, operator);
    }

    private static ExecutorService executor() {
        return Executors.newThreadPerTaskExecutor(new VirtualThreadFactory("redis-lock-thread-"));
    }

}