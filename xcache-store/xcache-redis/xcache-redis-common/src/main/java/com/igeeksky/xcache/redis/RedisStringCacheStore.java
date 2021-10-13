package com.igeeksky.xcache.redis;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.ExpiryKeyValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.config.CacheProperties;
import com.igeeksky.xcache.config.PropertiesKey;
import com.igeeksky.xcache.extension.compress.Compressor;
import com.igeeksky.xcache.extension.monitor.CacheMonitor;
import com.igeeksky.xcache.extension.serialization.Serializer;
import com.igeeksky.xcache.store.AbstractRemoteCacheStore;
import com.igeeksky.xcache.store.CacheKeyPrefix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-03
 */
public class RedisStringCacheStore<K, V> extends AbstractRemoteCacheStore<K, V> {

    public static final String STORE_TYPE = "redis-string";

    private static final Logger logger = LoggerFactory.getLogger(RedisStringCacheStore.class);

    private final boolean useKeyPrefix;
    private final CacheKeyPrefix<K> cacheKeyPrefix;
    private final RedisStringWriter redisWriter;
    private final Serializer<K> keySerializer;
    private final long expireAfterWriteMills;

    public RedisStringCacheStore(String name, CacheProperties.Redis redis, Class<K> keyType, Class<V> valueType,
                                 List<CacheMonitor<K, V>> cacheMonitors, RedisStringWriter redisWriter,
                                 Serializer<K> keySerializer, Serializer<V> valueSerializer, Compressor compressor) {
        super(name, redis, keyType, valueType, cacheMonitors, valueSerializer, compressor);

        this.redisWriter = redisWriter;
        this.keySerializer = keySerializer;

        this.useKeyPrefix = redis.isEnableKeyPrefix();
        this.cacheKeyPrefix = new CacheKeyPrefix<>(keySerializer, redis.getCharset(), redis.getNamespace(), getName());

        this.expireAfterWriteMills = PropertiesKey.getLong(redis.getMetadata(), PropertiesKey.METADATA_EXPIRE_AFTER_WRITE, PropertiesKey.UN_SET);
    }

    @Override
    public String getStoreType() {
        return STORE_TYPE;
    }

    @Override
    protected Mono<CacheValue<V>> doStoreGet(byte[] keyBytes) {
        return redisWriter.get(keyBytes).map(this::fromStoreValue);
    }

    @Override
    protected Flux<KeyValue<K, CacheValue<V>>> doStoreGetAll(byte[][] keys) {
        return redisWriter.mget(keys)
                .map(kv -> new KeyValue<>(fromStoreKey(kv.getKey()), fromStoreValue(kv.getValue())));
    }

    @Override
    protected Mono<Void> doStorePut(byte[] key, byte[] value) {
        if (expireAfterWriteMills <= 0) {
            return redisWriter.set(key, value);
        }
        return redisWriter.psetex(key, expireAfterWriteMills, value);
    }

    @Override
    protected Mono<Void> doStorePutAll(Map<byte[], byte[]> keyValues) {
        if (expireAfterWriteMills <= 0) {
            return redisWriter.mset(keyValues);
        }
        List<ExpiryKeyValue<byte[], byte[]>> expiryKeyValueList = new ArrayList<>();
        for (Map.Entry<byte[], byte[]> entry : keyValues.entrySet()) {
            byte[] k = entry.getKey();
            byte[] v = entry.getValue();
            expiryKeyValueList.add(new ExpiryKeyValue<>(k, v, Duration.ofMillis(expireAfterWriteMills)));
        }
        return redisWriter.mpsetex(expiryKeyValueList);
    }

    @Override
    protected Mono<Void> doStoreRemove(byte[] key) {
        return redisWriter.del(key).then();
    }

    @Override
    protected Mono<Void> doStoreRemoveAll(byte[][] keys) {
        return redisWriter.del(keys).then();
    }

    @Override
    protected Mono<Void> doClear() {
        return Mono.fromSupplier(() -> {
            if (logger.isDebugEnabled()) {
                logger.debug("The RedisStringCache does not support clear operation");
            }
            return null;
        });
    }

    @Override
    protected byte[] toStoreKey(K key) {
        if (useKeyPrefix) {
            return cacheKeyPrefix.concatPrefixBytes(key);
        }
        return keySerializer.serialize(key);
    }

    @Override
    protected K fromStoreKey(byte[] key) {
        if (useKeyPrefix) {
            return cacheKeyPrefix.removePrefix(key);
        }
        return keySerializer.deserialize(key);
    }
}
