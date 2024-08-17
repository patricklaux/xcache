package com.igeeksky.xcache.extension.sync;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public interface CacheSyncProvider {

    <V> void register(byte[] channel, SyncMessageListener<V> consumer);

    <V> CacheSyncMonitor getMonitor(SyncConfig<V> config);

}
