package com.igeeksky.xcache.redis.lock;


import com.igeeksky.xcache.extension.lock.LockConfig;
import com.igeeksky.xcache.extension.lock.LockTestTask;
import com.igeeksky.xcache.redis.LettuceTestHelper;
import com.igeeksky.xredis.lettuce.LettuceOperatorProxy;
import com.igeeksky.xredis.lettuce.api.RedisOperator;
import com.igeeksky.xredis.lettuce.api.RedisOperatorFactory;
import io.lettuce.core.codec.ByteArrayCodec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * RedisSpinLock 测试
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/27
 */
class RedisSpinLockTest {

    private static final String key = "test-lock";
    private static RedisLockService lockService;
    private static RedisOperatorFactory redisOperatorFactory;

    @BeforeAll
    static void beforeAll() {
        LockConfig config = LockConfig.builder()
                .sid(UUID.randomUUID().toString())
                .name("user")
                .group("shop")
                .charset(StandardCharsets.UTF_8)
                .enableGroupPrefix(true)
                .provider("redis")
                .initialCapacity(64)
                .leaseTime(10000)
                .build();

        redisOperatorFactory = LettuceTestHelper.createStandaloneFactory();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        RedisOperator<byte[], byte[]> redisOperator = redisOperatorFactory.redisOperator(ByteArrayCodec.INSTANCE);
        LettuceOperatorProxy operatorProxy = new LettuceOperatorProxy(10000, 60000, redisOperator);
        RedisLockProvider provider = new RedisLockProvider(operatorProxy, scheduler);
        lockService = provider.get(config);
    }

    @AfterAll
    static void afterAll() {
        redisOperatorFactory.shutdown();
    }

    @Test
    void lock() {
        RedisSpinLock lock = lockService.acquire(key);
        try {
            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + ": Lock acquired");
            } finally {
                lock.unlock();
            }
        } finally {
            lockService.release(key);
        }

        Assertions.assertEquals(0, lockService.size());
    }

    @Test
    void lockInterruptibly() throws InterruptedException {
        LockTestTask task = new LockTestTask(lockService, key);

        for (int i = 0; i < 10; i++) {
            new Thread(task).start();
        }

        Thread.sleep(2000);

        Assertions.assertEquals(0, lockService.size());
    }

    @Test
    void testLockInterruptibly() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new LockTestTask(lockService, null));
            thread.start();
        }

        Thread.sleep(2000);

        Assertions.assertEquals(0, lockService.size());
    }

    @Test
    void tryLock() throws InterruptedException {
        RedisSpinLock lock = lockService.acquire(key);
        try {
            boolean success = lock.tryLock();
            if (success) {
                try {
                    new Thread(() -> {
                        RedisSpinLock lock2 = lockService.acquire(key);
                        boolean lock2Tried = lock2.tryLock();
                        Assertions.assertFalse(lock2Tried);
                        lockService.release(key);
                    }).start();
                    System.out.println(Thread.currentThread().getName() + ": Lock acquired1");
                    Thread.sleep(1000);
                    System.out.println(Thread.currentThread().getName() + ": Lock acquired2");
                } finally {
                    lock.unlock();
                }
            }
        } finally {
            lockService.release(key);
        }

        Assertions.assertEquals(0, lockService.size());
    }

    @Test
    void testTryLock() throws InterruptedException {
        RedisSpinLock lock = lockService.acquire(key);
        try {
            boolean success = lock.tryLock(1000, TimeUnit.MILLISECONDS);
            if (success) {
                try {
                    new Thread(() -> {
                        try {
                            RedisSpinLock lock2 = lockService.acquire(key);
                            boolean lock2Tried = lock2.tryLock(1000, TimeUnit.MILLISECONDS);
                            Assertions.assertTrue(lock2Tried);
                            lock2.unlock();
                            lockService.release(key);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                } finally {
                    lock.unlock();
                }
            }
        } finally {
            lockService.release(key);
        }

        Thread.sleep(2000);
    }

    @Test
    void newCondition() {
        Exception ex = null;
        try {
            Condition condition = lockService.acquire(key).newCondition();
            condition.signal();
        } catch (Exception e) {
            ex = e;
        } finally {
            lockService.release(key);
        }

        Assertions.assertInstanceOf(UnsupportedOperationException.class, ex);
        Assertions.assertEquals(0, lockService.size());
    }

}