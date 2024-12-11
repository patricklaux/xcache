package com.igeeksky.redis;

import com.igeeksky.redis.sorted.ScoredValue;
import com.igeeksky.redis.stream.AddOptions;
import com.igeeksky.xtool.core.function.tuple.ExpiryKeyValue;
import com.igeeksky.xtool.core.function.tuple.KeyValue;

import java.util.List;
import java.util.Map;

/**
 * Redis 客户端操作接口
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public interface RedisOperator extends AutoCloseable {

    /**
     * Redis 服务端正确执行命令时的返回值
     */
    String OK = "OK";

    /**
     * 是否为集群模式
     *
     * @return true：集群模式；false：单机模式
     */
    boolean isCluster();

    /**
     * Redis 客户端是否未关闭连接
     *
     * @return true：已打开；false：未打开
     */
    boolean isOpen();

    // String Command --start--

    Long exists(byte[]... keys);

    /**
     * 获取指定键的值。
     *
     * @param key 键
     * @return 值
     */
    byte[] get(byte[] key);

    /**
     * 批量获取指定键的值。
     *
     * @param keys 键集
     * @return 值集
     */
    List<KeyValue<byte[], byte[]>> mget(byte[]... keys);

    /**
     * 存储键值对。
     *
     * @param key   键
     * @param value 值
     * @return result
     */
    String set(byte[] key, byte[] value);

    /**
     * 存储键值对，并设置过期时间。
     *
     * @param key          键
     * @param milliseconds 过期时间，单位：毫秒
     * @param value        值
     * @return result
     */
    String psetex(byte[] key, long milliseconds, byte[] value);

    /**
     * 设置键的过期时间。
     *
     * @param key          键
     * @param milliseconds 过期时间，单位：毫秒
     * @return true：设置成功；false：设置失败
     */
    boolean pexpire(byte[] key, long milliseconds);

    /**
     * 批量存储键值对。
     *
     * @param keyValues 键值对集合
     * @return result
     */
    String mset(Map<byte[], byte[]> keyValues);

    /**
     * <b>批量存储带过期时间的键值对集合</b>
     *
     * @param keyValues 键值对集合
     * @return result
     */
    String mpsetex(List<ExpiryKeyValue<byte[], byte[]>> keyValues);

    /**
     * <b>批量删除键集</b>
     * <p>
     * 当数据量小于等于限定数量时，直接删除；当数据量大于限定数量时 (size &gt; 20000)，分批删除
     *
     * @param keys 键集
     * @return 实际删除的键的数量
     */
    long del(byte[]... keys);
    // String Command --end--

    // Hash Command --start--

    boolean hexists(byte[] key, byte[] field);

    /**
     * 获取指定键的指定字段的值。
     *
     * @param key   键
     * @param field 字段
     * @return 值
     */
    byte[] hget(byte[] key, byte[] field);

    /**
     * 获取指定键的所有字段的值。
     *
     * @param key 键
     * @return 所有字段的值
     */
    Map<byte[], byte[]> hgetall(byte[] key);

    /**
     * 批量获取指定键的指定字段的值。
     *
     * @param key   键
     * @param field 字段集
     * @return 结果集
     */
    List<KeyValue<byte[], byte[]>> hmget(byte[] key, byte[]... field);

    /**
     * 批量获取指定键的指定字段的值。
     *
     * @param keyFields (键 : 属性) 集合
     * @param totalSize 所有 field 的数量
     * @return 结果集
     */
    List<KeyValue<byte[], byte[]>> hmget(Map<byte[], List<byte[]>> keyFields, int totalSize);

    /**
     * 设置指定键的指定字段的值。
     *
     * @param key   键
     * @param field 字段
     * @param value 值
     * @return true：设置成功；false：设置失败
     */
    Boolean hset(byte[] key, byte[] field, byte[] value);

    /**
     * 批量设置指定键的指定字段的值。
     *
     * @param key         键
     * @param fieldValues 字段 : 值 的集合
     * @return 命令执行结果：如正确执行，返回 OK。
     */
    String hmset(byte[] key, Map<byte[], byte[]> fieldValues);

    /**
     * 批量设置指定键的指定字段的值。
     *
     * @param keyFieldValues 键 : 字段 : 值 的集合
     * @return 命令执行结果：如正确执行，返回 OK。
     */
    String hmset(Map<byte[], Map<byte[], byte[]>> keyFieldValues);

    /**
     * 删除指定键的指定字段。
     *
     * @param key    键
     * @param fields 字段列表
     * @return 删除的字段数量
     */
    long hdel(byte[] key, byte[]... fields);

    /**
     * 批量删除指定键的指定字段。
     *
     * @param keyFields 键 : 字段列表
     * @return 删除的字段数量
     */
    long hdel(Map<byte[], List<byte[]>> keyFields);
    // Hash Command --end--


    // SortSet Command --start--

    long zadd(byte[] key, double score, byte[] member);

    long zadd(byte[] key, ScoredValue... scoredValues);

    long zrem(byte[] key, byte[]... members);

    long zremrangeByRank(byte[] key, long start, long stop);

    long zremrangeByScore(byte[] key, double min, double max);

    long zremrangeByLex(byte[] key, byte[] min, byte[] max);

    List<byte[]> zrangebyscore(byte[] key, double min, double max, long offset, long count);

    List<ScoredValue> zrangebyscoreWithScores(byte[] key, double min, double max, long offset, long count);

    long zcount(byte[] key, double min, double max);

    long zcard(byte[] key);

    long zrevrank(byte[] key, byte[] member);

    long zrank(byte[] key, byte[] member);

    double zscore(byte[] key, byte[] member);

    // SortSet Command --end--

    // Stream Command --start--

    /**
     * 向指定的 Stream 添加消息。
     *
     * @param key  Stream的名称。作为 Redis 中 Stream 数据结构的唯一标识。
     * @param body 包含一个或多个字段和值的 Map，这些字段和值将被添加到 Stream 中作为新的消息。
     * @return 返回此消息的 ID，该 ID 是唯一且按添加顺序递增的。
     */
    String xadd(byte[] key, Map<byte[], byte[]> body);

    /**
     * 向指定的 Stream 添加消息。
     *
     * @param key  Stream的名称。作为 Redis 中 Stream 数据结构的唯一标识。
     * @param args 添加消息的参数。
     * @param body 包含一个或多个字段和值的 Map，这些字段和值将被添加到 Stream 中作为新的消息。
     * @return 返回此消息的 ID，该 ID 是唯一且按添加顺序递增的。
     */
    String xadd(byte[] key, AddOptions args, Map<byte[], byte[]> body);
    // Stream Command --end--


    // Script Command --start--

    /**
     * 检查给定的脚本是否已经被加载。
     *
     * @param digests 脚本的 SHA1 校验和。
     * @return 一个布尔值列表，指示给定的脚本是否已经被加载。
     */
    List<Boolean> scriptExists(String... digests);

    /**
     * 将脚本加载到 Redis 服务器，并返回 SHA1 校验和。
     *
     * @param script 脚本对象。
     * @return 返回 SHA1 校验和。
     */
    String scriptLoad(RedisScript<?> script);

    /**
     * 使用指定的键和参数执行 Lua 脚本。
     *
     * @param <T>      返回结果类型
     * @param script   脚本对象
     * @param keyCount 键的数量
     * @param params   脚本参数列表
     * @return 返回执行脚本的结果。
     */
    <T> T eval(RedisScript<T> script, int keyCount, byte[]... params);

    /**
     * 使用指定的键和参数执行 Lua 脚本。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键的列表
     * @param args   脚本参数列表
     * @return 返回执行脚本的结果。
     */
    <T> T eval(RedisScript<T> script, byte[][] keys, byte[]... args);

    /**
     * 使用指定的键和参数执行只读的 Lua 脚本。
     *
     * @param <T>      返回结果类型
     * @param script   脚本对象
     * @param keyCount 键的数量
     * @param params   脚本参数列表
     * @return 返回执行脚本的结果。
     */
    <T> T evalReadOnly(RedisScript<T> script, int keyCount, byte[]... params);

    /**
     * 使用指定的键和参数执行只读的 Lua 脚本。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键的列表
     * @param args   脚本参数列表
     * @return 返回执行脚本的结果。
     */
    <T> T evalReadOnly(RedisScript<T> script, byte[][] keys, byte[]... args);

    /**
     * 使用指定的键和参数执行只读的 Lua 脚本。
     *
     * @param <T>      返回结果类型
     * @param script   脚本对象
     * @param keyCount 键数量
     * @param params   脚本参数列表
     * @return 返回执行脚本的结果
     */
    <T> T evalsha(RedisScript<T> script, int keyCount, byte[]... params);

    /**
     * 使用指定的键和参数执行只读的 Lua 脚本。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象。
     * @param keys   键的列表。
     * @param args   脚本参数列表
     * @return 返回执行脚本的结果。
     */
    <T> T evalsha(RedisScript<T> script, byte[][] keys, byte[]... args);

    /**
     * 使用指定的键和参数执行只读的 Lua 脚本。
     *
     * @param <T>      返回结果类型
     * @param script   脚本对象。
     * @param keyCount 键的数量。
     * @param params   脚本参数列表
     * @return 返回执行脚本的结果。
     */
    <T> T evalshaReadOnly(RedisScript<T> script, int keyCount, byte[]... params);

    /**
     * 使用指定的键和参数执行只读的 Lua 脚本。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象。
     * @param keys   键的列表。
     * @param args   脚本参数列表
     * @return 返回执行脚本的结果。
     */
    <T> T evalshaReadOnly(RedisScript<T> script, byte[][] keys, byte[]... args);
    // Script Command --end--


    // BaseCommand --start--

    /**
     * 获取匹配指定模式的所有键。
     *
     * @param matches 匹配模式
     * @return 键的列表
     */
    List<byte[]> keys(byte[] matches);

    /**
     * 清除匹配指定模式的所有键。
     *
     * @param matches 匹配模式
     * @return 删除的键的数量
     */
    long clear(byte[] matches);

    /**
     * 发布消息。
     * <p>
     * 发布订阅模式，用于在多个客户端之间广播消息。
     *
     * @param channel 发布消息的频道。
     * @param message 发布的消息。
     * @return 收到此消息的订阅者数量
     */
    long publish(byte[] channel, byte[] message);

    /**
     * 获取 Redis 服务器的当前时间戳。
     *
     * @return UNIX时间戳，毫秒
     */
    long timeMillis();

    // BaseCommand --end--
}