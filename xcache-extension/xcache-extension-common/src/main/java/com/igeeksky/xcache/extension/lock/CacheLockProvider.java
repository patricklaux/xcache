package com.igeeksky.xcache.extension.lock;

/**
 * 锁工厂，用于获取 {@link LockService}
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
public interface CacheLockProvider {

    /**
     * 获取缓存锁的Holder
     */
    LockService get(LockConfig lockConfig);

}
