package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xcache.common.CacheWriter;
import com.igeeksky.xcache.core.store.StoreProvider;
import com.igeeksky.xcache.extension.codec.CodecProvider;
import com.igeeksky.xcache.extension.codec.JdkCodecProvider;
import com.igeeksky.xcache.extension.compress.CompressorProvider;
import com.igeeksky.xcache.extension.compress.DeflaterCompressorProvider;
import com.igeeksky.xcache.extension.compress.GzipCompressorProvider;
import com.igeeksky.xcache.extension.contains.ContainsPredicateProvider;
import com.igeeksky.xcache.extension.contains.EmbedContainsPredicateProvider;
import com.igeeksky.xcache.extension.lock.CacheLockProvider;
import com.igeeksky.xcache.extension.lock.EmbedCacheLockProvider;
import com.igeeksky.xcache.extension.refresh.CacheRefreshProvider;
import com.igeeksky.xcache.extension.refresh.EmbedCacheRefreshProvider;
import com.igeeksky.xcache.extension.stat.CacheStatProvider;
import com.igeeksky.xcache.extension.stat.LogCacheStatProvider;
import com.igeeksky.xcache.extension.sync.CacheSyncProvider;
import com.igeeksky.xcache.props.CacheConstants;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 集中管理组件
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/18
 */
@SuppressWarnings("unchecked")
public class ComponentManagerImpl implements ComponentManager {

    private final ConcurrentMap<String, CacheLoader<?, ?>> loaders = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheWriter<?, ?>> writers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, StoreProvider> stores = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheSyncProvider> syncs = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheRefreshProvider> refreshes = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheStatProvider> stats = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheLockProvider> locks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CodecProvider> codecs = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CompressorProvider> compressors = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ContainsPredicateProvider> predicates = new ConcurrentHashMap<>();

    private final Lock lock = new ReentrantLock();

    private final long statPeriod;
    private final ScheduledExecutorService scheduler;

    private volatile LogCacheStatProvider logCacheStatProvider;
    private volatile EmbedCacheRefreshProvider embedCacheRefreshProvider;

    public ComponentManagerImpl(ScheduledExecutorService scheduler, Long statPeriod) {
        this.scheduler = scheduler;
        this.statPeriod = (statPeriod == null ? CacheConstants.DEFAULT_STAT_PERIOD : statPeriod);

        this.addProvider(CacheConstants.JDK_CODEC, JdkCodecProvider.getInstance());
        this.addProvider(CacheConstants.DEFLATER_COMPRESSOR, DeflaterCompressorProvider.getInstance());
        this.addProvider(CacheConstants.GZIP_COMPRESSOR, GzipCompressorProvider.getInstance());
        this.addProvider(CacheConstants.EMBED_CACHE_LOCK, EmbedCacheLockProvider.getInstance());
        this.addProvider(CacheConstants.EMBED_CONTAINS_PREDICATE, EmbedContainsPredicateProvider.getInstance());
    }

    @Override
    public void addCacheLoader(String name, CacheLoader<?, ?> cacheLoader) {
        this.loaders.put(name, cacheLoader);
    }

    @Override
    public <K, V> CacheLoader<K, V> getCacheLoader(String name) {
        return (CacheLoader<K, V>) this.loaders.get(name);
    }

    @Override
    public void addCacheWriter(String name, CacheWriter<?, ?> cacheWriter) {
        this.writers.put(name, cacheWriter);
    }

    @Override
    public <K, V> CacheWriter<K, V> getCacheWriter(String name) {
        return (CacheWriter<K, V>) this.writers.get(name);
    }

    @Override
    public void addProvider(String beanId, CacheLockProvider provider) {
        this.locks.put(beanId, provider);
    }

    @Override
    public CacheLockProvider getLockProvider(String beanId) {
        return this.locks.get(beanId);
    }

    @Override
    public void addProvider(String beanId, ContainsPredicateProvider provider) {
        this.predicates.put(beanId, provider);
    }

    @Override
    public ContainsPredicateProvider getPredicateProvider(String beanId) {
        return this.predicates.get(beanId);
    }

    @Override
    public void addProvider(String beanId, CacheRefreshProvider provider) {
        this.refreshes.put(beanId, provider);
    }

