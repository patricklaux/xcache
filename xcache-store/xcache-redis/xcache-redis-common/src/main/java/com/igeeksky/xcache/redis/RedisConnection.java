package com.igeeksky.xcache.redis;

import com.igeeksky.xcache.common.ExpiryKeyValue;
import com.igeeksky.xcache.common.KeyValue;

import java.util.List;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public interface RedisConnection extends AutoCloseable {

    String OK = "OK";

    boolean isCluster();

    // String Command --start--
    byte[] get(byte[] key);

    List<KeyValue<byte[], byte[]>> mget(byte[]... keys);

    void set(byte[] key, byte[] value);

    void psetex(byte[] key, long milliseconds, byte[] value);

    void mset(Map<byte[], byte[]> keyValues);

    /**
     * <b>批量存储带过期时间的键值对集合</b>
     *
     * @param keyValues 键值对集合
     */
    void mpsetex(List<ExpiryKeyValue<byte[], byte[]>> keyValues);

    /**
     * <b>批量删除键集</b>
     * <p>
     * 当数据量小于等于限定数量时，直接删除；当数据量大于限定数量时 (size &gt; 500)，分批删除
     *
     * @param keys 键集
     */
    void del(byte[]... keys);
    // String Command --end--

    // Hash Command --start--
    byte[] hget(byte[] key, byte[] field);

    List<KeyValue<byte[], byte[]>> hmget(byte[] key, byte[]... field);

    void hset(byte[] key, byte[] field, byte[] value);

    void hmset(byte[] key, Map<byte[], byte[]> map);

    void hdel(byte[] key, byte[]... fields);
    // Hash Command --end--

    List<byte[]> keys(byte[] matches);

    void clear(byte[] matches);
}