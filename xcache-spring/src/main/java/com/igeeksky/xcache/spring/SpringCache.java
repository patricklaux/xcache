package com.igeeksky.xcache.spring;

import com.igeeksky.xcache.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.loader.CacheLoader;
import com.igeeksky.xcache.common.loader.CacheLoaderException;
import org.springframework.lang.NonNull;

import java.util.concurrent.Callable;

/**
 * @author Patrick.Lau
 * @since 0.0.2 2021-06-03
 */
public class SpringCache implements org.springframework.cache.Cache {

    private final String name;
    private final Cache<Object, Object> cache;

    public SpringCache(Cache<Object, Object> cache) {
        this.name = cache.getName();
        this.cache = cache;
    }

    @Override
    @NonNull
    public String getName() {
        return name;
    }

    @Override
    @NonNull
    public Object getNativeCache() {
        return cache;
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        CacheValue<Object> cacheValue = cache.sync().get(key);
        return toValueWrapper(cacheValue);
    }


    private ValueWrapper toValueWrapper(CacheValue<Object> wrapper) {
        return null == wrapper ? null : (wrapper::getValue);
    }


    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        CacheValue<Object> cacheValue = cache.sync().get(key);
        return fromStoreValue(cacheValue);
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        CacheValue<Object> cacheValue = cache.sync().get(key, k -> new CacheLoaderImpl<>(valueLoader));
        return fromStoreValue(cacheValue);
    }

    @SuppressWarnings("unchecked")
    private <T> T fromStoreValue(CacheValue<Object> cacheValue) {
        return null == cacheValue ? null : (T) cacheValue.getValue();
    }

    @Override
    public void put(@NonNull Object key, Object value) {
        cache.sync().put(key, value);
    }

    @Override
    public void evict(@NonNull Object key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.sync().clear();
    }

    private static class CacheLoaderImpl<K, V> implements CacheLoader<K, V> {

        private final Callable<V> valueLoader;

        public CacheLoaderImpl(Callable<V> valueLoader) {
            this.valueLoader = valueLoader;
        }

        @Override
        public V load(K k) {
            try {
                return valueLoader.call();
            } catch (Exception e) {
                throw new CacheLoaderException(e);
            }
        }
    }

}
