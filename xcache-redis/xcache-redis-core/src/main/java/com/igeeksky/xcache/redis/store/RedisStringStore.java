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
    public boolean contains(String key) {
        return this.getCacheValue(key) != null;
    }

    @Override
    public CacheValue<V> getCacheValue(String key) {
        return this.convertor.fromExtraStoreValue(this.operator.get(toStoreKey(key)));
    }

    @Override
    public Map<String, CacheValue<V>> getAllCacheValues(Set<? extends String> keys) {
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
        byte[] storeKey = toStoreKey(key);
        byte[] storeValue = this.convertor.toExtraStoreValue(value);
        if (storeValue == null) {
            this.operator.del(storeKey);
        } else {
            if (expireAfterWrite <= 0) {
                checkResult(this.operator.set(storeKey, storeValue), "put", key, value);
            } else {
                checkResult(this.operator.psetex(storeKey, timeToLive(), storeValue), "psetex", key, value);
            }
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> keyValues) {
        if (expireAfterWrite <= 0) {
            List<byte[]> removeKeys = new ArrayList<>();
            Map<byte[], byte[]> putKeyValues = Maps.newHashMap(keyValues.size());
            keyValues.forEach((k, v) -> {
                byte[] storeKey = toStoreKey(k);
                byte[] storeValue = this.convertor.toExtraStoreValue(v);
                if (storeValue == null) {
                    removeKeys.add(storeKey);
                } else {
                    putKeyValues.put(storeKey, storeValue);
                }
            });
            if (!removeKeys.isEmpty()) {
                this.operator.del(removeKeys.toArray(new byte[0][]));
            }
            checkResult(this.operator.mset(putKeyValues), "mset");
            return;
        }

        List<byte[]> removeKeys = new ArrayList<>();
        List<ExpiryKeyValue<byte[], byte[]>> putKeyValues = new ArrayList<>(keyValues.size());
        keyValues.forEach((k, v) -> {
            byte[] storeValue = this.convertor.toExtraStoreValue(v);
            if (storeValue == null) {
                removeKeys.add(toStoreKey(k));
            } else {
                putKeyValues.add(new ExpiryKeyValue<>(toStoreKey(k), storeValue, timeToLive()));
            }
        });

        if (!removeKeys.isEmpty()) {
            this.operator.del(removeKeys.toArray(new byte[0][]));
        }
        checkResult(this.operator.mpsetex(putKeyValues), "mset");
    }

    @Override
    public void remove(String key) {
        this.operator.del(toStoreKey(key));
    }

    @Override
    public void removeAll(Set<? extends String> keys) {
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