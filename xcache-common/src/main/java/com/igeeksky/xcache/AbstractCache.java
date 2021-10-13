package com.igeeksky.xcache;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-19
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    private final String name;
    private final Class<K> keyType;
    private final Class<V> valueType;

    private final Object lock = new Object();
    private volatile SyncCache<K, V> syncCache;
    private volatile AsyncCache<K, V> asyncCache;

    public AbstractCache(String name, Class<K> keyType, Class<V> valueType) {
        this.name = name;
        this.keyType = keyType;
        this.valueType = valueType;
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
    public SyncCache<K, V> sync() {
        if (null == syncCache) {
            synchronized (lock) {
                if (null == syncCache) {
                    this.syncCache = new SyncCache.SyncCacheView<>(this);
                }
            }
        }
        return syncCache;
    }

    @Override
    public AsyncCache<K, V> async() {
        if (null == asyncCache) {
            synchronized (lock) {
                if (null == asyncCache) {
                    this.asyncCache = new AsyncCache.AsyncCacheView<>(this);
                }
            }
        }
        return asyncCache;
    }
}
