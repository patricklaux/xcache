package com.igeeksky.xcache.store;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.CacheValues;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.common.NullValue;
import com.igeeksky.xcache.config.CacheProperties;
import com.igeeksky.xcache.extension.compress.Compressor;
import com.igeeksky.xcache.extension.monitor.CacheMonitor;
import com.igeeksky.xcache.extension.serialization.Serializer;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.lang.ArrayUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.util.*;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-22
 */
public abstract class AbstractRemoteCacheStore<K, V> extends AbstractCacheStore<K, V> {

    private final boolean enableCompressValue;
    private final Compressor valueCompressor;
    protected final Serializer<V> valueSerializer;

    public AbstractRemoteCacheStore(String name, CacheProperties.Remote remote, Class<K> keyType, Class<V> valueType,
                                    List<CacheMonitor<K, V>> cacheMonitors,
                                    Serializer<V> valueSerializer, Compressor valueCompressor) {
        super(name, remote, keyType, valueType, cacheMonitors);
        this.valueSerializer = valueSerializer;
        this.enableCompressValue = remote.getEnableCompressValue();
        this.valueCompressor = valueCompressor;
    }

    @Override
    protected Mono<CacheValue<V>> doGet(K key) {
        byte[] keyBytes = toStoreKey(key);
        if (ArrayUtils.isNotEmpty(keyBytes)) {
            return doStoreGet(keyBytes);
        }
        return Mono.error(new RuntimeException("Key bytes must not be null or empty."));
    }

    protected abstract Mono<CacheValue<V>> doStoreGet(byte[] keyBytes);

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> doGetAll(Set<? extends K> keys) {
        byte[][] keyArray = new byte[keys.size()][];
        int i = 0;
        for (K key : keys) {
            keyArray[i] = toStoreKey(key);
            i++;
        }
        return this.doStoreGetAll(keyArray);
    }

    protected abstract Flux<KeyValue<K, CacheValue<V>>> doStoreGetAll(byte[][] keys);

    @Override
    public Mono<Void> doPut(K key, V value) {
        return Mono.just(Tuples.of(key, value))
                .map(kv -> kv.mapT1(this::toStoreKey).mapT2(this::toStoreValue))
                .flatMap(kv -> doStorePut(kv.getT1(), kv.getT2()));
    }

    protected abstract Mono<Void> doStorePut(byte[] key, byte[] value);

    @Override
    public Mono<Void> doPutAll(Map<? extends K, ? extends V> keyValues) {
        Map<byte[], byte[]> keyValuesMap = new HashMap<>(keyValues.size());
        for (Map.Entry<? extends K, ? extends V> entry : keyValues.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            byte[] keyBytes = toStoreKey(key);
            byte[] valueBytes = toStoreValue(value);
            if (null != keyBytes && null != valueBytes) {
                keyValuesMap.put(keyBytes, valueBytes);
            }
        }
        return this.doStorePutAll(keyValuesMap);
    }

    protected abstract Mono<Void> doStorePutAll(Map<byte[], byte[]> keyValues);

    @Override
    public Mono<Void> doRemove(K key) {
        return Mono.just(key)
                .filter(Objects::nonNull)
                .map(this::toStoreKey)
                .filter(Objects::nonNull)
                .flatMap(this::doStoreRemove);
    }

    protected abstract Mono<Void> doStoreRemove(byte[] key);

    @Override
    public Mono<Void> doRemoveAll(Set<? extends K> keys) {
        return Mono.justOrEmpty(keys)
                .filter(CollectionUtils::isNotEmpty)
                .map(ks -> {
                    List<byte[]> list = new ArrayList<>(ks.size());
                    for (K k : ks) {
                        byte[] keyBytes = toStoreKey(k);
                        if (null != keyBytes) {
                            list.add(keyBytes);
                        }
                    }
                    return list.toArray(new byte[list.size()][]);
                })
                .filter(bytes -> bytes.length > 0)
                .flatMap(this::doStoreRemoveAll);
    }

    protected abstract Mono<Void> doStoreRemoveAll(byte[][] keys);

    protected abstract byte[] toStoreKey(K key);

    protected abstract K fromStoreKey(byte[] key);

    /**
     * 存储对象 转换为 源对象
     *
     * @param storeValue 存储对象，可能为空。
     * @return 返回反序列化后的 源对象。如果开启压缩，解压缩后再反序列化。<br/>
     * 如果 存储对象 为 {@link NullValue#INSTANCE_BYTES}，返回 {@link CacheValues#emptyCacheValue()}
     */
    protected CacheValue<V> fromStoreValue(byte[] storeValue) {
        if (null == storeValue) {
            return null;
        }

        if (enableNullValue && Arrays.equals(NullValue.INSTANCE_BYTES, storeValue)) {
            return CacheValues.emptyCacheValue();
        }

        if (enableCompressValue) {
            try {
                storeValue = valueCompressor.decompress(storeValue);
            } catch (IOException e) {
                throw new RuntimeException("", e);
            }
        }

        V value = valueSerializer.deserialize(storeValue);
        return new CacheValue<>(value);
    }

    /**
     * 源对象 转换为 存储对象
     *
     * @param value 源对象，允许为空
     * @return 返回存储对象。如果开启压缩，返回先序列化再压缩的存储对象；其它返回序列化的存储对象。<br/>
     * 如果 源对象 为空，返回 {@link NullValue#INSTANCE_BYTES}
     */
    private byte[] toStoreValue(V value) {
        if (null == value) {
            return NullValue.INSTANCE_BYTES;
        }

        byte[] storeValue = valueSerializer.serialize(value);
        if (enableCompressValue) {
            try {
                return valueCompressor.compress(storeValue);
            } catch (IOException e) {
                throw new RuntimeException("can't compress", e);
            }
        }
        return storeValue;
    }

}
