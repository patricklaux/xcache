package com.igeeksky.xcache.common;

/**
 * 存储层类型
 * <p>
 * 主要用于区分存储层类型，生成默认的配置信息
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-14
 */
public enum StoreType {

    /**
     * 内嵌缓存
     */
    EMBED,

    /**
     * 外部缓存
     */
    EXTRA

}