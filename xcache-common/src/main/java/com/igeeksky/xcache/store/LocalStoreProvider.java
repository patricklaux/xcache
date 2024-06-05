package com.igeeksky.xcache.store;

import com.igeeksky.xcache.common.Provider;
import com.igeeksky.xcache.config.CacheConfig;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
public interface LocalStoreProvider extends Provider {

    /**
     * 根据配置生成缓存实例
     *
     * @param config 缓存配置
     * @param <K>    键类型
     * @param <V>    值类型
     * @return 缓存存储器
     */
    <K, V> LocalStore getLocalStore(CacheConfig<K, V> config);

    @Override
    default void close() {
        // do nothing
    }
}
