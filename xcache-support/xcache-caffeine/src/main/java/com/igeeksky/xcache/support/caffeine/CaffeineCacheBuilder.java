package com.igeeksky.xcache.support.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.igeeksky.xcache.core.*;
import com.igeeksky.xcache.core.config.CacheConfig;
import com.igeeksky.xcache.core.statistic.CacheStatisticsHolder;
import com.igeeksky.xcache.core.statistic.CacheStatisticsPublisher;

import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * @author: Patrick.Lau
 * @date: 2021-06-21
 */
public class CaffeineCacheBuilder implements CacheBuilder {

    @Override
    public Cache<Object, Object> build(String name) {
        return null;
    }

    @Override
    public <K, V> Cache<K, V> build(String name, Class<K> keyClazz, Class<V> valueClazz) {
        com.github.benmanes.caffeine.cache.Cache<K, ExpiryCacheValue<V>> caffeineCache =
                Caffeine.newBuilder()
                        .expireAfter(new RandomRangeCacheExpiry<K, V>(1000000000000L, 500000000000L))
                        .maximumSize(1280000)
                        .build();

        CaffeineCacheStore<K, V> cacheStore
                = new CaffeineCacheStore<>(caffeineCache);

        CacheConfig<K, V> cacheConfig = new CacheConfig<K, V>() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getNamespace() {
                return null;
            }

            @Override
            public CacheLevel getCacheLevel() {
                return null;
            }

            @Override
            public CacheStore<K, V> getCacheStore() {
                return cacheStore;
            }

            @Override
            public Function<K, V> getLoader() {
                return null;
            }

            @Override
            public CacheStatisticsHolder getCacheStatisticsHolder() {
                return null;
            }

            @Override
            public BiPredicate<String, K> getContainsPredicate() {
                return null;
            }

            @Override
            public CacheStatisticsPublisher getStatisticsPublisher() {
                return null;
            }

        };
        return new DefaultCache<>(cacheConfig);
    }
}
