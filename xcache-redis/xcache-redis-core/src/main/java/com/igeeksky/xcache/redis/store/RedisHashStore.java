package com.igeeksky.xcache.redis.store;


import com.igeeksky.redis.CRC16;
import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.CacheKeyPrefix;
import com.igeeksky.xcache.core.ExtraStoreValueConvertor;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.function.tuple.KeyValue;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.util.*;

/**
 * <p><b>使用哈希表作为缓存</b></p>
 * <p>
 * 采用哈希表作为缓存，因为 Redis的限制，无法设置过期时间。
 *
 * <p> 哈希表名设计：</p>
 * <p> 1. 单点模式、主从模式、哨兵模式：使用缓存名称，即配置的 cache-name</p>
 * <p> 2. 集群模式：默认生成 16384 个哈希表，哈希表名格式为（cache-name::1, cache-name::2……）</p>
 * 这 16384 个哈希表将分散到集群的不同节点，设计目的是为了避免大量访问同一节点。
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-03
 */
public class RedisHashStore<V> implements RedisStore<V> {

    private static final int LENGTH = 16384;

    private final byte[] hashKey;

    private final byte[][] hashKeys;

    private final RedisOperator operator;

    private final StringCodec stringCodec;

    private final ExtraStoreValueConvertor<V> convertor;

    public RedisHashStore(RedisOperator operator, RedisStoreConfig<V> config) {
        this.operator = operator;
        this.stringCodec = StringCodec.getInstance(config.getCharset());
        if (config.isEnableGroupPrefix()) {
            this.hashKey = stringCodec.encode(config.getGroup() + ":" + config.getName());
        } else {
            this.hashKey = stringCodec.encode(config.getName());
        }
        CacheKeyPrefix cacheKeyPrefix = new CacheKeyPrefix(config.getGroup(), config.getName(),
                config.isEnableGroupPrefix(), stringCodec);
        this.hashKeys = (this.operator.isCluster()) ? initHashKeys(cacheKeyPrefix) : null;
        this.convertor = new ExtraStoreValueConvertor<>(config.isEnableNullValue(), config.isEnableCompressValue(),
                config.getValueCodec(), config.getValueCompressor());
    }

    @Override
    public CacheValue<V> getCacheValue(String field) {
        byte[] storeField = this.toStoreField(field);
        if (this.operator.isCluster()) {
            return this.convertor.fromExtraStoreValue(this.operator.hget(selectStoreKey(storeField), storeField));
        } else {
            return this.convertor.fromExtraStoreValue(this.operator.hget(hashKey, storeField));
        }
    }

    @Override
    public Map<String, CacheValue<V>> getAllCacheValues(Set<? extends String> fields) {
        int size = fields.size();
        if (this.operator.isCluster()) {
            Map<byte[], List<byte[]>> keyFields = toKeyFields(fields);
            return toResult(this.operator.hmget(keyFields, size));
        }

        byte[][] storeFields = new byte[size][];
        int i = 0;
        for (String field : fields) {
            storeFields[i++] = toStoreField(field);
        }
        return toResult(this.operator.hmget(hashKey, storeFields));
    }

