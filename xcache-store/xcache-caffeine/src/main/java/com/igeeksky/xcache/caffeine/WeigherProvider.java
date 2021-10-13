package com.igeeksky.xcache.caffeine;

import com.github.benmanes.caffeine.cache.Weigher;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.Provider;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-10-05
 */
public interface WeigherProvider extends Provider {

    <K, V> Weigher<K, CacheValue<V>> get(String name, Class<K> keyType, Class<V> valueType);

}
