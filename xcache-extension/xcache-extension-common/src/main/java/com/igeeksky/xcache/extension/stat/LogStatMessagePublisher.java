package com.igeeksky.xcache.extension.stat;

import com.igeeksky.xcache.common.MessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志打印缓存统计信息
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/8/31
 */
public class LogStatMessagePublisher implements MessagePublisher<CacheStatMessage> {

    private static final Logger log = LoggerFactory.getLogger(LogStatMessagePublisher.class);

    private static final LogStatMessagePublisher INSTANCE = new LogStatMessagePublisher();

    public static LogStatMessagePublisher getInstance() {
        return INSTANCE;
    }

    @Override
    public void publish(CacheStatMessage message) {
        if (log.isInfoEnabled()) {
            log.info(message.toString());
        }
    }

}
