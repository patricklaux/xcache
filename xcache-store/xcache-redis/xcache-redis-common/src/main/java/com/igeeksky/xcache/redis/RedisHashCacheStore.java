package com.igeeksky.xcache.redis;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.common.annotation.NotNull;
import com.igeeksky.xcache.config.CacheProperties;
import com.igeeksky.xcache.extension.compress.Compressor;
import com.igeeksky.xcache.extension.monitor.CacheMonitor;
import com.igeeksky.xcache.extension.serialization.Serializer;
import com.igeeksky.xcache.store.AbstractRemoteCacheStore;
import com.igeeksky.xcache.store.CacheKeyPrefix;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.*;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-03
 */
public class RedisHashCacheStore<K, V> extends AbstractRemoteCacheStore<K, V> {

    public static final String STORE_TYPE = "redis-hash";

    private final RedisHashWriter redisHashWriter;
    private final byte[][] redisHashKeys;
    private final Serializer<K> keySerializer;

    public RedisHashCacheStore(String name, CacheProperties.Redis redis, Class<K> keyType, Class<V> valueType,
                               List<CacheMonitor<K, V>> cacheMonitors, RedisHashWriter redisHashWriter,
                               Serializer<K> keySerializer, Serializer<V> valueSerializer, Compressor compressor) {
        super(name, redis, keyType, valueType, cacheMonitors, valueSerializer, compressor);
        this.redisHashWriter = redisHashWriter;
        this.keySerializer = keySerializer;
        this.redisHashKeys = initHashKeys(createCacheKeyPrefix(redis, keySerializer));
    }

    @NotNull
    private CacheKeyPrefix<K> createCacheKeyPrefix(CacheProperties.Redis redis, Serializer<K> keySerializer) {
        Charset charset = redis.getCharset();
        String namespace = redis.getNamespace();
        return new CacheKeyPrefix<>(keySerializer, charset, namespace, getName());
    }

    @Override
    public String getStoreType() {
        return STORE_TYPE;
    }

    @Override
    protected Mono<CacheValue<V>> doStoreGet(byte[] field) {
        byte[] hashKey = selectHashKey(field);
        return redisHashWriter.hget(hashKey, field).map(this::fromStoreValue);
    }

    private byte[][] initHashKeys(CacheKeyPrefix<K> cacheKeyPrefix) {
        int length = 16384;
        byte[][] keys = new byte[length][];
        for (int i = 0; i < length; i++) {
            keys[i] = cacheKeyPrefix.createHashKey(i);
        }
        return keys;
    }

    private byte[] selectHashKey(byte[] field) {
        int index = CRC16.crc16(field);
        return redisHashKeys[index];
    }

    @Override
    protected Flux<KeyValue<K, CacheValue<V>>> doStoreGetAll(byte[][] fields) {
        int size = (fields.length > redisHashKeys.length) ? redisHashKeys.length : fields.length / 2;
        Map<byte[], List<byte[]>> hashKeyMap = new HashMap<>(size);
        for (byte[] field : fields) {
            byte[] hashKey = selectHashKey(field);
            List<byte[]> list = hashKeyMap.computeIfAbsent(hashKey, k -> new ArrayList<>());
            list.add(field);
        }

        Flux<KeyValue<byte[], byte[]>> result = Flux.empty();
        hashKeyMap.forEach((hashKey, list) -> {
            byte[][] fieldArray = list.toArray(new byte[list.size()][]);
            result.mergeWith(redisHashWriter.hmget(hashKey, fieldArray));
        });
        return result.map(kv -> new KeyValue<>(fromStoreKey(kv.getKey()), fromStoreValue(kv.getValue())))
                .filter(KeyValue::hasValue);
    }

    @Override
    protected Mono<Void> doStorePut(byte[] field, byte[] hashValue) {
        return redisHashWriter.hset(selectHashKey(field), field, hashValue).then();
    }

    @Override
    protected Mono<Void> doStorePutAll(Map<byte[], byte[]> keyValues) {
        Map<byte[], Map<byte[], byte[]>> hashKeyMap = new HashMap<>(keyValues.size());
        for (Map.Entry<byte[], byte[]> entry : keyValues.entrySet()) {
            byte[] field = entry.getKey();
            byte[] hashKey = selectHashKey(field);
            byte[] value = entry.getValue();
            Map<byte[], byte[]> splitMap = hashKeyMap.computeIfAbsent(hashKey, k -> new HashMap<>());
            splitMap.put(field, value);
        }

        Flux<Void> result = Flux.empty();
        Set<Map.Entry<byte[], Map<byte[], byte[]>>> set = hashKeyMap.entrySet();
        for (Map.Entry<byte[], Map<byte[], byte[]>> entry : set) {
            result = result.mergeWith(redisHashWriter.hmset(entry.getKey(), entry.getValue()));
        }

        return result.then();
    }

    @Override
    protected Mono<Void> doStoreRemove(byte[] field) {
        return redisHashWriter.hdel(selectHashKey(field), field).then();
    }

    @Override
    protected Mono<Void> doStoreRemoveAll(byte[][] fields) {
        Map<byte[], List<byte[]>> keyListMap = new HashMap<>();
        for (byte[] field : fields) {
            byte[] hashKey = selectHashKey(field);
            List<byte[]> list = keyListMap.computeIfAbsent(hashKey, key -> new ArrayList<>());
            list.add(field);
        }

        Flux<Long> flux = Flux.empty();
        Set<Map.Entry<byte[], List<byte[]>>> entrySet = keyListMap.entrySet();
        for (Map.Entry<byte[], List<byte[]>> entry : entrySet) {
            byte[] hashKey = entry.getKey();
            List<byte[]> list = entry.getValue();
            Mono<Long> mono = redisHashWriter.hdel(hashKey, list.toArray(new byte[list.size()][]));
            flux = flux.concatWith(mono);
        }

        return flux.then();
    }

    @Override
    protected Mono<Void> doClear() {
        return redisHashWriter.del(redisHashKeys).then();
    }

    @Override
    protected byte[] toStoreKey(K key) {
        return keySerializer.serialize(key);
    }

    @Override
    protected K fromStoreKey(byte[] key) {
        return keySerializer.deserialize(key);
    }
}
