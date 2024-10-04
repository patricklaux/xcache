package com.igeeksky.xcache.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 定时任务调度器配置项
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/12
 */
@Configuration
@ConfigurationProperties(prefix = "xcache.scheduler")
@AutoConfigureBefore({SchedulerAutoConfiguration.class})
public class SchedulerProperties {

    /**
     * 定时任务调度器核心线程数（可不填）
     * <p>
     * 既知可能用于以下组件：<p>
     * 1. EmbedCacheRefreshProvider {@link com.igeeksky.xcache.extension.refresh.EmbedCacheRefreshProvider }<p>
     * 2. RedisCacheRefreshProvider {@code com.igeeksky.xcache.redis.refresh.RedisCacheRefreshProvider }<p>
     * 3. LogCacheStatProvider {@link com.igeeksky.xcache.extension.stat.LogCacheStatProvider } <p>
     * 4. RedisCacheStatProvider {@code com.igeeksky.xcache.redis.stat.RedisCacheStatProvider } <p>
     * 5. StreamListenerContainer {@code com.igeeksky.redis.stream.StreamListenerContainer } <p>
     * 以上组件的定时任务的实际执行均使用虚拟线程，因此不会过多占用此调度器的线程资源。
     * <p>
     * 如果未配置，则使用 (核心数 / 8)，最小为 1 <p>
     * {@snippet :
     *     int corePoolSize = Math.max(1, Runtime.getRuntime().availableProcessors() / 8);
     *}
     */
    private Integer corePoolSize;

    /**
     * 默认构造函数
     */
    public SchedulerProperties() {
    }

    /**
     * 定时任务调度器核心线程数（可为空）
     *
     * @return Integer – 核心线程数
     */
    public Integer getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * 设置核心线程数
     *
     * @param corePoolSize 核心线程数
     */
    public void setCorePoolSize(Integer corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

}