package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.redis.RedisScript;
import com.igeeksky.redis.ResultType;

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
    public static final RedisScript<Boolean> LOCK = new RedisScript<>(
            "redis.call('hdel', KEYS[1], ARGV[1]); " +
                    "if (redis.call('hlen', KEYS[1]) == 0) then " +
                    "redis.call('hset', KEYS[1], ARGV[1], 1); " +
                    "return redis.call('pexpire', KEYS[1], ARGV[2]); " +
                    "end; " +
                    "return 0;"
            , ResultType.BOOLEAN);


    // /**
    //  * 释放锁
    //  * <p>
    //  * KEYS[1] 键 <p>
    //  * ARGV[1] sid
    //  */
    // public static final RedisScript<Integer> UNLOCK = new RedisScript<>(
    //         "redis.call('hdel', KEYS[1], ARGV[1]); " +
    //                 "return redis.call('hlen', KEYS[1]);"
    //         , ResultType.INTEGER);


    /**
     * 锁续期
     * <p>
     * KEYS[1] 键 <p>
     * ARGV[1] sid <p>
     * ARGV[2] 存续时间 单位：Millisecond
     * <p>
     * 续期成功返回 true，否则返回 false。
     */
    public static final RedisScript<Boolean> LOCK_NEW_EXPIRE = new RedisScript<>(
            "redis.call('hset', KEYS[1], ARGV[1], 1); " +
                    "return redis.call('pexpire', KEYS[1], ARGV[2]);"
            , ResultType.BOOLEAN);


    /**
     * 更新刷新线程启动时间
     * <p>
     * KEYS[1] refreshPeriodKey <p>
     * ARGV[1] refreshThreadPeriod 单位：Millisecond
     */
    public static final RedisScript<Long> UPDATE_TASK_TIME = new RedisScript<>(
            "redis.replicate_commands(); " +
                    "local server_time = redis.call('time'); " +
                    "local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); " +
                    "local task_time = now + ARGV[1]; " +
                    "if (task_time > 0) then " +
                    "redis.call('set', KEYS[1], task_time); " +
                    "end; " +
                    "return task_time;"
            , ResultType.INTEGER);


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
    public static final RedisScript<Boolean> ARRIVED_TASK_TIME = new RedisScript<>(
            "if (redis.call('exists', KEYS[1]) == 0) then " +
                    "return 1; " +
                    "end; " +
                    "local task_time = tonumber(redis.call('get', KEYS[1])); " +
                    "local server_time = redis.call('time'); " +
                    "local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); " +
                    "if (now >= task_time) then " +
                    "return 1; " +
                    "end; " +
                    "return 0;"
            , ResultType.BOOLEAN);


    /**
     * 保存数据刷新时间
     * <p>
     * KEYS[1] refreshKey <p>
     * ARGV[1] refreshAfterWrite 单位：Millisecond <p>
     * ARGV[2~n] members
     */
    public static final RedisScript<Long> PUT = new RedisScript<>(
            "redis.replicate_commands(); " +
                    "local server_time = redis.call('time'); " +
                    "local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); " +
                    "local refresh_time = now + ARGV[1]; " +
                    "if (refresh_time > 0) then " +
                    "local array = {}; " +
                    "local j = 0; " +
                    "for i = 2,#(ARGV) do " +
                    "j = j + 1; " +
                    "array[j] = refresh_time; " +
                    "j = j + 1; " +
                    "array[j] = ARGV[i]; " +
                    "end; " +
                    "redis.call('zadd', KEYS[1], unpack(array)); " +
                    "end; " +
                    "return refresh_time;"
            , ResultType.INTEGER);


    /**
     * 删除数据刷新时间
     * <p>
     * KEYS[1] refreshKey <p>
     * ARGV[1~n] members
     */
    public static final RedisScript<Long> REMOVE = new RedisScript<>(
            "return redis.call('zrem', KEYS[1], unpack(ARGV));"
            , ResultType.INTEGER);

}