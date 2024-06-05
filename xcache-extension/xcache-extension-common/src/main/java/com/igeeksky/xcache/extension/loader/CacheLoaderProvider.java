package com.igeeksky.xcache.extension.loader;

import com.igeeksky.xcache.config.props.CacheProps;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-23
 */
public interface CacheLoaderProvider {

    <K, V> CacheLoader<K, V> getCacheLoader(Class<K> keyType, Class<V> valueType, CacheProps props);

}
