package com.igeeksky.xcache.extension.lock;

/**
 * 内嵌缓存锁工厂类
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-06-10
 */
public class EmbedCacheLockProvider implements CacheLockProvider {

    private static final EmbedCacheLockProvider INSTANCE = new EmbedCacheLockProvider();

    public static EmbedCacheLockProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public EmbedLockService get(LockConfig lockConfig) {
        return new EmbedLockService(lockConfig.getInitialCapacity());
    }

}