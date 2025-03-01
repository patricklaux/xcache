package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.xredis.common.RedisScript;
import com.igeeksky.xredis.common.ResultType;

/**
 * Redis 数据刷新脚本
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/17
 */
public final class RedisRefreshScript {

    /**
     * 私有构造方法，禁止实例化
     */
    private RedisRefreshScript() {
    }

    /**
     * 加锁
     * <p>
     * KEYS[1] 键 <p>
     * ARGV[1] sid <p>
     * ARGV[2] 锁存续时间 单位：毫秒
     * <p>
     * {@code Returns:} 加锁成功返回 true，否则返回 false。
     */
    public static final RedisScript LOCK = new RedisScript(
            "local sid = redis.call('GET', KEYS[1]); " +
                    "if not sid or sid == ARGV[1] then " +
                    "    redis.call('PSETEX', KEYS[1], ARGV[2], ARGV[1]); " +
                    "    return 1; " +
                    "else " +
                    "    return 0; " +
                    "end; "
            , ResultType.BOOLEAN);

    /**
     * 解锁
     * <p>
     * KEYS[1] 键 <p>
     * ARGV[1] sid
     * <p>
     * {@code Returns:} 解锁成功返回 true，否则返回 false。
     */
    public static final RedisScript UNLOCK = new RedisScript(
            "local sid = redis.call('GET', KEYS[1]); " +
                    "if not sid then " +
                    "    return 1; " +
                    "end; " +
                    "if sid == ARGV[1] then " +
                    "    redis.call('DEL', KEYS[1]); " +
                    "    return 1; " +
                    "end; " +
                    "return 0;"
            , ResultType.BOOLEAN);

}