package com.igeeksky.xcache;

/**
 * <p>缓存</p>
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-05
 */
public interface Cache<K, V> extends Base<K, V> {

    String getName();

    Class<K> getKeyType();

    Class<V> getValueType();

    V get(K key, CacheLoader<K, V> cacheLoader);

}