    @Override
    public void put(String field, V value) {
        byte[] storeField = toStoreField(field);
        byte[] storeValue = this.convertor.toExtraStoreValue(value);
        if (storeValue == null) {
            return;
        }
        if (this.operator.isCluster()) {
            byte[] storeKey = selectStoreKey(storeField);
            this.operator.hset(storeKey, storeField, storeValue);
        } else {
            this.operator.hset(hashKey, storeField, storeValue);
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> keyValues) {
        if (this.operator.isCluster()) {

            int maximum = getMaximum(keyValues.size());
            Map<byte[], Map<byte[], byte[]>> keyFieldValues = Maps.newHashMap(maximum);

            keyValues.forEach((field, value) -> {
                byte[] storeField = toStoreField(field);
                // 根据 field 分配到不同的的哈希表
                byte[] storeKey = selectStoreKey(storeField);
                byte[] storeValue = this.convertor.toExtraStoreValue(value);
                if (storeValue != null) {
                    Map<byte[], byte[]> fieldMap = keyFieldValues.computeIfAbsent(storeKey, k -> new HashMap<>());
                    fieldMap.put(storeField, storeValue);
                }
            });

            checkResult(this.operator.hmset(keyFieldValues), "hmset");
            return;
        }

        Map<byte[], byte[]> fieldValues = Maps.newHashMap(keyValues.size());
        keyValues.forEach((field, value) -> {
            byte[] storeValue = this.convertor.toExtraStoreValue(value);
            if (storeValue != null) {
                fieldValues.put(toStoreField(field), storeValue);
            }
        });
        checkResult(this.operator.hmset(hashKey, fieldValues), "hmset");
    }


    @Override
    public void remove(String field) {
        byte[] storeField = toStoreField(field);
        if (this.operator.isCluster()) {
            this.operator.hdel(selectStoreKey(storeField), storeField);
            return;
        }
        this.operator.hdel(hashKey, storeField);
    }

    @Override
    public void removeAll(Set<? extends String> fields) {
        if (this.operator.isCluster()) {
            this.operator.hdel(toKeyFields(fields));
            return;
        }

        byte[][] storeFields = new byte[fields.size()][];
        int index = 0;
        for (String field : fields) {
            storeFields[index++] = toStoreField(field);
        }
        this.operator.hdel(hashKey, storeFields);
    }

    @Override
    public void clear() {
        if (this.operator.isCluster()) {
            this.operator.del(hashKeys);
        } else {
            this.operator.del(hashKey);
        }
    }

    /**
     * <p>初始化哈希表名</p>
     * <p/>
     * 仅集群模式时使用，可以将键和值分散到不同的节点。
     *
     * @return 16384 个哈希表名（group:cache-name:0, group:cache-name:1, ……, group:cache-name:16383）
     */
    private static byte[][] initHashKeys(CacheKeyPrefix cacheKeyPrefix) {
        byte[][] keys = new byte[LENGTH][];
        for (int i = 0; i < LENGTH; i++) {
            keys[i] = cacheKeyPrefix.createHashKey(i);
        }
        return keys;
    }

    /**
     * 根据 field 获取哈希表名称
     * <p>
     * 当处于集群模式时，为了避免只访问其中一个节点，因此将根据 field 选择不同的 hash key 来保存。
     * <p>
     * hash key 有 16384 个，key 由 cache-name + ":" + [0-16383] 组合而成。
     *
     * @param field redis hash field
     * @return redis hash key
     */
    private byte[] selectStoreKey(byte[] field) {
        return hashKeys[CRC16.crc16(field)];
    }

    private Map<String, CacheValue<V>> toResult(List<KeyValue<byte[], byte[]>> keyValues) {
        if (CollectionUtils.isEmpty(keyValues)) {
            return HashMap.newHashMap(0);
        }

        Map<String, CacheValue<V>> result = HashMap.newHashMap(keyValues.size());
        for (KeyValue<byte[], byte[]> keyValue : keyValues) {
            if (keyValue.hasValue()) {
                CacheValue<V> cacheValue = this.convertor.fromExtraStoreValue(keyValue.getValue());
                if (cacheValue != null) {
                    result.put(fromStoreField(keyValue.getKey()), cacheValue);
                }
            }
        }
        return result;
    }

    private Map<byte[], List<byte[]>> toKeyFields(Set<? extends String> fields) {
        int maximum = getMaximum(fields.size());
        Map<byte[], List<byte[]>> keyFields = Maps.newHashMap(maximum);
        for (String field : fields) {
            byte[] storeField = toStoreField(field);
            byte[] storeKey = selectStoreKey(storeField);
            List<byte[]> list = keyFields.computeIfAbsent(storeKey, k -> new ArrayList<>());
            list.add(storeField);
        }
        return keyFields;
    }

    private static int getMaximum(int size) {
        return Math.min(LENGTH, size / 2);
    }

    private byte[] toStoreField(String field) {
        return this.stringCodec.encode(field);
    }

    private String fromStoreField(byte[] field) {
        return this.stringCodec.decode(field);
    }

}