package com.igeeksky.xcache.props;

/**
 * 数据同步通知
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/11
 */
public enum SyncType {

    /**
     * 不处理所有数据同步事件
     */
    NONE,

    /**
     * 仅处理 clear 数据同步事件
     */
    CLEAR,

    /**
     * 处理所有数据同步事件（put, putAll, evict, evictAll, clear）
     */
    ALL

}
