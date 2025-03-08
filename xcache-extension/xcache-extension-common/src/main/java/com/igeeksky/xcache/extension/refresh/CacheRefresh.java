package com.igeeksky.xcache.extension.refresh;


import com.igeeksky.xtool.core.GracefulShutdown;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 缓存数据刷新
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface CacheRefresh extends GracefulShutdown {

    /**
     * 监听缓存写入事件
     *
     * @param key 键
     */
    void onPut(String key);

    /**
     * 监听缓存批量写入事件
     *
     * @param keys 键集
     */
    void onPutAll(Set<String> keys);

    /**
     * 监听缓存移除事件
     *
     * @param key 键
     */
    void onRemove(String key);

    /**
     * 监听缓存批量移除事件
     *
     * @param keys 键集
     */
    void onRemoveAll(Set<String> keys);

    /**
     * 设置刷新任务回调函数
     *
     * @param consumer  用以执行回源查询并将回源结果写入缓存
     * @param predicate 用以判断缓存是否存在键对应的值：如果此 {@code predicate} 返回 true，执行数据刷新；否则，不再继续刷新。 <br>
     *                  1. 缓存存储系统可能会根据特定算法清除某些访问频率较低的键，增加此判断可以避免缓存算法失效，导致命中率降低； <br>
     *                  2. {@link CacheRefresh#onRemove} 执行异常，导致未能移除无需再刷新的键，增加此判断可及时清理垃圾键集。
     */
    void startRefresh(Consumer<String> consumer, Predicate<String> predicate);

}