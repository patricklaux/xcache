package com.igeeksky.xcache.core.store;

import com.igeeksky.xcache.common.Store;

/**
 * 存储器提供者
 * <p>
 * 根据配置生成缓存存储器实例
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/13
 */
public interface StoreProvider {

    /**
     * 根据配置生成缓存实例
     *
     * @param config 缓存配置
     * @param <V>    缓存值类型
     * @return 缓存存储器
     */
    <V> Store<V> getStore(StoreConfig<V> config);

}