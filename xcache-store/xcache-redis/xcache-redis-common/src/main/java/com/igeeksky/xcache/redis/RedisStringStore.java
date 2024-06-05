package com.igeeksky.xcache.redis;

import com.igeeksky.xcache.common.*;
import com.igeeksky.xcache.config.CacheConfig;
import com.igeeksky.xcache.extension.serializer.StringSerializer;
import com.igeeksky.xcache.store.RemoteStore;
import com.igeeksky.xtool.core.collection.Maps;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-03
 */
public class RedisStringStore implements RemoteStore {

    public static final String STORE_NAME = "redis-string";

    private final boolean enableKeyPrefix;

    private final boolean enableRandomTtl;

    private final long expireAfterWrite;

    private final long expireAfterWriteMin;

    private final StringSerializer serializer;

    private final CacheKeyPrefix cacheKeyPrefix;

    private final RedisConnection redisConnection;

    public RedisStringStore(CacheConfig<?, ?> config, StringSerializer serializer, RedisConnection connection) {
        this.serializer = serializer;
        this.redisConnection = connection;
        this.enableKeyPrefix = config.getRemoteConfig().isEnableKeyPrefix();
        this.enableRandomTtl = config.getRemoteConfig().isEnableRandomTtl();
        this.expireAfterWrite = config.getRemoteConfig().getExpireAfterWrite();
        this.expireAfterWriteMin = (long) (expireAfterWrite * 0.8);
        this.cacheKeyPrefix = new CacheKeyPrefix(config.getName(), serializer);
    }

    @Override
    public CacheValue<byte[]> get(String key) {
        byte[] value = redisConnection.get(toStoreKey(key));
        if (value != null) {
            return CacheValues.newCacheValue(value);
        }
        return null;
    }

    @Override
    public Map<String, CacheValue<byte[]>> getAll(Set<? extends String> keys) {
        byte[][] keysArray = toKeysArray(keys);

        List<KeyValue<byte[], byte[]>> keyValues = redisConnection.mget(keysArray);

        Map<String, CacheValue<byte[]>> result = Maps.newLinkedHashMap(keyValues.size());
        for (KeyValue<byte[], byte[]> kv : keyValues) {
            result.put(fromStoreKey(kv.getKey()), new CacheValue<>(kv.getValue()));
        }

        return result;
    }

    private byte[][] toKeysArray(Set<? extends String> keys) {
        byte[][] keysArray = new byte[keys.size()][];
        int i = 0;
        for (String key : keys) {
            keysArray[i++] = toStoreKey(key);
        }
        return keysArray;
    }

    @Override
    public void put(String key, byte[] value) {
        if (expireAfterWrite <= 0) {
            redisConnection.set(toStoreKey(key), value);
        } else {
            redisConnection.psetex(toStoreKey(key), timeToLive(), value);
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends byte[]> keyValues) {
        if (expireAfterWrite <= 0) {
            Map<byte[], byte[]> map = new HashMap<>();
            keyValues.forEach((k, v) -> map.put(toStoreKey(k), v));
            redisConnection.mset(map);
            return;
        }
        List<ExpiryKeyValue<byte[], byte[]>> expiryKeyValues = new ArrayList<>();
        keyValues.forEach((k, v) -> expiryKeyValues.add(new ExpiryKeyValue<>(toStoreKey(k), v, timeToLive())));
        redisConnection.mpsetex(expiryKeyValues);
    }

    @Override
    public void evict(String key) {
        redisConnection.del(toStoreKey(key));
    }

    @Override
    public void evictAll(Set<? extends String> keys) {
        byte[][] keysArray = toKeysArray(keys);
        redisConnection.del(keysArray);
    }

    @Override
    public String getStoreName() {
        return STORE_NAME;
    }

    private byte[] toStoreKey(String key) {
        if (enableKeyPrefix) {
            return cacheKeyPrefix.concatPrefixBytes(key);
        }
        return serializer.serialize(key);
    }

    private String fromStoreKey(byte[] storeKey) {
        if (enableKeyPrefix) {
            return cacheKeyPrefix.removePrefix(storeKey);
        }
        return serializer.deserialize(storeKey);
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

    @Override
    public void clear() {
        redisConnection.clear(cacheKeyPrefix.concatPrefixBytes("*"));
    }

}