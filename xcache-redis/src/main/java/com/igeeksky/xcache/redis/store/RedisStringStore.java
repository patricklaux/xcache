package com.igeeksky.xcache.redis.store;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.CacheKeyPrefix;
import com.igeeksky.xcache.core.ExtraStoreConvertor;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xtool.core.ExpiryKeyValue;
import com.igeeksky.xtool.core.KeyValue;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.RandomUtils;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/13
 */
public class RedisStringStore<V> extends RedisStore<V> {

    private final RedisOperatorProxy operator;

    private final boolean enableRandomTtl;

    private final long expireAfterWrite;

    private final long expireAfterWriteMin;

    private final CacheKeyPrefix cacheKeyPrefix;

    private final ExtraStoreConvertor<V> convertor;

    public RedisStringStore(RedisOperatorProxy operator, RedisStoreConfig<V> config) {
        super(operator.getTimeout());
        this.operator = operator;
        boolean enableRandomTtl = config.isEnableRandomTtl();
        this.expireAfterWrite = config.getExpireAfterWrite();
        this.expireAfterWriteMin = expireAfterWrite * 4 / 5;
        this.enableRandomTtl = enableRandomTtl && (expireAfterWrite - expireAfterWriteMin) > 1;
        this.cacheKeyPrefix = new CacheKeyPrefix(config.getGroup(), config.getName(),
                config.isEnableGroupPrefix(), StringCodec.getInstance(config.getCharset()));
        this.convertor = new ExtraStoreConvertor<>(config.isEnableNullValue(), config.isEnableCompressValue(),
                config.getValueCodec(), config.getValueCompressor());
    }

    @Override
    public CompletableFuture<CacheValue<V>> getCacheValueAsync(String key) {
        return this.operator.getAsync(toStoreKey(key)).thenApply(this.convertor::fromExtraStoreValue);
    }

    @Override
    public CompletableFuture<Map<String, CacheValue<V>>> getAllCacheValuesAsync(Set<? extends String> keys) {
        return this.operator.mgetAsync(toStoreKeys(keys))
                .thenApply(keyValues -> {
                    Map<String, CacheValue<V>> result = Maps.newHashMap(keyValues.size());
                    for (KeyValue<byte[], byte[]> kv : keyValues) {
                        if (kv != null && kv.hasValue()) {
                            CacheValue<V> cacheValue = this.convertor.fromExtraStoreValue(kv.getValue());
                            if (cacheValue != null) {
                                result.put(fromStoreKey(kv.getKey()), cacheValue);
                            }
                        }
                    }
                    return result;
                });
    }

    @Override
    public CompletableFuture<Void> putAsync(String key, V value) {
        byte[] storeKey = toStoreKey(key);
        byte[] storeValue = this.convertor.toExtraStoreValue(value);
        if (storeValue == null) {
            return this.operator.delAsync(storeKey).thenApply(ignored -> null);
        }
        if (this.expireAfterWrite > 0) {
            long ttl = (this.enableRandomTtl) ? randomTtl() : expireAfterWrite;
            return this.operator.psetexAsync(storeKey, ttl, storeValue)
                    .thenApply(status -> checkResult(status, key, value));
        }
        return this.operator.setAsync(storeKey, storeValue)
                .thenApply(status -> checkResult(status, key, value));
    }

    @Override
    public CompletableFuture<Void> putAllAsync(Map<? extends String, ? extends V> keyValues) {
        if (this.expireAfterWrite > 0) {
            if (this.enableRandomTtl) {
                return this.putAllRandomTtl(keyValues);
            }
            return this.putAllFixTtl(keyValues);
        }
        return this.putAllUnlimitedTtl(keyValues);
    }

    private CompletableFuture<Void> putAllRandomTtl(Map<? extends String, ? extends V> keyValues) {
        List<byte[]> removeKeys = new ArrayList<>();
        List<ExpiryKeyValue<byte[], byte[]>> putKeyValues = new ArrayList<>(keyValues.size());
        keyValues.forEach((k, v) -> {
            byte[] storeValue = this.convertor.toExtraStoreValue(v);
            if (storeValue == null) {
                removeKeys.add(toStoreKey(k));
            } else {
                putKeyValues.add(new ExpiryKeyValue<>(toStoreKey(k), storeValue, this.randomTtl()));
            }
        });
        if (!removeKeys.isEmpty()) {
            this.operator.delAsync(removeKeys.toArray(new byte[0][]));
        }
        return this.operator.psetexAsync(putKeyValues).thenApply(RedisStore::checkResult);
    }

    private CompletableFuture<Void> putAllFixTtl(Map<? extends String, ? extends V> keyValues) {
        List<byte[]> removeKeys = new ArrayList<>();
        List<KeyValue<byte[], byte[]>> putKeyValues = new ArrayList<>(keyValues.size());
        keyValues.forEach((k, v) -> {
            byte[] storeValue = this.convertor.toExtraStoreValue(v);
            if (storeValue == null) {
                removeKeys.add(toStoreKey(k));
            } else {
                putKeyValues.add(new KeyValue<>(toStoreKey(k), storeValue));
            }
        });
        if (!removeKeys.isEmpty()) {
            this.operator.delAsync(removeKeys.toArray(new byte[0][]));
        }
        return this.operator.psetexAsync(putKeyValues, expireAfterWrite).thenApply(RedisStore::checkResult);
    }

    private CompletableFuture<Void> putAllUnlimitedTtl(Map<? extends String, ? extends V> keyValues) {
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
            this.operator.delAsync(removeKeys.toArray(new byte[0][]));
        }
        return this.operator.msetAsync(putKeyValues).thenApply(RedisStore::checkResult);
    }

    @Override
    public CompletableFuture<Void> removeAsync(String key) {
        return this.operator.delAsync(toStoreKey(key)).thenApply(ignore -> null);
    }

    @Override
    public CompletableFuture<Void> removeAllAsync(Set<? extends String> keys) {
        return this.operator.delAsync(toStoreKeys(keys)).thenApply(ignore -> null);
    }

    @Override
    public void clear() {
        this.operator.clear(this.cacheKeyPrefix.concatPrefixBytes("*"));
    }

    private byte[][] toStoreKeys(Set<? extends String> keys) {
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
     * 生成随机存活时间
     *
     * @return 返回随机存活时间
     */
    private long randomTtl() {
        return RandomUtils.nextLong(expireAfterWriteMin, expireAfterWrite);
    }

}
