package com.igeeksky.xcache.redis;


import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.CacheValues;
import com.igeeksky.xcache.common.KeyValue;

import com.igeeksky.xcache.core.CacheKeyPrefix;
import com.igeeksky.xcache.core.config.CacheConfig;
import com.igeeksky.xcache.core.store.RemoteStore;
import com.igeeksky.xcache.extension.serializer.StringSerializer;

import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;

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
public class RedisHashStore implements RemoteStore {

    public static final String STORE_NAME = "redis-hash";

    private final byte[] name;

    private final byte[][] redisHashKeys;

    private final StringSerializer serializer;

    private final RedisConnection connection;

    private final CacheKeyPrefix cacheKeyPrefix;

    public RedisHashStore(CacheConfig<?, ?> config, StringSerializer serializer, RedisConnection connection) {
        this.serializer = serializer;
        this.connection = connection;
        this.name = serializer.serialize(config.getName());
        this.cacheKeyPrefix = new CacheKeyPrefix(config.getName(), serializer);
        this.redisHashKeys = (connection.isCluster()) ? initHashFields() : null;
    }

    /**
     * <p>初始化哈希表名</p>
     * <p/>
     * 仅集群模式时使用，可以将键和值分散到不同的节点。
     *
     * @return 16384个哈希表名（cache-name::1, cache-name::2……）
     */
    private byte[][] initHashFields() {
        int len = 16384;
        byte[][] keys = new byte[len][];
        for (int i = 0; i < len; i++) {
            keys[i] = cacheKeyPrefix.createHashKey(i);
        }
        return keys;
    }

    @Override
    public CacheValue<byte[]> get(String key) {
        byte[] field = this.toStoreField(key);
        byte[] value;
        if (connection.isCluster()) {
            value = connection.hget(selectHashKey(field), field);
        } else {
            value = connection.hget(name, field);
        }
        if (value != null) {
            return CacheValues.newCacheValue(value);
        }
        return null;
    }

    @Override
    public Map<String, CacheValue<byte[]>> getAll(Set<? extends String> keys) {
        Map<String, CacheValue<byte[]>> result = Maps.newLinkedHashMap(keys.size());

        if (connection.isCluster()) {
            Map<byte[], List<byte[]>> keyListMap = toKeyListMap(keys);
            for (Map.Entry<byte[], List<byte[]>> e : keyListMap.entrySet()) {
                byte[] hashKey = e.getKey();
                List<byte[]> fields = e.getValue();
                getAndAddToResult(result, hashKey, fields);
            }
            return result;
        }

        List<byte[]> fields = new ArrayList<>(keys.size());
        keys.forEach(k -> fields.add(toStoreField(k)));
        getAndAddToResult(result, name, fields);

        return result;
    }

    private void getAndAddToResult(Map<String, CacheValue<byte[]>> result, byte[] hashKey, List<byte[]> fields) {
        int size = fields.size();
        List<KeyValue<byte[], byte[]>> keyValues = connection.hmget(hashKey, fields.toArray(new byte[size][]));

        if (CollectionUtils.isNotEmpty(keyValues)) {
            for (KeyValue<byte[], byte[]> keyValue : keyValues) {
                if (keyValue.hasValue()) {
                    result.put(fromStoreField(keyValue.getKey()), CacheValues.newCacheValue(keyValue.getValue()));
                }
            }
        }
    }

    private Map<byte[], List<byte[]>> toKeyListMap(Set<? extends String> fields) {
        int maximum = Math.min(16384, fields.size());
        Map<byte[], List<byte[]>> keyListMap = Maps.newHashMap(maximum);
        for (String fieldStr : fields) {
            byte[] field = toStoreField(fieldStr);
            byte[] hashKey = selectHashKey(field);
            List<byte[]> list = keyListMap.computeIfAbsent(hashKey, hk -> new ArrayList<>());
            list.add(field);
        }
        return keyListMap;
    }

    @Override
    public void put(String fieldStr, byte[] value) {
        byte[] field = toStoreField(fieldStr);
        if (connection.isCluster()) {
            connection.hset(selectHashKey(field), field, value);
        } else {
            connection.hset(name, field, value);
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends byte[]> keyValues) {
        if (connection.isCluster()) {
            int maximum = Math.min(16384, keyValues.size());
            Map<byte[], Map<byte[], byte[]>> keyMap = new HashMap<>(maximum);
            for (Map.Entry<? extends String, ? extends byte[]> entry : keyValues.entrySet()) {
                // 根据 field 分配到不同的哈希表
                byte[] field = toStoreField(entry.getKey());
                byte[] hashKey = selectHashKey(field);
                byte[] value = entry.getValue();
                Map<byte[], byte[]> fieldMap = keyMap.computeIfAbsent(hashKey, k -> new HashMap<>());
                fieldMap.put(field, value);
            }

            keyMap.forEach(connection::hmset);
            return;
        }

        Map<byte[], byte[]> fieldMap = Maps.newHashMap(keyValues.size());
        keyValues.forEach((field, value) -> fieldMap.put(toStoreField(field), value));
        connection.hmset(name, fieldMap);
    }

    @Override
    public void evict(String fieldStr) {
        byte[] field = toStoreField(fieldStr);
        if (connection.isCluster()) {
            connection.hdel(selectHashKey(field), field);
            return;
        }
        connection.hdel(name, field);
    }

    @Override
    public void evictAll(Set<? extends String> fieldStrings) {
        if (connection.isCluster()) {
            Map<byte[], List<byte[]>> keyListMap = toKeyListMap(fieldStrings);
            keyListMap.forEach((key, fields) -> connection.hdel(key, fields.toArray(new byte[fields.size()][])));
            return;
        }

        byte[][] fields = new byte[fieldStrings.size()][];
        int index = 0;
        for (String field : fieldStrings) {
            fields[index++] = toStoreField(field);
        }
        connection.hdel(name, fields);
    }

    @Override
    public String getStoreName() {
        return STORE_NAME;
    }

    private String fromStoreField(byte[] field) {
        return serializer.deserialize(field);
    }

    private byte[] toStoreField(String field) {
        return serializer.serialize(field);
    }

    /**
     * 根据 field 获取哈希表名称
     *
     * @param field 键
     * @return 哈希表名
     */
    private byte[] selectHashKey(byte[] field) {
        return redisHashKeys[CRC16.crc16(field)];
    }

    @Override
    public void clear() {
        if (redisHashKeys != null) {
            connection.del(redisHashKeys);
        } else {
            connection.del(name);
        }
    }
}