    @Override
    public CacheRefreshProvider getRefreshProvider(String beanId) {
        CacheRefreshProvider provider = this.refreshes.get(beanId);
        return (provider != null) ? provider : this.embedCacheRefresh(beanId);
    }

    @Override
    public void addProvider(String beanId, CodecProvider provider) {
        this.codecs.put(beanId, provider);
    }

    @Override
    public CodecProvider getCodecProvider(String beanId) {
        return this.codecs.get(beanId);
    }

    @Override
    public void addProvider(String beanId, CompressorProvider provider) {
        this.compressors.put(beanId, provider);
    }

    @Override
    public CompressorProvider getCompressorProvider(String beanId) {
        return this.compressors.get(beanId);
    }

    @Override
    public void addProvider(String beanId, CacheStatProvider provider) {
        this.stats.put(beanId, provider);
    }

    @Override
    public CacheStatProvider getStatProvider(String beanId) {
        CacheStatProvider cacheStatProvider = this.stats.get(beanId);
        return cacheStatProvider != null ? cacheStatProvider : this.logCacheStat(beanId);
    }

    @Override
    public void addProvider(String beanId, CacheSyncProvider provider) {
        this.syncs.put(beanId, provider);
    }

    @Override
    public CacheSyncProvider getSyncProvider(String beanId) {
        return this.syncs.get(beanId);
    }

    @Override
    public void addProvider(String beanId, StoreProvider provider) {
        this.stores.put(beanId, provider);
    }

    @Override
    public StoreProvider getStoreProvider(String beanId) {
        return this.stores.get(beanId);
    }

    /**
     * 添加 EmbedCacheRefreshProvider 到缓存管理器
     * <p>
     * 本方法的目的是为了启用内嵌的缓存刷新机制。它通过创建一个 EmbedCacheRefreshProvider 实例，
     * 并将其注册到缓存管理器中，使得缓存管理器能够管理和调度缓存的刷新任务。
     *
     * @param beanId 用于检查是否与预定义标识符匹配
     * @return EmbedCacheRefreshProvider 实例，如果 beanId 与 {@link CacheConstants#EMBED_CACHE_REFRESH} 匹配，则返回注册的实例，否则返回 null。
     */
    private EmbedCacheRefreshProvider embedCacheRefresh(String beanId) {
        if (!Objects.equals(CacheConstants.EMBED_CACHE_REFRESH, StringUtils.toLowerCase(beanId))) {
            return null;
        }
        if (this.embedCacheRefreshProvider == null) {
            lock.lock();
            try {
                if (this.embedCacheRefreshProvider == null) {
                    this.embedCacheRefreshProvider = new EmbedCacheRefreshProvider(scheduler);
                    this.addProvider(CacheConstants.EMBED_CACHE_REFRESH, this.embedCacheRefreshProvider);
                }
            } finally {
                lock.unlock();
            }
        }
        return this.embedCacheRefreshProvider;
    }

    /**
     * 添加 LogCacheStatProvider 到缓存管理器
     * 该方法用于将日志缓存统计提供者绑定到指定的缓存管理器上。
     * 如果 beanId 指定的是日志方式的缓存统计，则会尝试创建并注册 LogCacheStatProvider 实例，
     * 并将该实例添加到缓存管理器中。
     * <p>
     * 此方法确保了 LogCacheStatProvider 的单例性质，多次调用添加的是同一实例。
     *
     * @param beanId bean 标识符，用于判断是否为日志缓存统计。
     * @return 如果成功注册了LogCacheStatProvider，则返回该实例；否则返回null。
     */
    public LogCacheStatProvider logCacheStat(String beanId) {
        if (!Objects.equals(CacheConstants.LOG_CACHE_STAT, StringUtils.toLowerCase(beanId))) {
            return null;
        }

        if (this.logCacheStatProvider == null) {
            lock.lock();
            try {
                if (this.logCacheStatProvider == null) {
                    this.logCacheStatProvider = new LogCacheStatProvider(scheduler, statPeriod);
                    this.addProvider(CacheConstants.LOG_CACHE_STAT, this.logCacheStatProvider);
                }
            } finally {
                lock.unlock();
            }
        }
        return this.logCacheStatProvider;
    }

}