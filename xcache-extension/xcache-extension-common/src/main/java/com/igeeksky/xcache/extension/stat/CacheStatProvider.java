package com.igeeksky.xcache.extension.stat;

import java.util.List;

/**
 * 缓存命中率指标统计及数据发布
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-11
 */
public interface CacheStatProvider {

    void publish(List<CacheStatMessage> messages);

    CacheStatMonitor getMonitor(StatConfig config);

}