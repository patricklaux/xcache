package com.igeeksky.xcache;

import com.igeeksky.xcache.common.Provider;
import com.igeeksky.xcache.config.CacheProperties;

/**
 * 缓存工厂类接口
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-10
 */
public interface CacheProvider extends Provider {

    /**
     * 根据配置生成缓存实例
     *
     * @param name            缓存名称
     * @param cacheProperties 缓存配置
     * @param keyType         键的数据类型
     * @param valueType       值的数据类型
     * @param <K>             键类型
     * @param <V>             值类型
     * @return 缓存
     */
    <K, V> Cache<K, V> get(String name, CacheProperties cacheProperties, Class<K> keyType, Class<V> valueType);

}
