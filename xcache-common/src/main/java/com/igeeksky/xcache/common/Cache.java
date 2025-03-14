package com.igeeksky.xcache.common;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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
     * 获取缓存值的类型
     *
     * @return 缓存值的类型
     */
    Class<V> getValueType();

    /**
     * 获取缓存值
     *
     * @param key 键
     * @return 值（返回值为原始值）
     */
    V get(K key);

    /**
     * 获取缓存值（异步）
     *
     * @param key 键
     * @return 值（返回值为原始值）
     */
    CompletableFuture<V> getAsync(K key);

    /**
     * 1. 先从缓存取值，如果缓存有命中，返回已缓存的值。<p>
     * 2. 如果缓存未命中：<p>
     * 2.1. 有配置 CacheLoader，则通过 cacheLoader 回源取值，取值结果先存入缓存，最后返回该结果；<p>
     * 2.2. 未配置 CacheLoader，返回 null。
     * <p>
     * 注：回源时内部加锁执行。
     *
     * @param key 键
     * @return 值
     */
    V getOrLoad(K key);

    /**
     * 1. 先从缓存取值，如果缓存有命中，返回已缓存的值。<p>
     * 2. 如果缓存未命中：<p>
     * 2.1. 有配置 CacheLoader，则通过 cacheLoader 回源取值，取值结果先存入缓存，最后返回该结果；<p>
     * 2.2. 未配置 CacheLoader，返回 null。
     * <p>
     * 注：回源时内部加锁执行。
     *
     * @param key 键
     * @return 值
     */
    CompletableFuture<V> getOrLoadAsync(K key);

    /**
     * 1. 先从缓存取值，如果缓存有命中，返回已缓存的值。<p>
     * 2. 如果缓存未命中，通过 cacheLoader 回源取值，取值结果存入缓存并返回。
     * <p>
     * 注：回源时内部加锁执行。
     *
     * @param key         键
     * @param cacheLoader 回源函数
     * @return 值
     */
    V getOrLoad(K key, CacheLoader<K, V> cacheLoader);

    /**
     * 1. 先从缓存取值，如果缓存有命中，返回已缓存的值。<p>
     * 2. 如果缓存未命中，通过 cacheLoader 回源取值，取值结果存入缓存并返回。
     * <p>
     * 注：回源时内部加锁执行。
     *
     * @param key         键
     * @param cacheLoader 回源函数
     * @return {@code CompletableFuture<V>} – 值
     */
    CompletableFuture<V> getOrLoadAsync(K key, CacheLoader<K, V> cacheLoader);

    /**
     * 批量获取缓存值
     *
     * @param keys 键集
     * @return 键值对集合
     */
    Map<K, V> getAll(Set<? extends K> keys);

    /**
     * 批量获取缓存值
     *
     * @param keys 键集
     * @return {@code CompletableFuture<Map<K, V>>} – 键值对集合
     */
    CompletableFuture<Map<K, V>> getAllAsync(Set<? extends K> keys);

    /**
     * 1. 先从缓存取值，如果缓存命中全部数据，返回缓存数据集。<p>
     * 2. 如果缓存有未命中数据：<p>
     * 2.1. 有配置 CacheLoader，则通过 cacheLoader 回源取值，取值结果先存入缓存，最后返回合并结果集：缓存数据集 + 回源取值结果集。<p>
     * 2.2. 未配置 CacheLoader，返回缓存数据集。
     * <p>
     * 注：批量回源取值不加锁
     *
     * @param keys 键集
     * @return 键值对集合
     */
    Map<K, V> getAllOrLoad(Set<? extends K> keys);

    /**
     * 1. 先从缓存取值，如果缓存命中全部数据，返回缓存数据集。<p>
     * 2. 如果缓存有未命中数据：<p>
     * 2.1. 有配置 CacheLoader，则通过 cacheLoader 回源取值，取值结果先存入缓存，最后返回合并结果集：缓存数据集 + 回源取值结果集。<p>
     * 2.2. 未配置 CacheLoader，返回缓存数据集。
     * <p>
     * 注：批量回源取值不加锁
     *
     * @param keys 键集
     * @return {@code CompletableFuture<Map<K, V>>} – 键值对集合
     */
    CompletableFuture<Map<K, V>> getAllOrLoadAsync(Set<? extends K> keys);

    /**
     * 先从缓存取值，如果缓存无值，则通过 cacheLoader 回源取值
     * <p>
     * <b>注意</b>：<p>
     * 批量回源取值不加锁，如希望加锁，请循环调用 {@link #getOrLoad(Object, CacheLoader)}
     *
     * @param keys        键集
     * @param cacheLoader 回源函数
     * @return 键值对集合
     */
    Map<K, V> getAllOrLoad(Set<? extends K> keys, CacheLoader<K, V> cacheLoader);

    /**
     * 先从缓存取值，如果缓存无值，则通过 cacheLoader 回源取值
     * <p>
     * <b>注意</b>：<p>
     * 批量回源取值不加锁，如希望加锁，请循环调用 {@link #getOrLoadAsync(Object, CacheLoader)}
     *
     * @param keys        键集
     * @param cacheLoader 回源函数
     * @return {@code CompletableFuture<Map<K, V>>} – 键值对集合
     */
    CompletableFuture<Map<K, V>> getAllOrLoadAsync(Set<? extends K> keys, CacheLoader<K, V> cacheLoader);

}