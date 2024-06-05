package com.igeeksky.xcache.core.store;


import com.igeeksky.xcache.common.Provider;
import com.igeeksky.xcache.core.config.CacheConfig;

/**
 * 缓存存储器工厂类接口
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-10
 */
public interface RemoteStoreProvider extends Provider {

    /**
     * 根据配置生成缓存实例
     *
     * @param config 缓存配置
     * @return 缓存存储器
     */
    RemoteStore getRemoteCacheStore(CacheConfig<?, ?> config);

}
