package com.igeeksky.xcache.extension.sync;

import java.util.Set;

/**
 * 缓存同步监视器的无操作实现
 */
public class NoOpCacheSyncMonitor implements CacheSyncMonitor {

    private static final NoOpCacheSyncMonitor INSTANCE = new NoOpCacheSyncMonitor();

    private NoOpCacheSyncMonitor() {
    }

    public static NoOpCacheSyncMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    public void afterPut(String key) {
    }

    @Override
    public void afterPutAll(Set<String> keys) {
    }

    @Override
    public void afterRemove(String key) {
    }

    @Override
    public void afterRemoveAll(Set<String> keys) {
    }

    @Override
    public void afterClear() {
    }

}
