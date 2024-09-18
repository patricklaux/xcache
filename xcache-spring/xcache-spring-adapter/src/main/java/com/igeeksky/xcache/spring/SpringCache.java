package com.igeeksky.xcache.spring;

import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xcache.common.CacheLoadingException;
import com.igeeksky.xcache.common.CacheValue;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.lang.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * 适配 Spring Cache
 *
 * @author Patrick.Lau
 * @since 0.0.2 2021-06-03
 */
@SuppressWarnings("unchecked")
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
        return this.toValueWrapper(cache.get(key));
    }

    @Override
    public CompletableFuture<?> retrieve(@NonNull Object key) {
        Object cacheOrLoad = cache.getOrLoad(key);
        if (cacheOrLoad == null) {
            return null;
        }
        return CompletableFuture.completedFuture(toValueWrapper(cacheOrLoad));
    }

    @Override
    @NonNull
    public <T> CompletableFuture<T> retrieve(@NonNull Object key, @NonNull Supplier<CompletableFuture<T>> valueLoader) {
        T value = (T) cache.get(key, k -> (CacheLoader<Object, T>) ignored -> {
            try {
                CompletableFuture<T> future = valueLoader.get();
                if (future != null) {
                    return future.get();
                }
                return null;
            } catch (InterruptedException | ExecutionException e) {
                throw new CacheLoadingException(e);
            }
        });
        return CompletableFuture.completedFuture(value);
    }

    @Override
    public <V> V get(@NonNull Object key, Class<V> type) {
        return this.fromStoreValue(cache.get(key));
    }

    @Override
    public <V> V get(@NonNull Object key, @NonNull Callable<V> valueLoader) {
        Object value = cache.get(key, k -> new CacheLoaderImpl<>(valueLoader));
        return (V) value;
    }

    private ValueWrapper toValueWrapper(CacheValue<Object> cacheValue) {
        return null == cacheValue ? null : (cacheValue::getValue);
    }

    @SuppressWarnings("unchecked")
    private <V> V fromStoreValue(CacheValue<Object> cacheValue) {
        return null == cacheValue ? null : (V) cacheValue.getValue();
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

    private static SimpleValueWrapper toValueWrapper(Object storeValue) {
        return null != storeValue ? new SimpleValueWrapper(storeValue) : null;
    }

    private record CacheLoaderImpl<K, V>(Callable<V> valueLoader) implements CacheLoader<K, V> {

        @Override
        public V load(K k) {
            try {
                return valueLoader.call();
            } catch (Exception e) {
                throw new CacheLoadingException("Key:[" + k + "], " + e.getMessage(), e);
            }
        }
    }

}
