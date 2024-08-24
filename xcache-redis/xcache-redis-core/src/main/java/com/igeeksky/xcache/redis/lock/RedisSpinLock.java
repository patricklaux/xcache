package com.igeeksky.xcache.redis.lock;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.redis.RedisScript;
import com.igeeksky.xcache.extension.lock.KeyLock;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RedisSpinLock extends KeyLock {

    private static final Logger log = LoggerFactory.getLogger(RedisSpinLock.class);

    /**
     * 加锁
     * <p>
     * KEYS[1] 锁对应的键 <p>
     * ARGV[1] 锁存续时间 <p>
     * ARGV[2] sid + ":" + 线程 id
     */
    private static final RedisScript<Long> LOCK_SCRIPT = RedisLockScript.LOCK_SCRIPT;

    /**
     * 释放锁
     * <p>
     * KEYS[1] 锁对应的键 <p>
     * ARGV[1] 锁存续时间 <p>
     * ARGV[2] sid + ":" + 线程 id
     */
    private static final RedisScript<Boolean> UNLOCK_SCRIPT = RedisLockScript.UNLOCK_SCRIPT;

    /**
     * 锁续期
     * <p>
     * KEYS[1] 锁对应的键 <p>
     * ARGV[1] 锁存续时间 <p>
     * ARGV[2] sid + ":" + 线程 id
     */
    private static final RedisScript<Boolean> NEW_EXPIRE = RedisLockScript.NEW_EXPIRE;

    /**
     * <b>内部加锁解锁顺序</b>：<p>
     * 1. innerLock lock <p>
     * 2. redisLock lock <p>
     * 3. redisLock unlock <p>
     * 4. innerLock unlock <p>
     * <b>用途</b>：<p>
     * 1. 避免无效的网络交互：应用实例内部竞争获取 innerLock 成功后再执行 Redis 加锁脚本，避免多个线程同时与 Redis 交互；<p>
     * 2. 避免 CPU 空闲等待：当 JDK 侦测到应用内部的 ReentrantLock 正处于竞争锁阶段，会切换虚拟线程。
     */
    private final ReentrantLock innerLock = new ReentrantLock();

    /**
     * 申请锁的次数（当计数为 0 时从 map 中删除锁）
     */
    private final AtomicInteger count = new AtomicInteger(1);

    private final ScheduledExecutorService scheduler;

    private final ExecutorService executor;

    private final RedisOperator operator;

    private final long leaseTime;

    private final byte[][] args = new byte[3][];

    private volatile ScheduledFuture<?> future;

    /**
     * @param key       锁对应的键
     * @param sid       实例标识（UUID），可以在 Redis 中区分不同实例
     * @param leaseTime 锁存续时间（大于 0）
     * @param scheduler 续期执行器，用于在加锁执行期间，定期运行 watchdog，延长锁的存续时间
     * @param operator  Redis连接
     */
    public RedisSpinLock(String key, String sid, long leaseTime, StringCodec codec, RedisOperator operator,
                         ScheduledExecutorService scheduler, ExecutorService executor) {
        super(key);
        this.executor = executor;
        this.scheduler = scheduler;
        this.leaseTime = leaseTime;
        this.operator = operator;
        this.args[0] = codec.encode(key);
        this.args[1] = codec.encode(Long.toString(leaseTime));
        this.args[2] = codec.encode(sid);
    }

    protected int decrementAndGet() {
        return count.decrementAndGet();
    }

    protected void increment() {
        count.incrementAndGet();
    }

    @Override
    public void lock() {
        try {
            lockInterruptibly();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        Long ttl = null;
        innerLock.lockInterruptibly();
        try {
            long start = System.currentTimeMillis();
            ttl = tryAcquire();
            if (ttl == null) {
                return;
            }

            SleepPolicy policy = new SleepPolicy(System.currentTimeMillis() - start);
            while (ttl != null) {
                Thread.sleep(policy.getNext(ttl));
                ttl = tryAcquire();
            }
        } catch (Throwable e) {
            innerLock.unlock();
            throw e;
        } finally {
            if (ttl != null) {
                innerLock.unlock();
            }
        }
    }

    @Override
    public boolean tryLock() {
        boolean success = innerLock.tryLock();
        if (success) {
            Long ttl = null;
            try {
                return (ttl = tryAcquire()) == null;
            } catch (Throwable e) {
                innerLock.unlock();
                return false;
            } finally {
                if (ttl != null) {
                    innerLock.unlock();
                }
            }
        }
        return false;
    }

    @Override
    public boolean tryLock(long waitTime, TimeUnit unit) throws InterruptedException {
        boolean success = innerLock.tryLock(waitTime, unit);
        if (success) {
            try {
                success = tryLock(unit.toMillis(waitTime));
            } catch (Throwable e) {
                innerLock.unlock();
                throw e;
            } finally {
                if (!success) {
                    innerLock.unlock();
                }
            }
        }
        return success;
    }

    private boolean tryLock(long waitTime) throws InterruptedException {
        long start = System.currentTimeMillis();
        Long ttl = tryAcquire();
        if (ttl == null) {
            return true;
        }

        long usedTime = System.currentTimeMillis() - start;
        if (usedTime >= waitTime) {
            return false;
        }

        SleepPolicy policy = new SleepPolicy(waitTime, usedTime);

        while (true) {
            Thread.sleep(policy.getNext(ttl));
            ttl = tryAcquire();
            if (ttl == null) {
                return true;
            }
            usedTime = System.currentTimeMillis() - start;
            if (usedTime >= waitTime) {
                return false;
            }
        }
    }

    private Long tryAcquire() {
        Long result = this.operator.evalsha(LOCK_SCRIPT, 1, this.args);
        if (result == null && this.future == null) {
            // 如果加锁成功，且没有启动续期任务，则启动一个定时任务，定时续期
            long period = leaseTime / 3;
            this.future = this.scheduler.scheduleAtFixedRate(this::execute, period, period, TimeUnit.MILLISECONDS);
        }
        return result;
    }

    @Override
    public void unlock() {
        try {
            Boolean result = this.operator.evalsha(UNLOCK_SCRIPT, 1, this.args);
            // 1. 如果结果为 null，说明 Redis 原来无锁对应的键，释放锁成功
            // 2. 如果结果为 true，说明 Redis 原来有锁对应的键，但锁重入计数已为 0，此次操作已删除键，释放锁成功
            if (result == null || result) {
                this.stopExpirationTask();
            }
        } catch (Throwable e) {
            // 一旦出现异常，则不再续期，避免重入计数永不为 0，导致一直续期
            this.stopExpirationTask();
            log.error("key:[{}] Unlocking failed, possibly due to redis or network issues.", this.getKey(), e);
        } finally {
            innerLock.unlock();
        }
    }

    /**
     * 停止续期任务
     */
    protected void stopExpirationTask() {
        if (this.future != null) {
            this.future.cancel(false);
            this.future = null;
        }
    }

    /**
     * 分布式锁，不支持此操作
     *
     * @return throw {@link UnsupportedOperationException}
     */
    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

    private void execute() {
        try {
            this.executor.submit(new ExtendExpirationTask(args, operator));
        } catch (Throwable e) {
            log.error("RedisSpinLock: execute task has failed: {}", e.getMessage(), e);
        }
    }

    /**
     * 计划任务：用于加锁期间自动续期
     */
    private record ExtendExpirationTask(byte[][] args, RedisOperator operator) implements Runnable {

        @Override
        public void run() {
            try {
                Boolean success = this.operator.evalsha(NEW_EXPIRE, 1, this.args);
                if (log.isDebugEnabled()) {
                    log.debug("extend expiration task result: {}", success);
                }
            } catch (Throwable e) {
                log.error("extend expiration task has failed: {}", e.getMessage(), e);
            }
        }

    }

}