package com.igeeksky.xcache.extension.writer;

/**
 * 缓存写策略
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/2
 */
public enum CacheWriteStrategy {

    /**
     * 写入缓存时同步写入存储
     */
    WRITE_THROUGH,

    /**
     * 写入缓存时异步写入存储
     */
    WRITE_BEHIND

}
