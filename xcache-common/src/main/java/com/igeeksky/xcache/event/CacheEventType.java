package com.igeeksky.xcache.event;

/**
 * 缓存事件类型
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-25
 */
public enum CacheEventType {

    /**
     * 单键取值事件
     */
    GET,

    /**
     * 多键取值事件
     */
    GET_ALL,

    /**
     * 单键回源加载事件
     */
    LOAD,

    /**
     * 多键回源加载事件
     */
    LOAD_ALL,

    /**
     * 单键值保存事件
     */
    PUT,

    /**
     * 多键值保存事件
     */
    PUT_ALL,

    /**
     * 单键删除事件
     */
    REMOVE,

    /**
     * 多键删除事件
     */
    REMOVE_ALL,

    /**
     * 缓存清空事件
     */
    CLEAR

}
