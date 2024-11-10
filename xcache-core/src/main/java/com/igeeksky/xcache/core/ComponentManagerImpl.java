package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xcache.common.CacheWriter;
import com.igeeksky.xcache.core.store.StoreProvider;
import com.igeeksky.xcache.extension.codec.CodecProvider;
import com.igeeksky.xcache.extension.codec.JdkCodecProvider;
import com.igeeksky.xcache.extension.compress.CompressorProvider;
import com.igeeksky.xcache.extension.compress.DeflaterCompressorProvider;
import com.igeeksky.xcache.extension.compress.GzipCompressorProvider;
import com.igeeksky.xcache.common.ContainsPredicate;
import com.igeeksky.xcache.extension.lock.CacheLockProvider;
import com.igeeksky.xcache.extension.lock.EmbedCacheLockProvider;
import com.igeeksky.xcache.extension.refresh.CacheRefreshProvider;
import com.igeeksky.xcache.extension.refresh.EmbedCacheRefreshProvider;
import com.igeeksky.xcache.extension.stat.CacheStatProvider;
import com.igeeksky.xcache.extension.stat.LogCacheStatProvider;
import com.igeeksky.xcache.extension.sync.CacheSyncProvider;
import com.igeeksky.xcache.props.CacheConstants;
import com.igeeksky.xcache.props.StatProps;
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

    private final Lock lock = new ReentrantLock();

    private final ConcurrentMap<String, CacheLoader<?, ?>> loaders = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheWriter<?, ?>> writers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, StoreProvider> stores = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheSyncProvider> syncs = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheRefreshProvider> refreshes = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheStatProvider> stats = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheLockProvider> locks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CodecProvider> codecs = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CompressorProvider> compressors = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ContainsPredicate<?>> predicates = new ConcurrentHashMap<>();

    private final long statPeriod;
    private final ScheduledExecutorService scheduler;

    private volatile LogCacheStatProvider logCacheStatProvider;

    public ComponentManagerImpl(ScheduledExecutorService scheduler, StatProps statProps) {
        if (statProps != null && statProps.getPeriod() != null) {
            this.statPeriod = statProps.getPeriod();
        } else {
            this.statPeriod = CacheConstants.DEFAULT_STAT_PERIOD;
        }

        this.scheduler = scheduler;
        this.addProvider(CacheConstants.JDK_CODEC, JdkCodecProvider.getInstance());
        this.addProvider(CacheConstants.DEFLATE_COMPRESSOR, DeflaterCompressorProvider.getInstance());
        this.addProvider(CacheConstants.GZIP_COMPRESSOR, GzipCompressorProvider.getInstance());
        this.addProvider(CacheConstants.EMBED_CACHE_LOCK, EmbedCacheLockProvider.getInstance());
        this.addProvider(CacheConstants.EMBED_CACHE_REFRESH, new EmbedCacheRefreshProvider(scheduler));
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
    public void addContainsPredicate(String name, ContainsPredicate<?> provider) {
        this.predicates.put(name, provider);
    }

    @Override
    public <K> ContainsPredicate<K> getContainsPredicate(String name) {
        return (ContainsPredicate<K>) this.predicates.get(name);
    }

    @Override
    public void addProvider(String beanId, CacheRefreshProvider provider) {
        this.refreshes.put(beanId, provider);
    }

    @Override
    public CacheRefreshProvider getRefreshProvider(String beanId) {
        return this.refreshes.get(beanId);
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
     * 根据 beanId 创建或获取日志缓存统计提供者
     * <p>
     * 缓存指标采集会执行定时任务，为避免不必要的线程消耗，只有确定需要日志缓存统计时才创建 LogCacheStatProvider
     *
     * @param beanId bean的标识符，用于确定是否创建日志缓存统计提供者
     * @return LogCacheStatProvider 返回日志缓存统计提供者实例，如果beanId不匹配则返回null
     */
    private LogCacheStatProvider logCacheStat(String beanId) {
        // 检查 beanId 是否匹配日志缓存统计的标识符，忽略大小写
        if (!Objects.equals(CacheConstants.LOG_CACHE_STAT, StringUtils.toLowerCase(beanId))) {
            return null;
        }

        if (this.logCacheStatProvider == null) {
            lock.lock();
            try {
                if (this.logCacheStatProvider == null) {
                    // 实例化并注册 LogCacheStatProvider
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
