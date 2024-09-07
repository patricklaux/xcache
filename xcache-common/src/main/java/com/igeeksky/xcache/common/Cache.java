package com.igeeksky.xcache.common;

import java.util.Map;
import java.util.Set;

/**
 * 缓存接口
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-05
 */
public interface Cache<K, V> extends Base<K, V> {

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    String getName();

    /**
     * 获取缓存键的类型
     *
     * @return 缓存键的类型
     */
    Class<K> getKeyType();

    /**
     * 获取缓存键的泛型参数类型
     *
     * @return 缓存键的泛型参数类型
     */
    Class<?>[] getKeyParams();

    /**
     * 获取缓存值的类型
     *
     * @return 缓存值的类型
     */
    Class<V> getValueType();

    /**
     * 获取缓存值的泛型参数类型
     *
     * @return 缓存值的泛型参数类型
     */
    Class<?>[] getValueParams();

    /**
     * 先从缓存取值，如果缓存无值，但配置了 CacheLoader，则通过 cacheLoader 回源取值
     * <p>
     * 注意：为减少回源次数，回源时加锁执行
     *
     * @param key 键
     * @return CacheValue – 值的包装类
     */
    V getOrLoad(K key);

    /**
     * 先从缓存取值，如果缓存无值，则通过 cacheLoader 回源取值
     * <p>
     * 注意：回源时加锁，避免多个线程同时回源，导致缓存击穿
     *
     * @param key         键
     * @param cacheLoader 回源函数
     * @return 值
     */
    V get(K key, CacheLoader<K, V> cacheLoader);

    /**
     * 先从缓存取值，如果缓存无值，但配置了 CacheLoader，则通过 cacheLoader 回源取值
     * <p>
     * <b>注意</b>：<p>
     * 批量回源取值不加锁，如希望加锁，请循环调用 {@link #getOrLoad(Object)}
     *
     * @param keys 键集
     * @return 键值对集合
     */
    Map<K, V> getOrLoadAll(Set<? extends K> keys);

    /**
     * 先从缓存取值，如果缓存无值，则通过 cacheLoader 回源取值
     * <p>
     * <b>注意</b>：<p>
     * 批量回源取值不加锁，如希望加锁，请循环调用 {@link #get(Object, CacheLoader)}
     *
     * @param keys        键集
     * @param cacheLoader 回源函数
     * @return 键值对集合
     */
    Map<K, V> getAll(Set<? extends K> keys, CacheLoader<K, V> cacheLoader);

    void setCacheLoader(CacheLoader<K, V> cacheLoader);

    void setCacheWriter(CacheWriter<K, V> cacheWriter);

    void setCacheRefresh(CacheRefresh cacheRefresh, CacheLoader<K, V> cacheLoader);

}