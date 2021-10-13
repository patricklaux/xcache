package com.igeeksky.xcache.extension.update;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-24
 */
public interface CacheEventPolicy<K, V> {

    void onMessage(byte[] msgBytes);

}
