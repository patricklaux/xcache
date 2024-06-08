package com.igeeksky.xcache.extension.statistic;

import com.igeeksky.xcache.common.Provider;

import java.util.List;

/**
 * 缓存指标数据发布
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-11
 */
public interface CacheStatManager extends Provider {

    void publish(List<CacheStatMessage> messages);

    void register(String name, CacheStatMonitor monitor);

}
