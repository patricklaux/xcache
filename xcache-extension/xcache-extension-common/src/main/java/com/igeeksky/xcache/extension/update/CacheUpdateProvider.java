package com.igeeksky.xcache.extension.update;

import com.igeeksky.xcache.common.Provider;

/**
 * 缓存数据更新：发布器及监听器工厂
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-12
 */
public interface CacheUpdateProvider extends Provider {

    CacheUpdatePublisher getPublisher(String name);

    <K, V> void register(String name, CacheUpdatePolicy<K, V> cacheUpdatePolicy);

}

