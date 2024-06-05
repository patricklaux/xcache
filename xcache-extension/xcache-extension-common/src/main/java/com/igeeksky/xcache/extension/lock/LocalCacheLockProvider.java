package com.igeeksky.xcache.extension.lock;

import com.igeeksky.xcache.config.props.CacheProps;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-06-10
 */
public class LocalCacheLockProvider implements CacheLockProvider {

    private static final LocalCacheLockProvider INSTANCE = new LocalCacheLockProvider();

    public static LocalCacheLockProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public CacheLock get(CacheProps cacheProps) {
        Integer lockSize = cacheProps.getExtension().getCacheLockSize();
        if (lockSize == null) {
            return new LocalCacheLock<>();
        }
        return new LocalCacheLock<>(lockSize);
    }

    @Override
    public void close() {
        // do nothing
    }
}