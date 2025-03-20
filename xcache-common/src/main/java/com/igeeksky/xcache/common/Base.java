package com.igeeksky.xcache.common;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 缓存基础接口
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-03
 */
public interface Base<K, V> {

    /**
     * 根据键从缓存中读取值
     * <p>
     * 增加一层包装类 CacheValue ，当允许缓存空值时，可以明确判断是缓存无值，还是数据源无值，从而决定是否需要再回源查询
     * {@snippet :
     * if (cacheValue == null) {
     *     // 未缓存，未知数据源是否有数据
     *     doSth();
     * } else {
     *     if (cacheValue.hasValue()){
     *         // 已缓存，数据源有数据
     *         doSth();
     *     } else {
     *         // 已缓存，数据源无数据，无需回源（只有允许缓存空值，才会出现这个条件）；
     *         doSth();
     *     }
     * }
     *}
     *
     * @param key 键
     * @return CacheValue – 值的包装类
     */
    CacheValue<V> getCacheValue(K key);

    /**
     * 根据键从缓存中读取值（异步）
     * <p>
     * 增加一层包装类 CacheValue ，当允许缓存空值时，可以明确判断是缓存无值，还是数据源无值，从而决定是否需要再回源查询
     * {@snippet :
     * if (cacheValue == null) {
     *     // 未缓存，未知数据源是否有数据
     *     doSth();
     * } else {
     *     if (cacheValue.hasValue()){
     *         // 已缓存，数据源有数据
     *         doSth();
     *     } else {
     *         // 已缓存，数据源无数据，无需回源（只有允许缓存空值，才会出现这个条件）；
     *         doSth();
     *     }
     * }
     *}
     *
     * @param key 键
     * @return {@code CompletableFuture<CacheValue<V>>} – 值的包装类
     */
    CompletableFuture<CacheValue<V>> getCacheValueAsync(K key);

    /**
     * 根据键集从缓存中读取值
     *
     * @param keys 多个键的集合
     * @return KeyValues – 键值对映射<p>
     * CacheValue 不为空，表示已缓存： <p>
     * 1. CacheValue.hasValue() == true，缓存的是正常值； <p>
     * 2. CacheValue.hasValue() == false，缓存的是空值
     */
    Map<K, CacheValue<V>> getAllCacheValues(Set<? extends K> keys);

    /**
     * 根据键集从缓存中读取值
     *
     * @param keys 多个键的集合
     * @return {@code CompletableFuture<Map<K, CacheValue<V>>>} – 键值对映射 <p>
     * CacheValue 不为空，表示已缓存： <p>
     * 1. CacheValue.hasValue() == true，缓存的是正常值； <p>
     * 2. CacheValue.hasValue() == false，缓存的是空值； <p>
     */
    CompletableFuture<Map<K, CacheValue<V>>> getAllCacheValuesAsync(Set<? extends K> keys);

    /**
     * 将单个键值对存入缓存
     *
     * @param key   键
     * @param value 值
     */
    void put(K key, V value);

    /**
     * 将单个键值对存入缓存
     *
     * @param key   键
     * @param value 值
     * @return {@code CompletableFuture<Void>} – void
     */
    CompletableFuture<Void> putAsync(K key, V value);

    /**
     * 将多个键值对存入缓存
     *
     * @param keyValues 键值对的集合
     */
    void putAll(Map<? extends K, ? extends V> keyValues);

    /**
     * 将多个键值对存入缓存
     *
     * @param keyValues 键值对的集合
     * @return {@code CompletableFuture<Void>} – void
     */
    CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> keyValues);

    /**
     * 根据键将数据逐出缓存
     *
     * @param key 键
     */
    void remove(K key);

    /**
     * 根据键将数据逐出缓存
     *
     * @param key 键
     * @return {@code CompletableFuture<Void>} – void
     */
    CompletableFuture<Void> removeAsync(K key);

    /**
     * 根据键集将数据逐出缓存
     *
     * @param keys 键集
     */
    void removeAll(Set<? extends K> keys);

    /**
     * 根据键集将数据逐出缓存
     *
     * @param keys 键集
     * @return {@code CompletableFuture<Void>} – void
     */
    CompletableFuture<Void> removeAllAsync(Set<? extends K> keys);

    /**
     * 清空缓存中的所有数据
     * <p>
     * Redis 缓存，当使用 Redis-String 作为数据存储，
     * 实现类采用 ScanCursor 删除数据，数据量大时，此方法会耗时较长，请谨慎使用。
     */
    void clear();

}