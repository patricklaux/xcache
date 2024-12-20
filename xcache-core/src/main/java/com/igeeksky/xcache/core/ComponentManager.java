package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xcache.common.ContainsPredicate;
import com.igeeksky.xcache.core.store.StoreProvider;
import com.igeeksky.xcache.extension.codec.CodecProvider;
import com.igeeksky.xcache.extension.compress.CompressorProvider;
import com.igeeksky.xcache.extension.lock.CacheLockProvider;
import com.igeeksky.xcache.extension.refresh.CacheRefreshProvider;
import com.igeeksky.xcache.extension.stat.CacheStatProvider;
import com.igeeksky.xcache.extension.sync.CacheSyncProvider;

/**
 * 缓存组件管理器
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/8
 */
public interface ComponentManager {

    /**
     * 添加缓存加载器
     *
     * @param name        缓存名称
     * @param cacheLoader 缓存加载器
     */
    void addCacheLoader(String name, CacheLoader<?, ?> cacheLoader);

    <K, V> CacheLoader<K, V> getCacheLoader(String name);

    void addProvider(String beanId, CacheLockProvider provider);

    CacheLockProvider getLockProvider(String beanId);

    void addContainsPredicate(String name, ContainsPredicate<?> predicate);

    <K> ContainsPredicate<K> getContainsPredicate(String beanId);

    void addProvider(String beanId, CacheRefreshProvider provider);

    CacheRefreshProvider getRefreshProvider(String beanId);

    void addProvider(String beanId, CodecProvider provider);

    CodecProvider getCodecProvider(String beanId);

    void addProvider(String beanId, CompressorProvider provider);

    CompressorProvider getCompressorProvider(String beanId);

    void addProvider(String beanId, CacheStatProvider provider);

    CacheStatProvider getStatProvider(String beanId);

    void addProvider(String beanId, CacheSyncProvider provider);

    CacheSyncProvider getSyncProvider(String beanId);

    void addProvider(String beanId, StoreProvider provider);

    StoreProvider getStoreProvider(String beanId);

}
