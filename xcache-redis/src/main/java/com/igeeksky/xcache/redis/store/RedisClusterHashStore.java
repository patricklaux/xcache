package com.igeeksky.xcache.redis.store;


import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.ExtraStoreConvertor;
import com.igeeksky.xcache.redis.RedisClusterHelper;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xtool.core.ExpiryKeyValue;
import com.igeeksky.xtool.core.KeyValue;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.RandomUtils;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 使用哈希表作为缓存（集群模式）
 * <p>
 * 为了避免数据倾斜，默认生成 32 个哈希表，这 32 个哈希表将分散到集群的不同节点。
 * 哈希表名格式为（cache-name:1, cache-name:2……）
 * <p>
 * <b>注意：</b><br>
 * HashStore 支持设置过期时间，但 RedisServer 的版本必须为 7.4.0+。<br>
 * 见：<a href="https://redis.io/docs/latest/commands/hpexpire/">hpexpire</a><br/>
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class RedisClusterHashStore<V> extends RedisStore<V> {

    private final boolean enableRandomTtl;
    private final long expireAfterWrite;
    private final long expireAfterWriteMin;

    private final RedisOperatorProxy operator;
    private final ExtraStoreConvertor<V> convertor;
    private final RedisClusterHelper clusterHelper;
    private final RedisHashStoreHelper<V> hashStoreHelper;

    public RedisClusterHashStore(RedisOperatorProxy operator, RedisStoreConfig<V> config) {
        super(operator.getTimeout());
        this.operator = operator;
        boolean enableRandomTtl = config.isEnableRandomTtl();
        this.expireAfterWrite = config.getExpireAfterWrite();
        this.expireAfterWriteMin = expireAfterWrite * 4 / 5;
        this.enableRandomTtl = enableRandomTtl && (expireAfterWrite - expireAfterWriteMin) > 1;

        RedisHashStoreHelper.checkServerVersion(this.operator, this.expireAfterWrite);

        String hashKey = config.getName();
        if (config.isEnableGroupPrefix()) {
            hashKey = config.getGroup() + ":" + config.getName();
        }
        StringCodec stringCodec = StringCodec.getInstance(config.getCharset());
        this.convertor = new ExtraStoreConvertor<>(config.isEnableNullValue(), config.isEnableCompressValue(),
                config.getValueCodec(), config.getValueCompressor());
        this.clusterHelper = new RedisClusterHelper(config.getKeySequenceSize(), hashKey, stringCodec);
        this.hashStoreHelper = new RedisHashStoreHelper<>(stringCodec, this.convertor::fromExtraStoreValue);
    }

    @Override
    public CompletableFuture<CacheValue<V>> getCacheValueAsync(String field) {
        byte[] storeField = this.hashStoreHelper.toStoreField(field);
        byte[] storeKey = this.clusterHelper.selectKey(storeField);
        return this.operator.hgetAsync(storeKey, storeField).thenApply(this.convertor::fromExtraStoreValue);
    }

    @Override
    public CompletableFuture<Map<String, CacheValue<V>>> getAllCacheValuesAsync(Set<? extends String> fields) {
        Map<byte[], List<byte[]>> keyFields = this.toKeyFields(fields);
        return this.operator.hmgetAsync(keyFields).thenApply(this.hashStoreHelper::toResult);
    }

    @Override
    public CompletableFuture<Void> putAsync(String field, V value) {
        byte[] storeField = this.hashStoreHelper.toStoreField(field);
        byte[] storeValue = this.convertor.toExtraStoreValue(value);
        byte[] storeKey = this.clusterHelper.selectKey(storeField);
        if (storeValue == null) {
            return this.operator.hdelAsync(storeKey, storeField).thenApply(ignore -> null);
        }
        if (this.expireAfterWrite > 0) {
            long ttl = (this.enableRandomTtl) ? randomTtl() : expireAfterWrite;
            return this.operator.hpsetAsync(storeKey, ttl, storeField, storeValue).thenApply(ignore -> null);
        }
        return this.operator.hsetAsync(storeKey, storeField, storeValue).thenApply(ignore -> null);
    }

    @Override
    public CompletableFuture<Void> putAllAsync(Map<? extends String, ? extends V> keyValues) {
        if (expireAfterWrite > 0) {
            if (this.enableRandomTtl) {
                return this.putAllRandomTtl(keyValues);
            }
            return this.putAllFixTtl(keyValues);
        }
        return this.putAllUnlimitedTtl(keyValues);
    }

    private CompletableFuture<Void> putAllRandomTtl(Map<? extends String, ? extends V> fieldValues) {
        Map<byte[], List<byte[]>> removeKeyFields = Maps.newHashMap();
        Map<byte[], List<ExpiryKeyValue<byte[], byte[]>>> expiryKeysFieldsValues = Maps.newHashMap();
        fieldValues.forEach((field, value) -> {
            byte[] storeField = this.hashStoreHelper.toStoreField(field);
            byte[] storeKey = this.clusterHelper.selectKey(storeField);
            byte[] storeValue = this.convertor.toExtraStoreValue(value);
            if (storeValue == null) {
                removeKeyFields.computeIfAbsent(storeKey, k -> new ArrayList<>()).add(storeField);
            } else {
                ExpiryKeyValue<byte[], byte[]> fieldValue = ExpiryKeyValue.create(storeField, storeValue, randomTtl());
                expiryKeysFieldsValues.computeIfAbsent(storeKey, k -> new ArrayList<>()).add(fieldValue);
            }
        });
        if (!removeKeyFields.isEmpty()) {
            this.operator.hdelAsync(removeKeyFields);
        }
        return this.operator.hmpsetAsync(expiryKeysFieldsValues).thenApply(ignore -> null);
    }

    private CompletableFuture<Void> putAllFixTtl(Map<? extends String, ? extends V> fieldValues) {
        Map<byte[], List<byte[]>> removeKeyFields = Maps.newHashMap();
        Map<byte[], List<KeyValue<byte[], byte[]>>> keyFieldValues = HashMap.newHashMap(fieldValues.size());
        fieldValues.forEach((field, value) -> {
            byte[] storeField = this.hashStoreHelper.toStoreField(field);
            byte[] storeKey = this.clusterHelper.selectKey(storeField);
            byte[] storeValue = this.convertor.toExtraStoreValue(value);
            if (storeValue == null) {
                removeKeyFields.computeIfAbsent(storeKey, k -> new ArrayList<>()).add(storeField);
            } else {
                KeyValue<byte[], byte[]> fieldValue = KeyValue.create(storeField, storeValue);
                keyFieldValues.computeIfAbsent(storeKey, k -> new ArrayList<>()).add(fieldValue);
            }
        });
        if (!removeKeyFields.isEmpty()) {
            this.operator.hdelAsync(removeKeyFields);
        }
        return this.operator.hmpsetAsync(keyFieldValues, expireAfterWrite).thenApply(ignore -> null);
    }

    private CompletableFuture<Void> putAllUnlimitedTtl(Map<? extends String, ? extends V> keyValues) {
        int maximum = this.clusterHelper.calculateCapacity(keyValues.size());
        Map<byte[], List<byte[]>> removeKeyFields = Maps.newHashMap();
        Map<byte[], Map<byte[], byte[]>> keyFieldValues = HashMap.newHashMap(maximum);

        keyValues.forEach((field, value) -> {
            byte[] storeField = this.hashStoreHelper.toStoreField(field);
            byte[] storeKey = this.clusterHelper.selectKey(storeField);
            byte[] storeValue = this.convertor.toExtraStoreValue(value);
            if (storeValue == null) {
                removeKeyFields.computeIfAbsent(storeKey, k -> new ArrayList<>()).add(storeField);
            } else {
                keyFieldValues.computeIfAbsent(storeKey, k -> new HashMap<>()).put(storeField, storeValue);
            }
        });

        if (!removeKeyFields.isEmpty()) {
            this.operator.hdelAsync(removeKeyFields);
        }

        return this.operator.hmsetAsync(keyFieldValues).thenApply(RedisStore::checkResult);
    }

    @Override
    public CompletableFuture<Void> removeAsync(String field) {
        byte[] storeField = this.hashStoreHelper.toStoreField(field);
        byte[] storeKey = this.clusterHelper.selectKey(storeField);
        return this.operator.hdelAsync(storeKey, storeField).thenApply(ignore -> null);
    }

    @Override
    public CompletableFuture<Void> removeAllAsync(Set<? extends String> fields) {
        return this.operator.hdelAsync(this.toKeyFields(fields)).thenApply(ignore -> null);
    }

    @Override
    public void clear() {
        byte[][] hashKeys = this.clusterHelper.getKeys();
        this.operator.del(hashKeys);
    }

    private Map<byte[], List<byte[]>> toKeyFields(Set<? extends String> fields) {
        int maximum = this.clusterHelper.calculateCapacity(fields.size());
        Map<byte[], List<byte[]>> keyFields = HashMap.newHashMap(maximum);
        for (String field : fields) {
            byte[] storeField = this.hashStoreHelper.toStoreField(field);
            byte[] storeKey = this.clusterHelper.selectKey(storeField);
            keyFields.computeIfAbsent(storeKey, k -> new ArrayList<>()).add(storeField);
        }
        return keyFields;
    }

    /**
     * 随机存活时间
     *
     * @return 返回随机生成的存活时间
     */
    private long randomTtl() {
        return RandomUtils.nextLong(expireAfterWriteMin, expireAfterWrite);
    }

}