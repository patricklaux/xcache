package com.igeeksky.xcache.redis.store;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.core.CacheValue;
import com.igeeksky.xcache.core.CacheKeyPrefix;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.function.tuple.ExpiryKeyValue;
import com.igeeksky.xtool.core.function.tuple.KeyValue;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/13
 */
public class RedisStringStore<V> extends AbstractRedisStore<V> {

    private final RedisOperator connection;

    private final boolean enableKeyPrefix;

    private final boolean enableRandomTtl;

    private final long expireAfterWrite;

    private final long expireAfterWriteMin;

    private final StringCodec stringCodec;

    private final CacheKeyPrefix cacheKeyPrefix;

    public RedisStringStore(RedisOperator connection, RedisStoreConfig<V> config) {
        super(config.isEnableNullValue(), config.isEnableCompressValue(),
                config.getValueCompressor(), config.getValueCodec());
        this.connection = connection;
        this.enableKeyPrefix = config.isEnableKeyPrefix();
        this.enableRandomTtl = config.isEnableRandomTtl();
        this.expireAfterWrite = config.getExpireAfterWrite();
        this.expireAfterWriteMin = (long) (expireAfterWrite * 0.8);
        this.stringCodec = StringCodec.getInstance(config.getCharset());
        this.cacheKeyPrefix = new CacheKeyPrefix(config.getName(), stringCodec);
    }

    @Override
    public CacheValue<V> get(String key) {
        return fromExtraStoreValue(connection.get(toStoreKey(key)));
    }

    @Override
    public Map<String, CacheValue<V>> getAll(Set<? extends String> keys) {
        List<KeyValue<byte[], byte[]>> keyValues = connection.mget(toKeysArray(keys));

        Map<String, CacheValue<V>> result = Maps.newLinkedHashMap(keyValues.size());
        for (KeyValue<byte[], byte[]> kv : keyValues) {
            CacheValue<V> cacheValue = fromExtraStoreValue(kv.getValue());
            if (cacheValue != null) {
                result.put(fromStoreKey(kv.getKey()), cacheValue);
            }
        }

        return result;
    }

    @Override
    public void put(String key, V value) {
        byte[] storeValue = toExtraStoreValue(value);
        if (storeValue != null) {
            if (expireAfterWrite <= 0) {
                checkResult(connection.set(toStoreKey(key), storeValue), "put", key, value);
            } else {
                checkResult(connection.psetex(toStoreKey(key), timeToLive(), storeValue), "psetex", key, value);
            }
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> keyValues) {
        if (expireAfterWrite <= 0) {
            Map<byte[], byte[]> map = Maps.newHashMap(keyValues.size());
            keyValues.forEach((k, v) -> {
                byte[] storeValue = toExtraStoreValue(v);
                if (storeValue != null) {
                    map.put(toStoreKey(k), storeValue);
                }
            });
            checkResult(connection.mset(map), "mset");
            return;
        }

        List<ExpiryKeyValue<byte[], byte[]>> expiryKeyValues = new ArrayList<>(keyValues.size());
        keyValues.forEach((k, v) -> {
            byte[] storeValue = toExtraStoreValue(v);
            if (storeValue != null) {
                expiryKeyValues.add(new ExpiryKeyValue<>(toStoreKey(k), storeValue, timeToLive()));
            }
        });

        checkResult(connection.mpsetex(expiryKeyValues), "mset");
    }

    @Override
    public void evict(String key) {
        connection.del(toStoreKey(key));
    }

    @Override
    public void evictAll(Set<? extends String> keys) {
        connection.del(toKeysArray(keys));
    }

    @Override
    public void clear() {
        connection.clear(cacheKeyPrefix.concatPrefixBytes("*"));
    }

    private byte[][] toKeysArray(Set<? extends String> keys) {
        byte[][] keysArray = new byte[keys.size()][];
        int i = 0;
        for (String key : keys) {
            keysArray[i++] = toStoreKey(key);
        }
        return keysArray;
    }

    private byte[] toStoreKey(String key) {
        if (enableKeyPrefix) {
            return cacheKeyPrefix.concatPrefixBytes(key);
        }
        return stringCodec.encode(key);
    }

    private String fromStoreKey(byte[] storeKey) {
        if (enableKeyPrefix) {
            return cacheKeyPrefix.removePrefix(storeKey);
        }
        return stringCodec.decode(storeKey);
    }

    /**
     * 随机生成过期时间
     *
     * @return 如果 randomAliveTime 为 true，随机生成过期时间；否则返回配置的 expireAfterWrite
     */
    private long timeToLive() {
        if (enableRandomTtl) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            return random.nextLong(expireAfterWriteMin, expireAfterWrite);
        }
        return expireAfterWrite;
    }

}