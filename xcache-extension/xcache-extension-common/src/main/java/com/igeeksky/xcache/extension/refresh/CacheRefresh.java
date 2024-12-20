package com.igeeksky.xcache.extension.refresh;


import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 缓存数据刷新
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface CacheRefresh extends AutoCloseable {

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
     * 设置刷新任务消费回调
     *
     * @param consumer  刷新任务消费接口（回源查询并写入缓存）
     * @param predicate 刷新任务过滤接口（判断键是否需要刷新）
     */
    void startRefresh(Consumer<String> consumer, Predicate<String> predicate);

}