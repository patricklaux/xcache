package com.igeeksky.xcache.redis.store;


import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.ExtraStoreConvertor;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xtool.core.ExpiryKeyValue;
import com.igeeksky.xtool.core.KeyValue;
import com.igeeksky.xtool.core.lang.RandomUtils;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 使用哈希表作为缓存（非集群模式）
 * <p>
 * <b>注意：</b><br>
 * HashStore 支持设置过期时间，但 RedisServer 的版本必须为 7.4.0+。<br>
 * 见：<a href="https://redis.io/docs/latest/commands/hpexpire/">hpexpire</a><br/>
 *
 * @author Patrick.Lau
 * @since 0.0.3
 */
public class RedisHashStore<V> extends AbstractRedisStore<V> {

    private final byte[] hashKey;

    private final long expireAfterWrite;
    private final long expireAfterWriteMin;
    private final boolean enableRandomTtl;

    private final RedisOperatorProxy redisOperator;
    private final ExtraStoreConvertor<V> convertor;
    private final RedisHashStoreHelper<V> hashStoreHelper;

    public RedisHashStore(RedisOperatorProxy redisOperator, RedisStoreConfig<V> config) {
        super(redisOperator.getTimeout());
        this.redisOperator = redisOperator;
        boolean enableRandomTtl = config.isEnableRandomTtl();
        this.expireAfterWrite = config.getExpireAfterWrite();
        this.expireAfterWriteMin = expireAfterWrite * 4 / 5;
        this.enableRandomTtl = enableRandomTtl && (expireAfterWrite - expireAfterWriteMin) > 1;
        RedisHashStoreHelper.checkServerVersion(this.redisOperator, this.expireAfterWrite);

        StringCodec stringCodec = StringCodec.getInstance(config.getCharset());
        if (config.isEnableGroupPrefix()) {
            this.hashKey = stringCodec.encode(config.getGroup() + ":" + config.getName());
        } else {
            this.hashKey = stringCodec.encode(config.getName());
        }
        this.convertor = new ExtraStoreConvertor<>(config.isEnableNullValue(), config.isEnableCompressValue(),
                config.getValueCodec(), config.getValueCompressor());
        this.hashStoreHelper = new RedisHashStoreHelper<>(stringCodec, this.convertor::fromExtraStoreValue);
    }

    @Override
    public CompletableFuture<CacheValue<V>> getCacheValueAsync(String field) {
        return CompletableFuture.completedFuture(field)
                .thenApply(this.hashStoreHelper::toStoreField)
                .thenCompose(storeField -> this.redisOperator.hgetAsync(this.hashKey, storeField))
                .thenApply(this.convertor::fromExtraStoreValue);
    }

    @Override
    public CompletableFuture<Map<String, CacheValue<V>>> getAllCacheValuesAsync(Set<? extends String> fields) {
        return CompletableFuture.completedFuture(fields)
                .thenApply(this::toStoreFields)
                .thenCompose(storeFields -> this.redisOperator.hmgetAsync(this.hashKey, storeFields))
                .thenApply(this.hashStoreHelper::toResult);
    }

    @Override
    public CompletableFuture<Void> putAsync(String field, V value) {
        return CompletableFuture.completedFuture(KeyValue.create(field, value))
                .thenApply(fieldValue -> fieldValue.map(this.hashStoreHelper::toStoreField,
                        this.convertor::toExtraStoreValue))
                .thenCompose(fieldValue -> {
                    byte[] storeField = fieldValue.getKey();
                    byte[] storeValue = fieldValue.getValue();
                    if (storeValue == null) {
                        return this.redisOperator.hdelAsync(hashKey, storeField).thenApply(ignore -> null);
                    }
                    if (this.expireAfterWrite > 0) {
                        long ttl = (this.enableRandomTtl) ? randomTtl() : expireAfterWrite;
                        return this.redisOperator.hpsetAsync(hashKey, ttl, storeField, storeValue)
                                .thenApply(ignore -> null);
                    }
                    return this.redisOperator.hsetAsync(hashKey, storeField, storeValue).thenApply(ignore -> null);
                });
    }

