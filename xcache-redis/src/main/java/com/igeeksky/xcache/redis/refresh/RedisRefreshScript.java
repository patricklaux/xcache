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
     * ARGV[2] 存续时间 单位：Millisecond
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
     * {@code return} 解锁成功返回 true，否则返回 false。
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
     * 更新刷新线程启动时间
     * <p>
     * KEYS[1] refreshPeriodKey <p>
     * ARGV[1] refreshThreadPeriod 单位：Millisecond
     */
    public static final RedisScript UPDATE_TASK_TIME = new RedisScript(
            "redis.replicate_commands(); " +
                    "local server_time = redis.call('time'); " +
                    "local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); " +
                    "local task_time = now + ARGV[1]; " +
                    "return redis.call('set', KEYS[1], task_time); "
            , ResultType.STATUS);


    /**
     * 是否已到达刷新线程启动时间
     * <p>
     * KEYS[1] refreshPeriodKey <p>
     * <p>
     * 已到启动时间返回 true，否则返回 false。
     * {@snippet :
     * if (未设置启动时间){
     *     return 1;
     * }
     * if (当前时间 >= 启动时间){
     *     return 1;
     * }
     * return 0;
     *}
     */
    public static final RedisScript ARRIVED_TASK_TIME = new RedisScript(
            "if (redis.call('exists', KEYS[1]) == 0) then " +
                    "    return 1; " +
                    "end; " +
                    "local task_time = tonumber(redis.call('get', KEYS[1])); " +
                    "local server_time = redis.call('time'); " +
                    "local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); " +
                    "if (now >= task_time) then " +
                    "    return 1; " +
                    "end; " +
                    "return 0;"
            , ResultType.BOOLEAN);

}