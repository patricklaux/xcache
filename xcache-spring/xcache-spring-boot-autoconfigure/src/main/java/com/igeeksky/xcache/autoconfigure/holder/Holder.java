package com.igeeksky.xcache.autoconfigure.holder;

import java.util.Map;

/**
 * 缓存组件容器
 *
 * @param <T> 缓存组件类型
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-29
 */
public interface Holder<T> {

    /**
     * 注册缓存组件
     *
     * @param beanId    缓存组件ID
     * @param component 缓存组件
     */
    void put(String beanId, T component);

    /**
     * 获取缓存组件
     *
     * @param beanId 缓存组件ID
     * @return 缓存组件
     */
    T get(String beanId);

    /**
     * 获取所有缓存组件
     *
     * @return 所有缓存组件
     */
    Map<String, T> getAll();

}
