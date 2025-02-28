package com.igeeksky.xcache.redis.store;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xredis.common.RedisFutureHelper;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xtool.core.KeyValue;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Redis Hash 存储辅助类
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class RedisHashStoreHelper<V> {

    private final StringCodec stringCodec;
    private final Function<byte[], CacheValue<V>> valueConvertor;

    /**
     * 创建 {@link RedisHashStoreHelper}
     *
     * @param stringCodec    字符串编解码
     * @param valueConvertor 值转换（缓存中的 Value {@code byte[]} 转换为 {@code CacheValue<V>}）
     */
    public RedisHashStoreHelper(StringCodec stringCodec, Function<byte[], CacheValue<V>> valueConvertor) {
        this.stringCodec = stringCodec;
        this.valueConvertor = valueConvertor;
    }

    /**
     * redis-hash 中获取的 {@code List<KeyValue<byte[], byte[]>>} 转换为 {@code Map<String, CacheValue<V>>}
     *
     * @param keyValues redis-hash 中缓存的键值对列表
     * @return {@link Map} – 缓存键：缓存值
     */
    public Map<String, CacheValue<V>> toResult(List<KeyValue<byte[], byte[]>> keyValues) {
        if (CollectionUtils.isEmpty(keyValues)) {
            return Collections.emptyMap();
        }

        Map<String, CacheValue<V>> result = HashMap.newHashMap(keyValues.size());
        for (KeyValue<byte[], byte[]> kv : keyValues) {
            if (kv != null && kv.hasValue()) {
                CacheValue<V> cacheValue = this.valueConvertor.apply(kv.getValue());
                if (cacheValue != null) {
                    result.put(this.fromStoreField(kv.getKey()), cacheValue);
                }
            }
        }
        return result;
    }

    /**
     * {@link String} 转换为 {@code byte[]}
     *
     * @param field key
     * @return redis-hash 的字段
     */
    public byte[] toStoreField(String field) {
        return this.stringCodec.encode(field);
    }

    /**
     * {@code byte[]} 转换为 {@link String}
     *
     * @param field redis-hash 的字段
     * @return 缓存键
     */
    public String fromStoreField(byte[] field) {
        return this.stringCodec.decode(field);
    }

    /**
     * 检查 redis 版本是否支持 hash field 设置过期时间
     *
     * @param redisOperator    redis操作代理
     * @param expireAfterWrite 过期时间
     * @param batchTimeout          future.get 超时时间
     */
    public static void checkServerVersion(RedisOperatorProxy redisOperator, long expireAfterWrite, long batchTimeout) {
        if (expireAfterWrite <= 0) {
            return;
        }
        String version = RedisFutureHelper.get(redisOperator.version(), batchTimeout);
        String[] array = version.split("\\.");
        String errorMsg = "If using hash as storage and expireAfterWrite > 0," +
                " the redis_server version must be greater than or equal to 7.4.0";
        if (Integer.parseInt(array[0]) < 7) {
            throw new UnsupportedOperationException(errorMsg);
        }
        if (Integer.parseInt(array[1]) < 4) {
            throw new UnsupportedOperationException(errorMsg);
        }
    }

}
