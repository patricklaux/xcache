package com.igeeksky.xcache.extension.sync;

import java.util.Set;

/**
 * 监听缓存数据更新事件
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/9
 */
public interface CacheSyncMonitor {

    /**
     * 监听缓存数据单个存储事件
     *
     * @param key 缓存键
     */
    void afterPut(String key);

    /**
     * 监听缓存数据批量存储事件
     *
     * @param keys 缓存键
     */
    void afterPutAll(Set<String> keys);

    /**
     * 监听缓存数据单个逐出事件
     *
     * @param key 缓存键
     */
    void afterEvict(String key);

    /**
     * 监听缓存数据批量逐出事件
     *
     * @param keys 缓存键
     */
    void afterEvictAll(Set<String> keys);

    /**
     * 监听缓存清空事件
     */
    void afterClear();

}
