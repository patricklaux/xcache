package com.igeeksky.xcache.extension.statistic;

import java.util.List;

/**
 * 缓存指标数据发布
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-11
 */
public interface CacheStatisticsPublisher {

    void publish(List<CacheStatisticsMessage> messages);

    <K, V> CacheStatisticsMonitor<K, V> getStatisticsMonitor(String name, String storeType, String application);

}
