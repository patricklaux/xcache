package com.igeeksky.xcache.redis.lock;

import com.igeeksky.redis.lettuce.LettuceStandaloneFactory;
import com.igeeksky.redis.lettuce.LettuceTestHelper;
import com.igeeksky.xcache.extension.lock.LockConfig;
import com.igeeksky.xcache.extension.lock.LockTestTask;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/27
 */
class RedisSpinLockTest {

    private static final String key = "test-lock";
    private static LettuceStandaloneFactory lettuceStandaloneFactory;
    private static RedisLockService lockService;

    @BeforeAll
    static void beforeAll() {
        LockConfig config = LockConfig.builder()
                .sid(UUID.randomUUID().toString())
                .name("user")
                .provider("redis")
                .initialCapacity(64)
                .group("shop")
                .enableGroupPrefix(true)
                .leaseTime(10000)
                .build();

        lettuceStandaloneFactory = LettuceTestHelper.createStandaloneFactory();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        RedisLockProvider provider = new RedisLockProvider(scheduler, lettuceStandaloneFactory);

        lockService = provider.get(config);
    }

    @AfterAll
    static void afterAll() {
        lettuceStandaloneFactory.close();
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