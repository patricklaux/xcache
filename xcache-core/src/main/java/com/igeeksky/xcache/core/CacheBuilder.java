package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.Store;

/**
 * 缓存构建器
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/5
 */
@SuppressWarnings("unchecked")
public class CacheBuilder<K, V> {

    private CacheConfig<K, V> cacheConfig;

    private ExtendConfig<K, V> extendConfig;

    private final Store<V>[] stores = new Store[3];

    public static <K, V> CacheBuilder<K, V> builder(CacheConfig<K, V> cacheConfig) {
        CacheBuilder<K, V> builder = new CacheBuilder<>();
        builder.cacheConfig = cacheConfig;
        return builder;
    }

    public CacheBuilder<K, V> extendConfig(ExtendConfig<K, V> extendConfig) {
        this.extendConfig = extendConfig;
        return this;
    }

    public CacheBuilder<K, V> firstStore(Store<V> firstStore) {
        stores[0] = firstStore;
        return this;
    }

    public CacheBuilder<K, V> secondStore(Store<V> secondStore) {
        stores[1] = secondStore;
        return this;
    }

    public CacheBuilder<K, V> thirdStore(Store<V> thirdStore) {
        stores[2] = thirdStore;
        return this;
    }

    public Cache<K, V> build() {
        int count = count(stores);
        if (count == 3) {
            return new ThreeLevelCache<>(cacheConfig, extendConfig, stores);
        }
        if (count == 2) {
            return new TwoLevelCache<>(cacheConfig, extendConfig, stores);
        }
        if (count == 1) {
            return new OneLevelCache<>(cacheConfig, extendConfig, stores);
        }
        return new NoOpCache<>(cacheConfig, extendConfig.getCacheLoader(), extendConfig.getCacheWriter());
    }

    public static int count(Store<?>[] stores) {
        int count = 0;
        for (Store<?> store : stores) {
            if (store != null) {
                count++;
            }
        }
        return count;
    }

}