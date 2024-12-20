package com.igeeksky.xcache.redis.store;


import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.ExtraStoreConvertor;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 使用哈希表作为缓存（非集群模式）
 * <p>
 * <b>注意：</b><p>
 * HashStore 当前不支持设置过期时间。
 * <p>
 * 另：Redis 7.4.0+ 已支持设置 filed 存活时间，见：<a href="https://redis.io/docs/latest/commands/hpexpire/">hpexpire</a><br/>
 * 后续如 Lettuce 和 Jedis 客户端支持此命令，再行开发。
 *
 * @author Patrick.Lau
 * @since 0.0.3
 */
public class RedisHashStore<V> implements RedisStore<V> {

    private final byte[] hashKey;

    private final RedisOperator operator;

    private final ExtraStoreConvertor<V> convertor;

    private final RedisHashStoreHelper<V> hashStoreHelper;

    public RedisHashStore(RedisOperator operator, RedisStoreConfig<V> config) {
        this.operator = operator;
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
    public boolean contains(String field) {
        return this.operator.hexists(this.hashKey, this.hashStoreHelper.toStoreField(field));
    }

    @Override
    public CacheValue<V> getCacheValue(String field) {
        byte[] storeField = this.hashStoreHelper.toStoreField(field);
        byte[] storeValue = this.operator.hget(this.hashKey, storeField);
        return this.convertor.fromExtraStoreValue(storeValue);
    }

    @Override
    public Map<String, CacheValue<V>> getAllCacheValues(Set<? extends String> fields) {
        byte[][] storeFields = this.toStoreFields(fields);
        return this.hashStoreHelper.toResult(this.operator.hmget(this.hashKey, storeFields));
    }

    @Override
    public void put(String field, V value) {
        byte[] storeField = this.hashStoreHelper.toStoreField(field);
        byte[] storeValue = this.convertor.toExtraStoreValue(value);
        if (storeValue == null) {
            this.operator.hdel(hashKey, storeField);
        } else {
            this.operator.hset(hashKey, storeField, storeValue);
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> keyValues) {
        List<byte[]> removeFields = new ArrayList<>();
        Map<byte[], byte[]> fieldValues = Maps.newHashMap(keyValues.size());
        keyValues.forEach((field, value) -> {
            byte[] storeField = this.hashStoreHelper.toStoreField(field);
            byte[] storeValue = this.convertor.toExtraStoreValue(value);
            if (storeValue == null) {
                removeFields.add(storeField);
            } else {
                fieldValues.put(storeField, storeValue);
            }
        });
        if (!removeFields.isEmpty()) {
            this.operator.hdel(hashKey, removeFields.toArray(new byte[0][]));
        }
        checkResult(this.operator.hmset(hashKey, fieldValues), "hmset");
    }

    @Override
    public void remove(String field) {
        byte[] storeField = this.hashStoreHelper.toStoreField(field);
        this.operator.hdel(hashKey, storeField);
    }

    @Override
    public void removeAll(Set<? extends String> fields) {
        byte[][] storeFields = this.toStoreFields(fields);
        this.operator.hdel(hashKey, storeFields);
    }

    @Override
    public void clear() {
        this.operator.del(hashKey);
    }

    private byte[][] toStoreFields(Set<? extends String> fields) {
        int i = 0, size = fields.size();
        byte[][] storeFields = new byte[size][];
        for (String field : fields) {
            storeFields[i++] = this.hashStoreHelper.toStoreField(field);
        }
        return storeFields;
    }

}