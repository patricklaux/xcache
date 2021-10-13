package com.igeeksky.xcache.extension.serialization;

import com.igeeksky.xcache.common.SPI;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-06
 */
@SPI
public interface CacheEventSerializerProvider {

    <K, V> CacheEventSerializer<K, V> get(Class<K> keyType, Class<V> valueType);

}
