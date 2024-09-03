package com.igeeksky.xcache.extension.stat;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 缓存统计提供者
 * <p>
 * 统计信息输出到日志
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-19
 */
public class LogCacheStatProvider extends AbstractCacheStatProvider {

    private static final LogStatMessagePublisher publisher = LogStatMessagePublisher.getInstance();

    public LogCacheStatProvider(ScheduledExecutorService scheduler, long period) {
        super(scheduler, period);
    }

    /**
     * 发布缓存统计信息
     * <p>
     * 此方法用于将缓存统计信息的消息列表写入日志。如果日志级别允许（INFO），将遍历并打印每个缓存的统计信息。
     *
     * @param messages 缓存统计信息的消息列表，每个消息包含特定缓存的统计信息。
     */
    @Override
    public void publish(List<CacheStatMessage> messages) {
        // 遍历消息列表，并打印每个消息的内容
        for (CacheStatMessage message : messages) {
            publisher.publish(message);
        }
    }

}