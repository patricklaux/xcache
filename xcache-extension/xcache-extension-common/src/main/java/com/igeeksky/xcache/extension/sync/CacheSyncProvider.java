package com.igeeksky.xcache.extension.sync;

import java.nio.charset.Charset;

/**
 * 缓存数据同步器工厂接口
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public interface CacheSyncProvider {

    /**
     * 注册缓存数据同步消费者
     *
     * @param channel  缓存数据同步通道
     * @param consumer 消费者
     */
    <V> void register(String channel, Charset charset, SyncMessageListener<V> consumer);

    /**
     * 获取缓存数据同步监控器
     *
     * @param <V>    缓存值类型
     * @param config 缓存数据同步配置
     * @return {@code CacheSyncMonitor} - 缓存数据同步监控器
     */
    <V> CacheSyncMonitor getMonitor(SyncConfig<V> config);

}
