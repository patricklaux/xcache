package com.igeeksky.xcache.extension.metrics;

import com.igeeksky.xcache.common.MessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志打印缓存统计信息
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/8/31
 */
public class LogCacheMetricsPublisher implements MessagePublisher<CacheMetricsMessage> {

    private static final Logger log = LoggerFactory.getLogger(LogCacheMetricsPublisher.class);

    private static final LogCacheMetricsPublisher INSTANCE = new LogCacheMetricsPublisher();

    public static LogCacheMetricsPublisher getInstance() {
        return INSTANCE;
    }

    @Override
    public void publish(CacheMetricsMessage message) {
        if (log.isInfoEnabled()) {
            log.info(message.toString());
        }
    }

}
