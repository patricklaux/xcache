package com.igeeksky.xcache.extension.statistic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 缓存统计信息打印到日志
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-19
 */
public class LogCacheStatManager extends AbstractCacheStatManager {

    private static final Logger log = LoggerFactory.getLogger(LogCacheStatManager.class);

    public LogCacheStatManager(long period) {
        super(period);
    }

    @Override
    public void publish(List<CacheStatMessage> messages) {
        if (log.isInfoEnabled()) {
            for (CacheStatMessage message : messages) {
                log.info(message.toString());
            }
        }
    }

}
