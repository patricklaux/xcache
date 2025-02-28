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
        super(config.getBatchTimeout());
        this.operator = operator;
        this.enableRandomTtl = config.isEnableRandomTtl();
        this.expireAfterWrite = config.getExpireAfterWrite();
        this.expireAfterWriteMin = expireAfterWrite * 4 / 5;
        this.cacheKeyPrefix = new CacheKeyPrefix(config.getGroup(), config.getName(),
                config.isEnableGroupPrefix(), StringCodec.getInstance(config.getCharset()));
        this.convertor = new ExtraStoreConvertor<>(config.isEnableNullValue(), config.isEnableCompressValue(),
                config.getValueCodec(), config.getValueCompressor());
    }

    @Override
    public CompletableFuture<CacheValue<V>> asyncGetCacheValue(String key) {
        return this.operator.get(toStoreKey(key)).thenApply(this.convertor::fromExtraStoreValue);
    }

    @Override
    public CompletableFuture<Map<String, CacheValue<V>>> asyncGetAllCacheValues(Set<? extends String> keys) {
        return this.operator.mget(toStoreKeys(keys))
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
    public CompletableFuture<Void> asyncPut(String key, V value) {
        byte[] storeKey = toStoreKey(key);
        byte[] storeValue = this.convertor.toExtraStoreValue(value);
        if (storeValue == null) {
            return this.operator.del(storeKey).thenApply(ignored -> null);
        }
        if (expireAfterWrite <= 0) {
            return this.operator.set(storeKey, storeValue)
                    .thenApply(status -> checkResult(status, key, value));
        }
        return this.operator.psetex(storeKey, timeToLive(), storeValue)
                .thenApply(status -> checkResult(status, key, value));
    }

    @Override
    public CompletableFuture<Void> asyncPutAll(Map<? extends String, ? extends V> keyValues) {
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
            return this.operator.mset(putKeyValues).thenApply(RedisStore::checkResult);
        }

        List<byte[]> removeKeys = new ArrayList<>();
        List<ExpiryKeyValue<byte[], byte[]>> putKeyValues = new ArrayList<>(keyValues.size());
        keyValues.forEach((k, v) -> {
            byte[] storeValue = this.convertor.toExtraStoreValue(v);
            if (storeValue == null) {
                removeKeys.add(toStoreKey(k));
            } else {
                putKeyValues.add(new ExpiryKeyValue<>(toStoreKey(k), storeValue, this.timeToLive()));
            }
        });
        if (!removeKeys.isEmpty()) {
            this.operator.del(removeKeys.toArray(new byte[0][]));
        }
        return this.operator.psetex(putKeyValues).thenApply(RedisStore::checkResult);
    }

    @Override
    public CompletableFuture<Void> asyncRemove(String key) {
        return this.operator.del(toStoreKey(key)).thenApply(ignore -> null);
    }

    @Override
    public CompletableFuture<Void> asyncRemoveAll(Set<? extends String> keys) {
        return this.operator.del(toStoreKeys(keys)).thenApply(ignore -> null);
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
     * 生成过期时间
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
