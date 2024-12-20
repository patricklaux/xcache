package com.igeeksky.xcache.redis.store;


import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.ExtraStoreConvertor;
import com.igeeksky.xcache.redis.RedisClusterHelper;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.util.*;

/**
 * 使用哈希表作为缓存（集群模式）
 * <p>
 * 为了避免数据倾斜，默认生成 32 个哈希表，这 32 个哈希表将分散到集群的不同节点。
 * 哈希表名格式为（cache-name:1, cache-name:2……）
 * <p>
 * <b>注意：</b><p>
 * HashStore 当前不支持设置过期时间。
 * <p>
 * 另：Redis 7.4.0+ 已支持设置 filed 存活时间，见：<a href="https://redis.io/docs/latest/commands/hpexpire/">hpexpire</a><br/>
 * 后续如 Lettuce 和 Jedis 客户端支持此命令，再行开发。
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class RedisClusterHashStore<V> implements RedisStore<V> {

    private final RedisOperator operator;

    private final RedisClusterHelper clusterHelper;
    private final RedisHashStoreHelper<V> hashStoreHelper;

    private final ExtraStoreConvertor<V> convertor;

    public RedisClusterHashStore(RedisOperator operator, RedisStoreConfig<V> config) {
        this.operator = operator;
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
    public boolean contains(String field) {
        byte[] storeField = this.hashStoreHelper.toStoreField(field);
        byte[] storeKey = this.clusterHelper.selectKey(storeField);
        return this.operator.hexists(storeKey, storeField);
    }

    @Override
    public CacheValue<V> getCacheValue(String field) {
        byte[] storeField = this.hashStoreHelper.toStoreField(field);
        byte[] storeKey = this.clusterHelper.selectKey(storeField);
        return this.convertor.fromExtraStoreValue(this.operator.hget(storeKey, storeField));
    }

    @Override
    public Map<String, CacheValue<V>> getAllCacheValues(Set<? extends String> fields) {
        int size = fields.size();
        Map<byte[], List<byte[]>> keyFields = this.toKeyFields(fields);
        return this.hashStoreHelper.toResult(this.operator.hmget(keyFields, size));
    }

    @Override
    public void put(String field, V value) {
        byte[] storeField = this.hashStoreHelper.toStoreField(field);
        byte[] storeValue = this.convertor.toExtraStoreValue(value);
        byte[] storeKey = this.clusterHelper.selectKey(storeField);
        if (storeValue == null) {
            this.operator.hdel(storeKey, storeField);
        } else {
            this.operator.hset(storeKey, storeField, storeValue);
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> keyValues) {
        int maximum = this.clusterHelper.calculateCapacity(keyValues.size());
        Map<byte[], List<byte[]>> removeKeyFields = Maps.newHashMap();
        Map<byte[], Map<byte[], byte[]>> keyFieldValues = Maps.newHashMap(maximum);

        keyValues.forEach((field, value) -> {
            byte[] storeField = this.hashStoreHelper.toStoreField(field);
            // 根据 field 分配到不同的的哈希表
            byte[] storeKey = this.clusterHelper.selectKey(storeField);
            byte[] storeValue = this.convertor.toExtraStoreValue(value);
            if (storeValue == null) {
                removeKeyFields.computeIfAbsent(storeKey, k -> new ArrayList<>()).add(storeField);
            } else {
                keyFieldValues.computeIfAbsent(storeKey, k -> new HashMap<>()).put(storeField, storeValue);
            }
        });
        if (!removeKeyFields.isEmpty()) {
            this.operator.hdel(removeKeyFields);
        }
        checkResult(this.operator.hmset(keyFieldValues), "hmset");
    }


    @Override
    public void remove(String field) {
        byte[] storeField = this.hashStoreHelper.toStoreField(field);
        byte[] storeKey = this.clusterHelper.selectKey(storeField);
        this.operator.hdel(storeKey, storeField);
    }

    @Override
    public void removeAll(Set<? extends String> fields) {
        Map<byte[], List<byte[]>> keyFields = this.toKeyFields(fields);
        this.operator.hdel(keyFields);
    }

    @Override
    public void clear() {
        byte[][] hashKeys = this.clusterHelper.getKeys();
        this.operator.del(hashKeys);
    }

    private Map<byte[], List<byte[]>> toKeyFields(Set<? extends String> fields) {
        int maximum = this.clusterHelper.calculateCapacity(fields.size());
        Map<byte[], List<byte[]>> keyFields = Maps.newHashMap(maximum);
        for (String field : fields) {
            byte[] storeField = this.hashStoreHelper.toStoreField(field);
            byte[] storeKey = this.clusterHelper.selectKey(storeField);
            List<byte[]> list = keyFields.computeIfAbsent(storeKey, k -> new ArrayList<>());
            list.add(storeField);
        }
        return keyFields;
    }

}