    @Override
    public CompletableFuture<Void> putAllAsync(Map<? extends String, ? extends V> fieldValues) {
        if (expireAfterWrite > 0) {
            if (this.enableRandomTtl) {
                return CompletableFuture.completedFuture(fieldValues).thenCompose(this::putAllRandomTtl);
            }
            return CompletableFuture.completedFuture(fieldValues).thenCompose(this::putAllFixTtl);
        }
        return CompletableFuture.completedFuture(fieldValues).thenCompose(this::putAllUnlimitedTtl);
    }

    private CompletableFuture<Void> putAllRandomTtl(Map<? extends String, ? extends V> fieldValues) {
        List<byte[]> removeFields = new ArrayList<>();
        List<ExpiryKeyValue<byte[], byte[]>> storeFieldValues = new ArrayList<>();
        fieldValues.forEach((field, value) -> {
            byte[] storeField = this.hashStoreHelper.toStoreField(field);
            byte[] storeValue = this.convertor.toExtraStoreValue(value);
            if (storeValue == null) {
                removeFields.add(storeField);
            } else {
                storeFieldValues.add(ExpiryKeyValue.create(storeField, storeValue, randomTtl()));
            }
        });
        if (!removeFields.isEmpty()) {
            int size = removeFields.size();
            this.redisOperator.hdelAsync(hashKey, removeFields.toArray(new byte[size][]));
        }
        return this.redisOperator.hmpsetAsync(hashKey, storeFieldValues).thenApply(ignore -> null);
    }

    private CompletableFuture<Void> putAllFixTtl(Map<? extends String, ? extends V> fieldValues) {
        List<byte[]> removeFields = new ArrayList<>();
        List<KeyValue<byte[], byte[]>> storeFieldValues = new ArrayList<>();
        fieldValues.forEach((field, value) -> {
            byte[] storeField = this.hashStoreHelper.toStoreField(field);
            byte[] storeValue = this.convertor.toExtraStoreValue(value);
            if (storeValue == null) {
                removeFields.add(storeField);
            } else {
                storeFieldValues.add(KeyValue.create(storeField, storeValue));
            }
        });
        if (!removeFields.isEmpty()) {
            this.redisOperator.hdelAsync(hashKey, removeFields.toArray(new byte[0][]));
        }
        return this.redisOperator.hmpsetAsync(hashKey, expireAfterWrite, storeFieldValues).thenApply(ignore -> null);
    }

    private CompletableFuture<Void> putAllUnlimitedTtl(Map<? extends String, ? extends V> fieldValues) {
        List<byte[]> removeFields = new ArrayList<>();
        Map<byte[], byte[]> storeFieldValues = HashMap.newHashMap(fieldValues.size());
        fieldValues.forEach((field, value) -> {
            byte[] storeField = this.hashStoreHelper.toStoreField(field);
            byte[] storeValue = this.convertor.toExtraStoreValue(value);
            if (storeValue == null) {
                removeFields.add(storeField);
            } else {
                storeFieldValues.put(storeField, storeValue);
            }
        });
        if (!removeFields.isEmpty()) {
            this.redisOperator.hdelAsync(hashKey, removeFields.toArray(new byte[0][]));
        }
        return this.redisOperator.hmsetAsync(hashKey, storeFieldValues).thenApply(AbstractRedisStore::checkResult);
    }

    @Override
    public CompletableFuture<Void> removeAsync(String field) {
        return CompletableFuture.completedFuture(field)
                .thenApply(this.hashStoreHelper::toStoreField)
                .thenCompose(storeField -> this.redisOperator.hdelAsync(hashKey, storeField))
                .thenApply(ignore -> null);
    }

    @Override
    public CompletableFuture<Void> removeAllAsync(Set<? extends String> fields) {
        return CompletableFuture.completedFuture(fields)
                .thenApply(this::toStoreFields)
                .thenCompose(storeFields -> this.redisOperator.hdelAsync(hashKey, storeFields))
                .thenApply(ignore -> null);
    }

    @Override
    public void clear() {
        this.redisOperator.del(hashKey);
    }

    private byte[][] toStoreFields(Set<? extends String> fields) {
        int i = 0, size = fields.size();
        byte[][] storeFields = new byte[size][];
        for (String field : fields) {
            storeFields[i++] = this.hashStoreHelper.toStoreField(field);
        }
        return storeFields;
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