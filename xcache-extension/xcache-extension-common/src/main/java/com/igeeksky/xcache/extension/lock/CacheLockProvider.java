package com.igeeksky.xcache.extension.lock;

import com.igeeksky.xcache.common.Provider;
import com.igeeksky.xcache.config.props.CacheProps;

/**
 * 缓存锁工厂，用于获取 {@link CacheLock}
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
public interface CacheLockProvider extends Provider {

    /**
     * 获取缓存锁的Holder
     */
    CacheLock get(CacheProps cacheProps);

}
