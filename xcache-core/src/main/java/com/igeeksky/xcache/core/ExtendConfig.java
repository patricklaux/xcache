package com.igeeksky.xcache.core;


import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xcache.extension.contains.ContainsPredicate;
import com.igeeksky.xcache.extension.lock.LockService;
import com.igeeksky.xcache.extension.refresh.CacheRefresh;
import com.igeeksky.xcache.extension.stat.CacheStatMonitor;
import com.igeeksky.xcache.extension.sync.CacheSyncMonitor;
import com.igeeksky.xtool.core.lang.codec.KeyCodec;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/16
 */
public class ExtendConfig<K, V> {

    private final LockService cacheLock;

    private final KeyCodec<K> keyCodec;

    private final CacheStatMonitor statMonitor;

    private final CacheSyncMonitor syncMonitor;

    private final CacheRefresh cacheRefresh;

    private final CacheLoader<K, V> cacheLoader;

    private final ContainsPredicate<K> containsPredicate;

    public ExtendConfig(Builder<K, V> builder) {
        this.cacheLock = builder.cacheLock;
        this.keyCodec = builder.keyCodec;
        this.statMonitor = builder.statMonitor;
        this.syncMonitor = builder.syncMonitor;
        this.cacheLoader = builder.cacheLoader;
        this.cacheRefresh = builder.cacheRefresh;
        this.containsPredicate = builder.containsPredicate;
    }

    public static <K, V> Builder<K, V> builder(CacheLoader<K, V> cacheLoader) {
        return new Builder<>(cacheLoader);
    }

    public LockService getCacheLock() {
        return cacheLock;
    }

    public KeyCodec<K> getKeyCodec() {
        return keyCodec;
    }

    public CacheStatMonitor getStatMonitor() {
        return statMonitor;
    }

    public CacheSyncMonitor getSyncMonitor() {
        return syncMonitor;
    }

    public CacheLoader<K, V> getCacheLoader() {
        return cacheLoader;
    }

    public ContainsPredicate<K> getContainsPredicate() {
        return containsPredicate;
    }

    public CacheRefresh getCacheRefresh() {
        return cacheRefresh;
    }

    public static class Builder<K, V> {

        private LockService cacheLock;

        private KeyCodec<K> keyCodec;

        private CacheStatMonitor statMonitor;

        private CacheSyncMonitor syncMonitor;

        private final CacheLoader<K, V> cacheLoader;

        private ContainsPredicate<K> containsPredicate;

        private CacheRefresh cacheRefresh;

        private Builder(CacheLoader<K, V> cacheLoader) {
            this.cacheLoader = cacheLoader;
        }

        public Builder<K, V> cacheLock(LockService cacheLock) {
            this.cacheLock = cacheLock;
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
            return new ExtendConfig<>(this);
        }

    }

}
