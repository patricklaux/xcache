package com.igeeksky.xcache.extension.statistic;

import com.igeeksky.xcache.common.Provider;
import com.igeeksky.xcache.common.SPI;

/**
 * 缓存指标采集器 工厂
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
@SPI
public interface CacheStatisticsProvider extends Provider {

    CacheStatisticsPublisher get();
    
}
