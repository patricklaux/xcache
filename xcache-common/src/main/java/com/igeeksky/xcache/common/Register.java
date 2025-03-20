package com.igeeksky.xcache.common;

import java.util.Map;

/**
 * 组件注册接口
 *
 * @param <T> 缓存组件类型
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-29
 */
public interface Register<T> {

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
