package com.igeeksky.xcache.redis.lock;

import com.igeeksky.redis.RedisScript;
import com.igeeksky.redis.ResultType;

/**
 * Redis 分布式锁脚本
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/17
 */
public final class RedisLockScript {

    /**
     * 私有构造方法，禁止实例化
     */
    private RedisLockScript() {
    }

    /**
     * 加锁
     * <p>
     * KEYS[1] 锁对应的键 <p>
     * ARGV[1] 锁存续时间 <p>
     * ARGV[2] sid
     */
    public static final RedisScript<Long> LOCK_SCRIPT = new RedisScript<>(
            "if (redis.call('exists', KEYS[1]) == 0) then " +
                    "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                    "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                    "return nil; " +
                    "end; " +
                    "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                    "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                    "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                    "return nil; " +
                    "end; " +
                    "return redis.call('pttl', KEYS[1]);"
            , ResultType.INTEGER);

    /**
     * 释放锁
     * <p>
     * KEYS[1] 锁对应的键 <p>
     * ARGV[1] 锁存续时间 <p>
     * ARGV[2] sid
     */
    public static final RedisScript<Boolean> UNLOCK_SCRIPT = new RedisScript<>(
            "if (redis.call('hexists', KEYS[1], ARGV[2]) == 0) then " +
                    "return nil;" +
                    "end; " +
                    "local counter = redis.call('hincrby', KEYS[1], ARGV[2], -1); " +
                    "if (counter > 0) then " +
                    "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                    "return 0; " +
                    "else " +
                    "redis.call('del', KEYS[1]); " +
                    "return 1; " +
                    "end; " +
                    "return nil;"
            , ResultType.BOOLEAN);

    /**
     * 锁续期
     * <p>
     * KEYS[1] 锁对应的键 <p>
     * ARGV[1] 锁存续时间 <p>
     * ARGV[2] sid
     */
    public static final RedisScript<Boolean> NEW_EXPIRE = new RedisScript<>(
            "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                    "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                    "return 1; " +
                    "end; " +
                    "return 0;"
            , ResultType.BOOLEAN);

}