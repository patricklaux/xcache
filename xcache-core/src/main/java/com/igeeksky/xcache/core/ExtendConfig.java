package com.igeeksky.xcache.core;


import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xcache.common.ContainsPredicate;
import com.igeeksky.xcache.extension.NoOpContainsPredicate;
import com.igeeksky.xcache.extension.lock.LockService;
import com.igeeksky.xcache.extension.refresh.CacheRefresh;
import com.igeeksky.xcache.extension.stat.CacheStatMonitor;
import com.igeeksky.xcache.extension.stat.NoOpCacheStatMonitor;
import com.igeeksky.xcache.extension.sync.CacheSyncMonitor;
import com.igeeksky.xcache.extension.sync.NoOpCacheSyncMonitor;
import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.codec.KeyCodec;

/**
 * 缓存扩展配置，用于扩展缓存能力
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/16
 */
public class ExtendConfig<K, V> {

    private final KeyCodec<K> keyCodec;

    private final LockService lockService;

    private final CacheRefresh cacheRefresh;

    private final CacheStatMonitor statMonitor;

    private final CacheSyncMonitor syncMonitor;

    private final CacheLoader<K, V> cacheLoader;

    private final ContainsPredicate<K> containsPredicate;

    public ExtendConfig(Builder<K, V> builder) {
        this.keyCodec = builder.keyCodec;
        this.lockService = builder.lockService;
        this.statMonitor = builder.statMonitor;
        this.syncMonitor = builder.syncMonitor;
        this.cacheLoader = builder.cacheLoader;
        this.cacheRefresh = builder.cacheRefresh;
        this.containsPredicate = builder.containsPredicate;
    }

    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    /**
     * 缓存锁服务，用于控制并发访问数据源，如：分布式环境下，防止缓存击穿
     *
     * @return LockService 缓存锁服务，不为空
     */
    public LockService getLockService() {
        return lockService;
    }

    /**
     * 键编码器，用于将键转换为字符串
     *
     * @return KeyCodec 键编码器，不为空
     */
    public KeyCodec<K> getKeyCodec() {
        return keyCodec;
    }

    /**
     * 缓存加载器，用于从数据源加载数据到缓存
     *
     * @return 如果有配置，返回配置的加载器；否则返回 null
     */
    public CacheLoader<K, V> getCacheLoader() {
        return cacheLoader;
    }

    /**
     * 缓存刷新器，用于定时刷新缓存数据，如：从数据库中加载数据到缓存中
     *
     * @return 如果有配置，返回配置的刷新器；否则返回 null
     */
    public CacheRefresh getCacheRefresh() {
        return cacheRefresh;
    }

    /**
     * 缓存统计监视器，用于统计缓存命中率、缓存命中次数等
     *
     * @return 如果有配置，返回配置的监视器；否则返回无操作监视器 {@link NoOpCacheStatMonitor}
     */
    public CacheStatMonitor getStatMonitor() {
        if (statMonitor == null) {
            return NoOpCacheStatMonitor.getInstance();
        }
        return statMonitor;
    }

    /**
     * 缓存同步监视器，用于在分布式环境下，同步缓存数据
     *
     * @return 如果有配置，返回配置的监视器；否则返回无操作监视器 {@link NoOpCacheSyncMonitor}
     */
    public CacheSyncMonitor getSyncMonitor() {
        if (syncMonitor == null) {
            return NoOpCacheSyncMonitor.getInstance();
        }
        return syncMonitor;
    }

    /**
     * 存在断言，用于判断数据源是否包含某个键
     *
     * @return 如果有配置，返回配置的断言；否则返回无操作断言 {@link NoOpContainsPredicate}
     */
    public ContainsPredicate<K> getContainsPredicate() {
        if (containsPredicate == null) {
            return NoOpContainsPredicate.getInstance();
        }
        return containsPredicate;
    }

    public static class Builder<K, V> {

        private KeyCodec<K> keyCodec;

        private LockService lockService;

        private CacheRefresh cacheRefresh;

        private CacheStatMonitor statMonitor;

        private CacheSyncMonitor syncMonitor;

        private CacheLoader<K, V> cacheLoader;

        private ContainsPredicate<K> containsPredicate;

        private Builder() {
        }

        public Builder<K, V> cacheLoader(CacheLoader<K, V> cacheLoader) {
            this.cacheLoader = cacheLoader;
            return this;
        }

        public Builder<K, V> lockService(LockService lockService) {
            this.lockService = lockService;
            return this;
        }

        public Builder<K, V> keyCodec(KeyCodec<K> keyCodec) {
            this.keyCodec = keyCodec;
            return this;
        }

        public Builder<K, V> statMonitor(CacheStatMonitor statMonitor) {
            this.statMonitor = statMonitor;
            return this;
        }

        public Builder<K, V> syncMonitor(CacheSyncMonitor syncMonitor) {
            this.syncMonitor = syncMonitor;
            return this;
        }

        public Builder<K, V> containsPredicate(ContainsPredicate<K> containsPredicate) {
            this.containsPredicate = containsPredicate;
            return this;
        }

        public Builder<K, V> cacheRefresh(CacheRefresh cacheRefresh) {
            this.cacheRefresh = cacheRefresh;
            return this;
        }

        public ExtendConfig<K, V> build() {
            Assert.notNull(this.keyCodec, "keyCodec must not be null");
            Assert.notNull(this.lockService, "cacheLock must not be null");
            return new ExtendConfig<>(this);
        }

    }

}
