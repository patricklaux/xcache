package com.igeeksky.xcache.extension.contain;

/**
 * 判断 cacheName 对应的数据源是否包含 key 对应的 value
 *
 * @author Patrick.Lau
 * @date 2021-07-26
 */
public interface ContainsPredicate<K> {

    boolean test(String cacheName, K key);

}
