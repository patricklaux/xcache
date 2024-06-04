package com.igeeksky.xcache.store;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.CacheValues;
import com.igeeksky.xcache.common.ExpiryCacheValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.config.CacheProperties;
import com.igeeksky.xcache.extension.compress.Compressor;
import com.igeeksky.xcache.extension.monitor.CacheMonitor;
import com.igeeksky.xcache.extension.serialization.Serializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-30
 */
public abstract class AbstractLocalCacheStore<K, V> extends AbstractCacheStore<K, V> {

    private final boolean enableSerializeValue;
    private final Serializer<V> valueSerializer;

    private final boolean enableCompressValue;
    private final Compressor compressor;

    public AbstractLocalCacheStore(String name, CacheProperties.Local local, Class<K> keyType, Class<V> valueType,
                                   List<CacheMonitor<K, V>> cacheMonitors,
                                   Serializer<V> valueSerializer, Compressor compressor) {
        super(name, local, keyType, valueType, cacheMonitors);
        this.enableSerializeValue = local.isEnableSerializeValue();
        this.enableCompressValue = local.isEnableCompressValue();
        this.valueSerializer = valueSerializer;
        this.compressor = compressor;
    }

    @Override
    public Mono<CacheValue<V>> doGet(K key) {
        return Mono.fromSupplier(() -> this.fromStoreValue(doStoreGet(key)));
    }

    @Override
    public Flux<KeyValue<K, CacheValue<V>>> doGetAll(Set<? extends K> keys) {
        return Flux.fromIterable(keys)
                .map(key -> new KeyValue<>(key, this.fromStoreValue(doStoreGet(key))));
    }

    @Override
    public Mono<Void> doPut(K key, V value) {
        return Mono.fromSupplier(() -> {
            doStorePut(key, toStoreValue(value));
            return null;
        });
    }

    @Override
    public Mono<Void> doPutAll(Map<? extends K, ? extends V> keyValues) {
        return Mono.just(keyValues)
                .doOnNext(kvs -> keyValues.forEach((k, v) -> doStorePut(k, this.toStoreValue(v))))
                .then();
    }

    @Override
    public Mono<Void> doRemove(K key) {
        return Mono.just(key).doOnNext(this::doStoreRemove).then();
    }

    @Override
    public Mono<Void> doRemoveAll(Set<? extends K> keys) {
        return Mono.just(keys).doOnNext(this::doStoreRemoveAll).then();
    }

    protected abstract CacheValue<Object> doStoreGet(K key);

    protected abstract void doStorePut(K key, CacheValue<Object> cacheValue);

    protected abstract void doStoreRemove(K key);

    protected abstract void doStoreRemoveAll(Set<? extends K> keys);

    /**
     * 转换存储对象 为 源对象
     *
     * @param cacheValue 存储对象，允许为空
     * @return 返回的源对象（如果可压缩，先解压缩；如果可序列化，先反序列化）。
     */
    @SuppressWarnings("unchecked")
    private CacheValue<V> fromStoreValue(CacheValue<Object> cacheValue) {
        if (null == cacheValue) {
            return null;
        }

        Object storeValue = cacheValue.getValue();
        if (null == storeValue) {
            return (CacheValue<V>) cacheValue;
        }

        if (enableSerializeValue) {
            byte[] value = (byte[]) storeValue;
            if (enableCompressValue) {
                try {
                    value = compressor.decompress(value);
                } catch (IOException e) {
                    throw new RuntimeException("", e);
                }
            }
            return CacheValues.newCacheValue(valueSerializer.deserialize(value));
        }

        return (CacheValue<V>) cacheValue;
    }

    /**
     * 转换源对象 为 存储对象
     *
     * @param value 源对象，允许为空
     * @return 如果可序列化，返回序列化数据；如果可压缩，返回压缩数据；其它返回源对象。
     */
    private CacheValue<Object> toStoreValue(V value) {
        if (null == value) {
            return CacheValues.emptyCacheValue();
        }

        if (enableSerializeValue) {
            byte[] storeValue = valueSerializer.serialize(value);
            if (enableCompressValue) {
                try {
                    storeValue = compressor.compress(storeValue);
                } catch (IOException e) {
                    throw new RuntimeException("can't compress", e);
                }
            }
            return CacheValues.newCacheValue(storeValue);
        }
        return CacheValues.newCacheValue(value);
    }

}
