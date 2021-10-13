package com.igeeksky.xcache;

import com.igeeksky.xcache.common.Provider;
import com.igeeksky.xcache.config.CacheProperties;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-10
 */
public interface CacheProvider extends Provider {

    <K, V> Cache<K, V> get(String name, CacheProperties cacheProperties, Class<K> keyType, Class<V> valueType);

}
