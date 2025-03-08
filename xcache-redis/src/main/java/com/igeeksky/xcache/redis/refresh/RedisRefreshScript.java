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


    /**
     * 新增或更新数据刷新时间
     * <p>
     * KEYS[1] refreshKey <p>
     * ARGV[1] refreshAfterWrite 单位：Millisecond <p>
     * ARGV[2~n] members
     * <p>
     * {@code Returns:} 刷新时间
     */
    public static final RedisScript PUT = new RedisScript(
            "redis.replicate_commands(); " +
                    "local key = KEYS[1]; " +
                    "local server_time = redis.call('time'); " +
                    "local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); " +
                    "local refresh_time = now + ARGV[1]; " +
                    "for i = 2, #(ARGV) do " +
                    "    redis.call('zadd', key, refresh_time, ARGV[i]); " +
                    "end; " +
                    "return refresh_time;"
            , ResultType.INTEGER);


    /**
     * 获取已到刷新时间的成员并更新刷新时间
     * <p>
     * KEYS[1] refreshKey <p>
     * ARGV[1] refreshAfterWrite 单位：Millisecond <p>
     * ARGV[2] count
     * <p>
     * {@code Returns:} 刷新时间
     */
    public static final RedisScript GET_UPDATE_REFRESH_MEMBERS = new RedisScript(
            "redis.replicate_commands(); " +
                    "local key = KEYS[1]; " +
                    "local server_time = redis.call('time'); " +
                    "local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); " +
                    "local refresh_time = now + ARGV[1]; " +
                    "local members = redis.call('ZRANGEBYSCORE', key, 0, refresh_time, 'LIMIT', 0, ARGV[2]); " +
                    "for i = 1, #(members) do " +
                    "    redis.call('zadd', key, refresh_time, members[i]); " +
                    "end; " +
                    "return members;"
            , ResultType.MULTI);

}