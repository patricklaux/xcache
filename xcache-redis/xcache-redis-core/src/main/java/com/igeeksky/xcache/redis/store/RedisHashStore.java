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

    private final byte[] name;

    private final byte[][] redisHashKeys;

    private final RedisOperator connection;

    private final StringCodec stringCodec;

    private final ExtraStoreValueConvertor<V> convertor;

    public RedisHashStore(RedisOperator connection, RedisStoreConfig<V> config) {
        this.connection = connection;
        this.stringCodec = StringCodec.getInstance(config.getCharset());
        this.name = stringCodec.encode(config.getName());
        CacheKeyPrefix cacheKeyPrefix = new CacheKeyPrefix(config.getName(), stringCodec);
        this.redisHashKeys = (connection.isCluster()) ? initHashKeys(cacheKeyPrefix) : null;
        this.convertor = new ExtraStoreValueConvertor<>(config.isEnableNullValue(), config.isEnableCompressValue(),
                config.getValueCodec(), config.getValueCompressor());
    }

    @Override
    public CacheValue<V> get(String field) {
        byte[] storeField = this.toStoreField(field);
        if (connection.isCluster()) {
            return this.convertor.fromExtraStoreValue(connection.hget(selectStoreKey(storeField), storeField));
        } else {
            return this.convertor.fromExtraStoreValue(connection.hget(name, storeField));
        }
    }

    @Override
    public Map<String, CacheValue<V>> getAll(Set<? extends String> fields) {
        int size = fields.size();
        if (connection.isCluster()) {
            Map<byte[], List<byte[]>> keyFields = toKeyFields(fields);
            return toResult(connection.hmget(keyFields, size));
        }

        byte[][] storeFields = new byte[size][];
        int i = 0;
        for (String field : fields) {
            storeFields[i++] = toStoreField(field);
        }
        return toResult(connection.hmget(name, storeFields));
    }

    @Override
    public void put(String field, V value) {
        byte[] storeField = toStoreField(field);
        byte[] storeValue = this.convertor.toExtraStoreValue(value);
        if (storeValue == null) {
            return;
        }
        if (connection.isCluster()) {
            byte[] storeKey = selectStoreKey(storeField);
            connection.hset(storeKey, storeField, storeValue);
        } else {
            connection.hset(name, storeField, storeValue);
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> keyValues) {
        if (connection.isCluster()) {

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

            checkResult(connection.hmset(keyFieldValues), "hmset");
            return;
        }

        Map<byte[], byte[]> fieldValues = Maps.newHashMap(keyValues.size());
        keyValues.forEach((field, value) -> {
            byte[] storeValue = this.convertor.toExtraStoreValue(value);
            if (storeValue != null) {
                fieldValues.put(toStoreField(field), storeValue);
            }
        });
        checkResult(connection.hmset(name, fieldValues), "hmset");
    }


    @Override
    public void evict(String field) {
        byte[] storeField = toStoreField(field);
        if (connection.isCluster()) {
            connection.hdel(selectStoreKey(storeField), storeField);
            return;
        }
        connection.hdel(name, storeField);
    }

    @Override
    public void evictAll(Set<? extends String> fields) {
        if (connection.isCluster()) {
            connection.hdel(toKeyFields(fields));
            return;
        }

        byte[][] storeFields = new byte[fields.size()][];
        int index = 0;
        for (String field : fields) {
            storeFields[index++] = toStoreField(field);
        }
        connection.hdel(name, storeFields);
    }

    @Override
    public void clear() {
        if (redisHashKeys != null) {
            connection.del(redisHashKeys);
        } else {
            connection.del(name);
        }
    }

    /**
     * <p>初始化哈希表名</p>
     * <p/>
     * 仅集群模式时使用，可以将键和值分散到不同的节点。
     *
     * @return 16384个哈希表名（cache-name:0, cache-name:1, ……, cache-name:16383）
     */
    private static byte[][] initHashKeys(CacheKeyPrefix cacheKeyPrefix) {
        int len = 16384;
        byte[][] keys = new byte[len][];
        for (int i = 0; i < len; i++) {
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
        return redisHashKeys[CRC16.crc16(field)];
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
        Map<byte[], List<byte[]>> keyFields = HashMap.newHashMap(maximum);
        for (String field : fields) {
            byte[] storeField = toStoreField(field);
            byte[] storeKey = selectStoreKey(storeField);
            List<byte[]> list = keyFields.computeIfAbsent(storeKey, k -> new ArrayList<>());
            list.add(storeField);
        }
        return keyFields;
    }

    private static int getMaximum(int size) {
        if (size > 32768) {
            return 16384;
        } else {
            return size / 2;
        }
    }

    private byte[] toStoreField(String field) {
        return stringCodec.encode(field);
    }

    private String fromStoreField(byte[] field) {
        return stringCodec.decode(field);
    }

}