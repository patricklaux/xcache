package com.igeeksky.xcache.redis.lock;

import com.igeeksky.xredis.common.RedisFutureHelper;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Redis 锁实现
 * <p>
 * Standalone 或 Cluster，均可使用。
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class RedisSpinLock implements Lock {

    private static final Logger log = LoggerFactory.getLogger(RedisSpinLock.class);

    /**
     * <b>内部加锁解锁顺序</b>：<p>
     * 1. innerLock lock <p>
     * 2. redisLock lock <p>
     * 3. redisLock unlock <p>
     * 4. innerLock unlock <p>
     * <b>用途</b>：<p>
     * 1. 避免无效的网络交互：应用实例内部竞争获取 innerLock 成功后再执行 Redis 加锁脚本，避免多个线程同时与 Redis 交互；<p>
     * 2. 避免 CPU 空闲等待：JDK 21+，如使用的是虚拟线程，当 JVM 侦测到 ReentrantLock 正处于竞争锁阶段，会自动切换线程。
     */
    private final ReentrantLock innerLock = new ReentrantLock();

    /**
     * 申请锁的次数（当计数为 0 时从 map 中删除锁）
     */
    private final AtomicInteger count = new AtomicInteger(1);

    private final RedisOperatorProxy operator;
    private final ScheduledExecutorService scheduler;
    private volatile ScheduledFuture<?> scheduledFuture;

    private final long leaseTime;
    private final long batchTimeout;

    private final byte[][] keys = new byte[1][];
    private final byte[][] args = new byte[2][];

    /**
     * @param key       锁对应的键
     * @param sid       实例标识（UUID），可以在 Redis 中区分不同实例
     * @param leaseTime 锁存续时间（大于 0）
     * @param codec     字符串编解码器
     * @param operator  Redis连接
     * @param scheduler 定时调度器，用于在加锁执行期间，定期运行 watchdog，延长锁的存续时间
     */
    public RedisSpinLock(String key, String sid, long leaseTime, long batchTimeout,
                         StringCodec codec, RedisOperatorProxy operator,
                         ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
        this.leaseTime = leaseTime;
        this.batchTimeout = batchTimeout;
        this.operator = operator;
        this.keys[0] = codec.encode(key);
        this.args[0] = codec.encode(sid);
        this.args[1] = codec.encode(Long.toString(leaseTime));
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
            ttl = this.tryAcquire();
            if (ttl == null) {
                return;
            }

            SleepPolicy policy = new SleepPolicy(System.currentTimeMillis() - start);
            while (ttl != null) {
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(policy.getNext(ttl)));
                ttl = this.tryAcquire();
            }
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
                return (ttl = this.tryAcquire()) == null;
            } catch (Throwable e) {
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
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        if (innerLock.tryLock(timeout, unit)) {
            boolean success = false;
            try {
                return (success = this.tryLock(unit.toMillis(timeout)));
            } finally {
                if (!success) {
                    innerLock.unlock();
                }
            }
        }
        return false;
    }

    private boolean tryLock(long waitTime) {
        long start = System.currentTimeMillis();
        Long ttl = this.tryAcquire();
        if (ttl == null) {
            return true;
        }

        long usedTime = System.currentTimeMillis() - start;
        if (usedTime >= waitTime) {
            return false;
        }

        SleepPolicy policy = new SleepPolicy(waitTime, usedTime);

        while (true) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(policy.getNext(ttl)));
            if ((ttl = this.tryAcquire()) == null) {
                return true;
            }
            if ((System.currentTimeMillis() - start) >= waitTime) {
                return false;
            }
        }
    }

    private Long tryAcquire() {
        CompletableFuture<Long> future = this.operator.evalsha(RedisLockScript.LOCK_SCRIPT, this.keys, this.args);

        Long result = RedisFutureHelper.get(future, batchTimeout);
        if (result == null && this.scheduledFuture == null) {
            // 如果加锁成功，且没有启动续期任务，则启动一个定时任务，定时续期
            long period = Math.max(1, leaseTime / 3);
            this.scheduledFuture = this.scheduler.scheduleAtFixedRate(new ExtendExpirationTask(keys, args, operator),
                    period, period, TimeUnit.MILLISECONDS
            );
        }
        return result;
    }

    @Override
    public void unlock() {
        try {
            CompletableFuture<Boolean> future = this.operator.evalsha(RedisLockScript.UNLOCK_SCRIPT,
                    this.keys, this.args);

            Boolean unlocked = RedisFutureHelper.get(future, batchTimeout);
            if (unlocked == null || unlocked) {
                this.stopExpirationTask();
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Unlocking failed: reference count greater than 0.");
            }
        } catch (Throwable e) {
            // 出现异常，则不再续期，避免重入计数永不为 0，导致一直续期
            this.stopExpirationTask();
            log.error("Unlocking failed: possibly due to redis or network issues.", e);
        } finally {
            innerLock.unlock();
        }
    }

    /**
     * 停止续期任务
     */
    protected void stopExpirationTask() {
        Future<?> future = this.scheduledFuture;
        if (future != null) {
            future.cancel(false);
            this.scheduledFuture = null;
        }
    }

    /**
     * 分布式锁，不支持此操作
     *
     * @return throw {@link UnsupportedOperationException}
     */
    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("RedisSpinLock unsupported this operation.");
    }

    /**
     * 计划任务：用于加锁期间自动续期
     */
    private record ExtendExpirationTask(byte[][] keys, byte[][] args, RedisOperatorProxy operator)
            implements Runnable {

        @Override
        public void run() {
            try {
                this.operator.evalsha(RedisLockScript.NEW_EXPIRE_SCRIPT, this.keys, this.args)
                        .whenComplete((result, t) -> {
                            if (t != null) {
                                log.error("extend expiration failed: {}", t.getMessage(), t);
                                return;
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("extend expiration task result: {}", result);
                            }
                        });
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }

    }

}