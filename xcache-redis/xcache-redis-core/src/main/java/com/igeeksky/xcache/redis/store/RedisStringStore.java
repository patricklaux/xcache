package com.igeeksky.xcache.redis.store;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.CacheKeyPrefix;
import com.igeeksky.xcache.core.ExtraStoreValueConvertor;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.function.tuple.ExpiryKeyValue;
import com.igeeksky.xtool.core.function.tuple.KeyValue;
import com.igeeksky.xtool.core.lang.RandomUtils;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/13
 */
public class RedisStringStore<V> implements RedisStore<V> {

    private final RedisOperator operator;

    private final boolean enableRandomTtl;

    private final long expireAfterWrite;

    private final long expireAfterWriteMin;

    private final CacheKeyPrefix cacheKeyPrefix;

    private final ExtraStoreValueConvertor<V> convertor;

    public RedisStringStore(RedisOperator operator, RedisStoreConfig<V> config) {
        this.operator = operator;
        this.enableRandomTtl = config.isEnableRandomTtl();
        this.expireAfterWrite = config.getExpireAfterWrite();
        this.expireAfterWriteMin = (long) (expireAfterWrite * 0.8);
        this.cacheKeyPrefix = new CacheKeyPrefix(config.getGroup(), config.getName(),
                config.isEnableGroupPrefix(), StringCodec.getInstance(config.getCharset()));
        this.convertor = new ExtraStoreValueConvertor<>(config.isEnableNullValue(), config.isEnableCompressValue(),
                config.getValueCodec(), config.getValueCompressor());
    }

    @Override
    public CacheValue<V> get(String key) {
        return this.convertor.fromExtraStoreValue(this.operator.get(toStoreKey(key)));
    }

    @Override
    public Map<String, CacheValue<V>> getAll(Set<? extends String> keys) {
        List<KeyValue<byte[], byte[]>> keyValues = this.operator.mget(toKeysArray(keys));

        Map<String, CacheValue<V>> result = Maps.newLinkedHashMap(keyValues.size());
        for (KeyValue<byte[], byte[]> kv : keyValues) {
            CacheValue<V> cacheValue = this.convertor.fromExtraStoreValue(kv.getValue());
            if (cacheValue != null) {
                result.put(fromStoreKey(kv.getKey()), cacheValue);
            }
        }

        return result;
    }

    @Override
    public void put(String key, V value) {
        byte[] storeValue = this.convertor.toExtraStoreValue(value);
        if (storeValue != null) {
            if (expireAfterWrite <= 0) {
                checkResult(this.operator.set(toStoreKey(key), storeValue), "put", key, value);
            } else {
                checkResult(this.operator.psetex(toStoreKey(key), timeToLive(), storeValue), "psetex", key, value);
            }
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> keyValues) {
        if (expireAfterWrite <= 0) {
            Map<byte[], byte[]> map = Maps.newHashMap(keyValues.size());
            keyValues.forEach((k, v) -> {
                byte[] storeValue = this.convertor.toExtraStoreValue(v);
                if (storeValue != null) {
                    map.put(toStoreKey(k), storeValue);
                }
            });
            checkResult(this.operator.mset(map), "mset");
            return;
        }

        List<ExpiryKeyValue<byte[], byte[]>> expiryKeyValues = new ArrayList<>(keyValues.size());
        keyValues.forEach((k, v) -> {
            byte[] storeValue = this.convertor.toExtraStoreValue(v);
            if (storeValue != null) {
                expiryKeyValues.add(new ExpiryKeyValue<>(toStoreKey(k), storeValue, timeToLive()));
            }
        });

        checkResult(this.operator.mpsetex(expiryKeyValues), "mset");
    }

    @Override
    public void evict(String key) {
        this.operator.del(toStoreKey(key));
    }

    @Override
    public void evictAll(Set<? extends String> keys) {
        this.operator.del(toKeysArray(keys));
    }

    @Override
    public void clear() {
        this.operator.clear(this.cacheKeyPrefix.concatPrefixBytes("*"));
    }

    private byte[][] toKeysArray(Set<? extends String> keys) {
        byte[][] keysArray = new byte[keys.size()][];
        int i = 0;
        for (String key : keys) {
            keysArray[i++] = this.toStoreKey(key);
        }
        return keysArray;
    }

    private byte[] toStoreKey(String key) {
        return this.cacheKeyPrefix.concatPrefixBytes(key);
    }

    private String fromStoreKey(byte[] storeKey) {
        return this.cacheKeyPrefix.removePrefix(storeKey);
    }

    /**
     * 随机生成过期时间
     *
     * @return 如果 randomAliveTime 为 true，返回随机生成的过期时间；否则返回配置的 expireAfterWrite
     */
    private long timeToLive() {
        if (enableRandomTtl) {
            return RandomUtils.nextLong(expireAfterWriteMin, expireAfterWrite);
        }
        return expireAfterWrite;
    }

}