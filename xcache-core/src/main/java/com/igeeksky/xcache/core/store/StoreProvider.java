package com.igeeksky.xcache.core.store;

import com.igeeksky.xcache.common.Store;

/**
 * Store 工厂类
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface StoreProvider {

    /**
     * 根据配置创建 Store 对象实例
     *
     * @param config 缓存配置
     * @param <V>    缓存值类型
     * @return 缓存存储器
     */
    <V> Store<V> getStore(StoreConfig<V> config);

}