package com.igeeksky.xcache.spring;

import com.igeeksky.xcache.Cache;
import com.igeeksky.xcache.common.CacheLoaderException;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.extension.loader.CacheLoader;
import org.springframework.lang.NonNull;

import java.util.concurrent.Callable;

/**
 * 适配 Spring Cache
 *
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
        return toValueWrapper(cache.get(key));
    }

    private ValueWrapper toValueWrapper(CacheValue<Object> wrapper) {
        return null == wrapper ? null : (wrapper::getValue);
    }

    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        return fromStoreValue(cache.get(key));
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        CacheValue<Object> cacheValue = cache.get(key, k -> new CacheLoaderImpl<>(valueLoader));
        return fromStoreValue(cacheValue);
    }

    @SuppressWarnings("unchecked")
    private <T> T fromStoreValue(CacheValue<Object> cacheValue) {
        return null == cacheValue ? null : (T) cacheValue.getValue();
    }

    @Override
    public void put(@NonNull Object key, Object value) {
        cache.put(key, value);
    }

    @Override
    public void evict(@NonNull Object key) {
        cache.evict(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    private record CacheLoaderImpl<K, V>(Callable<V> valueLoader) implements CacheLoader<K, V> {

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
