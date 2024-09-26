package com.igeeksky.redis;

import com.igeeksky.redis.stream.AddOptions;
import com.igeeksky.xtool.core.function.tuple.ExpiryKeyValue;
import com.igeeksky.xtool.core.function.tuple.KeyValue;

import java.util.List;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public interface RedisOperator extends AutoCloseable {

    String OK = "OK";

    boolean isCluster();

    boolean isOpen();

    // String Command --start--
    byte[] get(byte[] key);

    List<KeyValue<byte[], byte[]>> mget(byte[]... keys);

    String set(byte[] key, byte[] value);

    String psetex(byte[] key, long milliseconds, byte[] value);

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
     */
    long del(byte[]... keys);
    // String Command --end--

    // Hash Command --start--
    byte[] hget(byte[] key, byte[] field);

    Map<byte[], byte[]> hgetall(byte[] key);

    List<KeyValue<byte[], byte[]>> hmget(byte[] key, byte[]... field);

    /**
     * @param keyFields 键 : 属性集
     * @param totalSize 所有 field 的数量
     * @return 结果集
     */
    List<KeyValue<byte[], byte[]>> hmget(Map<byte[], List<byte[]>> keyFields, int totalSize);

    Boolean hset(byte[] key, byte[] field, byte[] value);

    String hmset(byte[] key, Map<byte[], byte[]> map);

    String hmset(Map<byte[], Map<byte[], byte[]>> keyFieldValues);

    long hdel(byte[] key, byte[]... fields);

    long hdel(Map<byte[], List<byte[]>> keyFields);
    // Hash Command --end--

    List<byte[]> keys(byte[] matches);

    long clear(byte[] matches);

    void publish(byte[] channel, byte[] message);


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
    List<Boolean> scriptExists(String... digests);

    String scriptLoad(RedisScript<?> script);

    <T> T eval(RedisScript<T> script, int keyCount, byte[]... params);

    <T> T eval(RedisScript<T> script, byte[][] keys, byte[]... args);

    <T> T evalReadOnly(RedisScript<T> script, int keyCount, byte[]... params);

    <T> T evalReadOnly(RedisScript<T> script, byte[][] keys, byte[]... args);

    <T> T evalsha(RedisScript<T> script, int keyCount, byte[]... params);

    <T> T evalsha(RedisScript<T> script, byte[][] keys, byte[]... args);

    <T> T evalshaReadOnly(RedisScript<T> script, int keyCount, byte[]... params);

    <T> T evalshaReadOnly(RedisScript<T> script, byte[][] keys, byte[]... args);
    // Script Command --end--

    /**
     * 获取 Redis 服务器的当前时间戳。
     *
     * @return UNIX时间戳，毫秒
     */
    long timeMillis();
}