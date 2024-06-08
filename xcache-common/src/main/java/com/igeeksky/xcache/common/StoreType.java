package com.igeeksky.xcache.common;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-14
 */
public enum StoreType {

    /**
     * 无缓存(空操作)
     */
    NOOP,

    /**
     * 本地缓存
     */
    LOCAL,

    /**
     * 远程缓存
     */
    REMOTE

}
