package com.igeeksky.xcache.core;

import com.igeeksky.xcache.NullValue;
import com.igeeksky.xcache.common.CacheKeyNullException;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.CacheValueNullException;
import com.igeeksky.xcache.common.CacheValues;
import com.igeeksky.xcache.core.config.CacheConfig;
import com.igeeksky.xcache.extension.compress.Compressor;
import com.igeeksky.xcache.extension.contains.ContainsPredicate;
import com.igeeksky.xcache.extension.convertor.KeyConvertor;
import com.igeeksky.xcache.extension.loader.CacheLoader;
import com.igeeksky.xcache.extension.lock.CacheLock;
import com.igeeksky.xcache.extension.serializer.Serializer;
import com.igeeksky.xtool.core.collection.CollectionUtils;

import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-19
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    private final String name;
    private final Class<K> keyType;
    private final Class<V> valueType;

    private final boolean compressLocalValue;
    private final boolean compressRemoteValue;
    private final boolean serializeLocalValue;
    private final boolean enableNullLocalValue;
    private final boolean enableNullRemoteValue;

    private final KeyConvertor keyConvertor;
    private final Compressor localValueCompressor;
    private final Compressor remoteValueCompressor;
    private final Serializer<V> localValueSerializer;
    private final Serializer<V> remoteValueSerializer;
    private final CacheLock cacheLock;
    private final ContainsPredicate<K> containsPredicate;

    public AbstractCache(CacheConfig<K, V> config) {
        this.name = config.getName();
        this.keyType = config.getKeyType();
        this.valueType = config.getValueType();
        this.compressLocalValue = config.getLocalConfig().isEnableCompressValue();
        this.compressRemoteValue = config.getRemoteConfig().isEnableCompressValue();
        this.serializeLocalValue = config.getLocalConfig().isEnableSerializeValue();
        this.enableNullLocalValue = config.getLocalConfig().isEnableNullValue();
        this.enableNullRemoteValue = config.getRemoteConfig().isEnableNullValue();
        this.keyConvertor = config.getKeyConvertor();
        this.localValueCompressor = config.getLocalConfig().getValueCompressor();
        this.remoteValueCompressor = config.getRemoteConfig().getValueCompressor();
        this.localValueSerializer = config.getLocalConfig().getValueSerializer();
        this.remoteValueSerializer = config.getRemoteConfig().getValueSerializer();
        this.cacheLock = config.getCacheLock();
        this.containsPredicate = config.getContainsPredicate();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<K> getKeyType() {
        return keyType;
    }

    @Override
    public Class<V> getValueType() {
        return valueType;
    }

    @Override
    public CacheValue<V> get(K key) {
        if (null == key) {
            throw new CacheKeyNullException();
        }
        return this.doGet(toStoreKey(key));
    }

    protected abstract CacheValue<V> doGet(String key);

    @Override
    public V get(K key, CacheLoader<K, V> cacheLoader) {
        if (null == key) {
            throw new CacheKeyNullException();
        }
        String storeKey = toStoreKey(key);
        CacheValue<V> cacheValue = this.doGet(storeKey);
        if (cacheValue != null) {
            return cacheValue.getValue();
        }
        return load(key, storeKey, cacheLoader);
    }

    private V load(K key, String storeKey, CacheLoader<K, V> cacheLoader) {
        if (containsPredicate.test(getName(), key)) {
            Lock keyLock = cacheLock.get(storeKey);
            keyLock.lock();
            try {
                CacheValue<V> cacheValue = this.doGet(storeKey);
                if (cacheValue != null) {
                    return cacheValue.getValue();
                }
                return this.doLoad(key, storeKey, cacheLoader);
            } finally {
                keyLock.unlock();
            }
        }
        return null;
    }

    protected abstract V doLoad(K key, String storeKey, CacheLoader<K, V> cacheLoader);

    @Override
    public Map<K, CacheValue<V>> getAll(Set<? extends K> keys) {
        if (null == keys) {
            throw new CacheKeyNullException("keys must not be null.");
        }
        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }
        // 1. 建立原生Key 和 缓存Key 之间的映射
        Map<String, K> keyMapping = new LinkedHashMap<>(keys.size());
        for (K k : keys) {
            if (k == null) {
                throw new CacheKeyNullException();
            }
            keyMapping.put(toStoreKey(k), k);
        }

        // 2. 从缓存中获取缓存值
        Map<String, CacheValue<V>> keyValues = this.doGetAll(new LinkedHashSet<>(keyMapping.keySet()));

        // 3. 原生Key 替换 缓存Key，并过滤掉无缓存的结果
        Map<K, CacheValue<V>> result = new LinkedHashMap<>(keyValues.size());
        for (Map.Entry<String, CacheValue<V>> entry : keyValues.entrySet()) {
            CacheValue<V> cacheValue = entry.getValue();
            if (cacheValue != null) {
                result.put(keyMapping.get(entry.getKey()), cacheValue);
            }
        }

        return result;
    }

    protected abstract Map<String, CacheValue<V>> doGetAll(Set<String> keys);

    @Override
    public void put(K key, V value) {
        if (null == key) {
            throw new CacheKeyNullException();
        }
        this.doPut(toStoreKey(key), value);
    }

    protected abstract void doPut(String key, V value);

    @Override
    public void putAll(Map<? extends K, ? extends V> keyValues) {
        Map<String, V> kvs = new LinkedHashMap<>(keyValues.size());
        keyValues.forEach((k, v) -> {
            if (k == null) {
                throw new CacheKeyNullException();
            }
            kvs.put(toStoreKey(k), v);
        });
        this.doPutAll(kvs);
    }

    protected abstract void doPutAll(Map<String, ? extends V> keyValues);

    @Override
    public void evict(K key) {
        if (null == key) {
            throw new CacheKeyNullException();
        }
        this.doEvict(toStoreKey(key));
    }

    protected abstract void doEvict(String key);

    @Override
    public void evictAll(Set<? extends K> keys) {
        if (null == keys) {
            throw new CacheKeyNullException();
        }
        if (keys.isEmpty()) {
            return;
        }

        Set<String> set = CollectionUtils.newLinkedHashSet(keys.size());
        for (K key : keys) {
            if (key == null) {
                throw new CacheKeyNullException();
            }
            set.add(toStoreKey(key));
        }

        this.doEvictAll(set);
    }

    protected abstract void doEvictAll(Set<String> keys);

    protected String toStoreKey(K key) {
        return keyConvertor.apply(key);
    }

    protected Object toLocalStoreValue(V value) {
        if (null == value) {
            if (enableNullLocalValue) {
                return null;
            }
            throw new CacheValueNullException();
        }
        if (serializeLocalValue) {
            if (compressLocalValue) {
                return localValueCompressor.compress(localValueSerializer.serialize(value));
            }
            return localValueSerializer.serialize(value);
        }
        return value;
    }

    protected byte[] toRemoteStoreValue(V value) {
        if (null == value) {
            if (enableNullRemoteValue) {
                return NullValue.INSTANCE_BYTES;
            }
            throw new CacheValueNullException();
        }
        byte[] remoteValue = remoteValueSerializer.serialize(value);
        if (compressRemoteValue) {
            return remoteValueCompressor.compress(remoteValue);
        }
        return remoteValue;
    }

    @SuppressWarnings("unchecked")
    protected CacheValue<V> fromLocalStoreValue(CacheValue<Object> cacheValue) {
        if (cacheValue == null) {
            return null;
        }
        // 本地缓存需要判断是否允许空值
        if (!cacheValue.hasValue()) {
            if (enableNullLocalValue) {
                return (CacheValue<V>) cacheValue;
            }
            return null;
        }
        if (serializeLocalValue) {
            Object storeValue = cacheValue.getValue();
            if (compressLocalValue) {
                V value = localValueSerializer.deserialize(localValueCompressor.decompress((byte[]) storeValue));
                return CacheValues.newCacheValue(value);
            }
            return CacheValues.newCacheValue(localValueSerializer.deserialize((byte[]) storeValue));
        }
        return (CacheValue<V>) cacheValue;
    }

    @SuppressWarnings("unchecked")
    protected CacheValue<V> fromRemoteStoreValue(CacheValue<byte[]> cacheValue) {
        if (cacheValue == null) {
            return null;
        }
        if (!cacheValue.hasValue()) {
            return (CacheValue<V>) cacheValue;
        }
        byte[] storeValue = cacheValue.getValue();
        // 远程缓存需要判断是否是空值(NullValue)
        if (Arrays.equals(NullValue.INSTANCE_BYTES, storeValue)) {
            if (enableNullRemoteValue) {
                return CacheValues.emptyCacheValue();
            }
            return null;
        }
        if (compressRemoteValue) {
            V value = remoteValueSerializer.deserialize(remoteValueCompressor.decompress(storeValue));
            return CacheValues.newCacheValue(value);
        }
        return CacheValues.newCacheValue(remoteValueSerializer.deserialize(storeValue));
    }

}