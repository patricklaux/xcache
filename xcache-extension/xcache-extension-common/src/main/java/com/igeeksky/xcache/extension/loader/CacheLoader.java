package com.igeeksky.xcache.extension.loader;

/**
 * 从数据源读取数据
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-05
 */
@FunctionalInterface
public interface CacheLoader<K, V> {

    V load(K key);

}
