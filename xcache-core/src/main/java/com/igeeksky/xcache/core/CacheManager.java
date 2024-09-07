package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xcache.common.CacheWriter;
import com.igeeksky.xcache.core.store.StoreProvider;
import com.igeeksky.xcache.extension.codec.CodecProvider;
import com.igeeksky.xcache.extension.compress.CompressorProvider;
import com.igeeksky.xcache.extension.contains.ContainsPredicateProvider;
import com.igeeksky.xcache.extension.lock.CacheLockProvider;
import com.igeeksky.xcache.extension.refresh.CacheRefreshProvider;
import com.igeeksky.xcache.extension.stat.CacheStatProvider;
import com.igeeksky.xcache.extension.sync.CacheSyncProvider;

import java.util.Collection;

/**
 * 缓存管理者接口
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-10
 */
public interface CacheManager {

    /**
     * 获取或创建指定名称的缓存
     *
     * @param cacheName 缓存名称，用于唯一标识一个缓存
     * @param keyType   缓存键类型
     * @param valueType 缓存值类型
     * @param <K>       泛型参数，表示键的类型
     * @param <V>       泛型参数，表示值的类型
     * @return 返回已存在或新创建的缓存对象
     * <p>
     * 此方法用于根据缓存名称和指定的键值类型获取一个缓存实例。<p>
     * 如果指定名称的缓存已经存在，则直接返回；否则，将根据指定的名称和类型创建一个新的缓存并返回。
     */
    default <K, V> Cache<K, V> getOrCreateCache(String cacheName, Class<K> keyType, Class<V> valueType) {
        return getOrCreateCache(cacheName, keyType, null, valueType, null);
    }

    /**
     * 获取或创建指定名称的缓存
     *
     * @param cacheName   缓存名称，用于唯一标识一个缓存
     * @param keyType     缓存键类型
     * @param keyParams   缓存键的泛型参数（用于较复杂的带泛型参数的键类型的序列化处理）
     * @param valueType   缓存值类型
     * @param valueParams 缓存值的泛型参数（用于较复杂的带泛型参数的值类型的序列化处理）
     * @param <K>         表示键的类型
     * @param <V>         表示值的类型
     * @return 返回已存在或新创建的缓存对象
     */
    <K, V> Cache<K, V> getOrCreateCache(String cacheName, Class<K> keyType, Class<?>[] keyParams, Class<V> valueType, Class<?>[] valueParams);

    /**
     * 获取所有缓存对象
     *
     * @return 所有缓存对象
     */
    Collection<Cache<?, ?>> getAll();

    /**
     * 获取所有缓存名称
     *
     * @return 所有缓存名称
     */
    Collection<String> getAllCacheNames();

    /**
     * 添加编解码器提供者
     *
     * @param beanId   编解码器提供者的唯一标识
     * @param provider 编解码器提供者
     */
    void addProvider(String beanId, CodecProvider provider);

    /**
     * 添加压缩器提供者
     *
     * @param beanId   压缩器提供者的唯一标识
     * @param provider 压缩器提供者
     */
    void addProvider(String beanId, CompressorProvider provider);

    /**
     * 添加缓存数据同步提供者
     *
     * @param beanId   缓存数据同步提供者的唯一标识
     * @param provider 缓存数据同步提供者
     */
    void addProvider(String beanId, CacheSyncProvider provider);

    /**
     * 添加缓存统计提供者
     *
     * @param beanId   缓存统计提供者的唯一标识
     * @param provider 缓存统计提供者
     */
    void addProvider(String beanId, CacheStatProvider provider);

    /**
     * 添加缓存锁提供者
     *
     * @param beanId   缓存锁提供者的唯一标识
     * @param provider 缓存锁提供者
     */
    void addProvider(String beanId, CacheLockProvider provider);

    /**
     * 添加缓存键值存在断言提供者
     *
     * @param beanId   缓存键值存在断言提供者的唯一标识
     * @param provider 缓存键值存在断言提供者
     */
    void addProvider(String beanId, ContainsPredicateProvider provider);

    /**
     * 添加缓存存储提供者
     *
     * @param beanId   缓存存储提供者的唯一标识
     * @param provider 缓存存储提供者
     */
    void addProvider(String beanId, StoreProvider provider);

    /**
     * 添加缓存加载器
     *
     * @param beanId   缓存加载器的唯一标识
     * @param loader   缓存加载器
     */
    void addCacheLoader(String beanId, CacheLoader<?, ?> loader);

    /**
     * 添加缓存写入器
     *
     * @param beanId   缓存写入器的唯一标识
     * @param writer   缓存写入器
     */
    void addCacheWriter(String beanId, CacheWriter<?, ?> writer);

    /**
     * 添加缓存刷新提供者
     *
     * @param beanId   缓存刷新提供者的唯一标识
     * @param provider 缓存刷新提供者
     */
    void addProvider(String beanId, CacheRefreshProvider provider);

}