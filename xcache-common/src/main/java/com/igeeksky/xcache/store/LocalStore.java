package com.igeeksky.xcache.store;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.CacheValues;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-16
 */
public interface LocalStore extends Store<String, Object> {

    @Override
    default void putAll(Map<? extends String, ?> keyValues) {
        Map<String, CacheValue<Object>> newMap = new LinkedHashMap<>();
        keyValues.forEach((k, v) -> newMap.put(k, new CacheValue<>(v)));
        this.doPutAll(newMap);
    }

    void doPutAll(Map<String, CacheValue<Object>> keyValues);

    @Override
    default void put(String key, Object value) {
        doPut(key, CacheValues.newCacheValue(value));
    }

    void doPut(String key, CacheValue<Object> value);
}
