package com.igeeksky.xcache.core.store;

import com.igeeksky.xcache.Store;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/13
 */
public interface StoreProvider {

    /**
     * 根据配置生成缓存实例
     *
     * @param config 缓存配置
     * @param <V>    值类型
     * @return 缓存存储器
     */
    <V> Store<V> getStore(StoreConfig<V> config);

}