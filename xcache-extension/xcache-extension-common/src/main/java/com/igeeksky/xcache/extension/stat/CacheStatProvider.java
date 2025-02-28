package com.igeeksky.xcache.extension.stat;

import java.util.List;

/**
 * 缓存指标统计接口
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-11
 */
public interface CacheStatProvider extends AutoCloseable {

    /**
     * 发布缓存指标统计消息
     *
     * @param messages 缓存指标统计消息
     */
    void publish(List<CacheStatMessage> messages);

    /**
     * 获取缓存指标监控器
     *
     * @param config 缓存指标统计配置
     * @return 缓存指标监控器
     */
    CacheStatMonitor getMonitor(StatConfig config);

}