package com.igeeksky.xcache.autoconfigure;

import com.igeeksky.xcache.extension.refresh.EmbedCacheRefreshProvider;
import com.igeeksky.xcache.extension.stat.LogCacheStatProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存定时任务的调度器
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/12
 */
@Configuration
@ConfigurationProperties(prefix = "xcache.scheduler")
@AutoConfigureBefore({SchedulerAutoConfiguration.class})
public class SchedulerProperties {

    /**
     * 定时任务调度器核心线程数 <p>
     * 既知可能用于以下组件：<p>
     * 1. 缓存数据刷新 {@link EmbedCacheRefreshProvider}；<p>
     * 2. 缓存数据刷新 {@link com.igeeksky.xcache.redis.refresh.RedisCacheRefreshProvider}；<p>
     * 3. 缓存指标统计 {@link LogCacheStatProvider}；<p>
     * 4. 缓存指标统计 {@link com.igeeksky.xcache.redis.stat.RedisCacheStatProvider}；<p>
     * 5. Stream监听（用于缓存数据同步） {@link com.igeeksky.redis.stream.StreamListenerContainer}；<p>
     * <p>
     * 以上组件的任务非常轻量，涉及到 IO 等待的均额外使用了虚拟线程，并不占用此调度器线程的工作时间。
     * <p>
     * 如果未配置，则使用 (核心数 / 8)，最小为 1 <p>
     * {@code Math.max(1, Runtime.getRuntime().availableProcessors() / 8)}
     */
    private Integer corePoolSize;

    public Integer getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(Integer corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

}