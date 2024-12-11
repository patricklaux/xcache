package com.igeeksky.xcache.extension.refresh;


import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 缓存刷新
 * <p>
 * 保存所有访问记录，定时使用 consumer 回源读取数据并存入缓存
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/26
 */
public interface CacheRefresh extends AutoCloseable {

    /**
     * 保存访问记录
     *
     * @param key 键
     */
    void onPut(String key);

    /**
     * 批量保存访问记录
     *
     * @param keys 键集
     */
    void onPutAll(Set<String> keys);

    /**
     * 移除访问记录
     *
     * @param key 键
     */
    void onRemove(String key);

    /**
     * 批量移除访问记录
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