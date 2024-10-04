package com.igeeksky.xcache.extension.sync;

import java.util.Set;

/**
 * 缓存同步监视器的无操作实现
 */
public class NoopCacheSyncMonitor implements CacheSyncMonitor {

    private static final NoopCacheSyncMonitor INSTANCE = new NoopCacheSyncMonitor();

    private NoopCacheSyncMonitor() {
    }

    public static NoopCacheSyncMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    public void afterPut(String key) {
    }

    @Override
    public void afterPutAll(Set<String> keys) {
    }

    @Override
    public void afterEvict(String key) {
    }

    @Override
    public void afterEvictAll(Set<String> keys) {
    }

    @Override
    public void afterClear() {
    }

}
