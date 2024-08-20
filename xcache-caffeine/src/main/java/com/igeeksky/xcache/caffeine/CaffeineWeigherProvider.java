package com.igeeksky.xcache.caffeine;

import com.github.benmanes.caffeine.cache.Weigher;
import com.igeeksky.xcache.core.CacheValue;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
public interface CaffeineWeigherProvider {

    Weigher<String, CacheValue<Object>> get(String name);

}
