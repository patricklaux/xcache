package com.igeeksky.xcache.redis.lock;


import com.igeeksky.xredis.common.RedisScript;
import com.igeeksky.xredis.common.ResultType;

/**
 * Redis 锁脚本
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
     * KEYS[1] 键 <p>
     * ARGV[1] sid <p>
     * ARGV[2] 存续时间
     * <p>
     * {@code @return Long - (null：加锁成功；Long：加锁失败，返回存续时间) }
     */
    public static final RedisScript LOCK_SCRIPT = new RedisScript(
            "if (redis.call('exists', KEYS[1]) == 0) then " +
                    "    redis.call('hincrby', KEYS[1], ARGV[1], 1); " +
                    "    redis.call('pexpire', KEYS[1], ARGV[2]); " +
                    "    return nil; " +
                    "end; " +
                    "if (redis.call('hexists', KEYS[1], ARGV[1]) == 1) then " +
                    "    redis.call('hincrby', KEYS[1], ARGV[1], 1); " +
                    "    redis.call('pexpire', KEYS[1], ARGV[2]); " +
                    "    return nil; " +
                    "end; " +
                    "return redis.call('pttl', KEYS[1]);"
            , ResultType.INTEGER);

    /**
     * 解锁
     * <p>
     * KEYS[1] 键 <p>
     * ARGV[1] sid <p>
     * ARGV[2] 存续时间
     * <p>
     * {@code @return Boolean - (null：锁不存在；false：解锁失败；true：解锁成功) }
     */
    public static final RedisScript UNLOCK_SCRIPT = new RedisScript(
            "if (redis.call('hexists', KEYS[1], ARGV[1]) == 0) then " +
                    "return nil;" +
                    "end; " +
                    "local counter = redis.call('hincrby', KEYS[1], ARGV[1], -1); " +
                    "if (counter > 0) then " +
                    "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                    "return 0; " +
                    "else " +
                    "redis.call('del', KEYS[1]); " +
                    "return 1; " +
                    "end; "
            , ResultType.BOOLEAN);

    /**
     * 续期
     * <p>
     * KEYS[1] 键 <p>
     * ARGV[1] sid <p>
     * ARGV[2] 存续时间
     * <p>
     * {@code @return boolean - (true：续期成功； false：续期失败) }
     */
    public static final RedisScript NEW_EXPIRE_SCRIPT = new RedisScript(
            "if (redis.call('hexists', KEYS[1], ARGV[1]) == 1) then " +
                    "redis.call('pexpire', KEYS[1], ARGV[2]); " +
                    "return 1; " +
                    "end; " +
                    "return 0;"
            , ResultType.BOOLEAN);

}