package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xcache.common.ContainsPredicate;
import com.igeeksky.xcache.core.store.StoreProvider;
import com.igeeksky.xcache.extension.codec.CodecProvider;
import com.igeeksky.xcache.extension.codec.JdkCodecProvider;
import com.igeeksky.xcache.extension.compress.CompressorProvider;
import com.igeeksky.xcache.extension.compress.DeflaterCompressorProvider;
import com.igeeksky.xcache.extension.compress.GzipCompressorProvider;
import com.igeeksky.xcache.extension.lock.CacheLockProvider;
import com.igeeksky.xcache.extension.lock.EmbedCacheLockProvider;
import com.igeeksky.xcache.extension.refresh.CacheRefreshProvider;
import com.igeeksky.xcache.extension.refresh.EmbedCacheRefreshProvider;
import com.igeeksky.xcache.extension.stat.CacheStatProvider;
import com.igeeksky.xcache.extension.stat.LogCacheStatProvider;
import com.igeeksky.xcache.extension.sync.CacheSyncProvider;
import com.igeeksky.xcache.props.CacheConstants;
import com.igeeksky.xcache.props.StatProps;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/**
 * 集中管理组件
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/18
 */
@SuppressWarnings("unchecked")
public class ComponentManager {

    private final ConcurrentMap<String, Supplier<CacheLoader<?, ?>>> loaders = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Supplier<StoreProvider>> stores = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Supplier<CacheSyncProvider>> syncs = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Supplier<CacheRefreshProvider>> refreshes = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Supplier<CacheStatProvider>> stats = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Supplier<CacheLockProvider>> locks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Supplier<CodecProvider>> codecs = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Supplier<CompressorProvider>> compressors = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Supplier<ContainsPredicate<?>>> predicates = new ConcurrentHashMap<>();

    private final long statPeriod;

    public ComponentManager(ScheduledExecutorService scheduler, StatProps statProps) {
        if (statProps != null && statProps.getPeriod() != null) {
            this.statPeriod = statProps.getPeriod();
        } else {
            this.statPeriod = CacheConstants.DEFAULT_STAT_PERIOD;
        }

        this.addCodecProvider(CacheConstants.JDK_CODEC, JdkCodecProvider::getInstance);
        this.addCompressorProvider(CacheConstants.GZIP_COMPRESSOR, GzipCompressorProvider::getInstance);
        this.addCompressorProvider(CacheConstants.DEFLATE_COMPRESSOR, DeflaterCompressorProvider::getInstance);
        this.addLockProvider(CacheConstants.EMBED_CACHE_LOCK, EmbedCacheLockProvider::getInstance);
        this.addStatProvider(CacheConstants.LOG_CACHE_STAT, SingletonSupplier.of(() -> new LogCacheStatProvider(scheduler, statPeriod)));
        this.addRefreshProvider(CacheConstants.EMBED_CACHE_REFRESH, SingletonSupplier.of(() -> new EmbedCacheRefreshProvider(scheduler)));
    }

    public void addCacheLoader(String name, Supplier<CacheLoader<?, ?>> cacheLoader) {
        this.loaders.put(name, cacheLoader);
    }

    public <K, V> CacheLoader<K, V> getCacheLoader(String name) {
        Supplier<CacheLoader<?, ?>> supplier = this.loaders.get(name);
        return (CacheLoader<K, V>) (supplier != null ? supplier.get() : null);
    }

    public void addContainsPredicate(String name, Supplier<ContainsPredicate<?>> provider) {
        this.predicates.put(name, provider);
    }

    public <K> ContainsPredicate<K> getContainsPredicate(String name) {
        Supplier<ContainsPredicate<?>> supplier = this.predicates.get(name);
        return (ContainsPredicate<K>) (supplier != null ? supplier.get() : null);
    }

    public void addLockProvider(String beanId, Supplier<CacheLockProvider> provider) {
        this.locks.put(beanId, provider);
    }

    public CacheLockProvider getLockProvider(String beanId) {
        Supplier<CacheLockProvider> supplier = this.locks.get(beanId);
        return supplier != null ? supplier.get() : null;
    }

    public void addRefreshProvider(String beanId, Supplier<CacheRefreshProvider> provider) {
        this.refreshes.put(beanId, provider);
    }

    public CacheRefreshProvider getRefreshProvider(String beanId) {
        Supplier<CacheRefreshProvider> supplier = this.refreshes.get(beanId);
        return supplier != null ? supplier.get() : null;
    }

    public void addCodecProvider(String beanId, Supplier<CodecProvider> provider) {
        this.codecs.put(beanId, provider);
    }

    public CodecProvider getCodecProvider(String beanId) {
        Supplier<CodecProvider> supplier = this.codecs.get(beanId);
        return supplier != null ? supplier.get() : null;
    }

    public void addCompressorProvider(String beanId, Supplier<CompressorProvider> provider) {
        this.compressors.put(beanId, provider);
    }

    public CompressorProvider getCompressorProvider(String beanId) {
        Supplier<CompressorProvider> supplier = this.compressors.get(beanId);
        return supplier != null ? supplier.get() : null;
    }

    public void addStatProvider(String beanId, Supplier<CacheStatProvider> provider) {
        this.stats.put(beanId, provider);
    }

    public CacheStatProvider getStatProvider(String beanId) {
        Supplier<CacheStatProvider> supplier = this.stats.get(beanId);
        return supplier != null ? supplier.get() : null;
    }

    public void addSyncProvider(String beanId, Supplier<CacheSyncProvider> provider) {
        this.syncs.put(beanId, provider);
    }

    public CacheSyncProvider getSyncProvider(String beanId) {
        Supplier<CacheSyncProvider> supplier = this.syncs.get(beanId);
        return supplier != null ? supplier.get() : null;
    }

    public void addStoreProvider(String beanId, Supplier<StoreProvider> provider) {
        this.stores.put(beanId, provider);
    }

    public StoreProvider getStoreProvider(String beanId) {
        Supplier<StoreProvider> supplier = this.stores.get(beanId);
        return supplier != null ? supplier.get() : null;
    }

}
