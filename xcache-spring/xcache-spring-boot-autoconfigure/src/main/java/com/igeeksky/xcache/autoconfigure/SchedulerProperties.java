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
     * 1. EmbedCacheRefreshProvider <br>
     * 2. RedisCacheRefreshProvider <br>
     * 3. LogCacheMetricsProvider <br>
     * 4. RedisCacheMetricsProvider <br>
     * 5. StreamContainer <br>
     * 以上组件的定时任务执行大部分使用虚拟线程或异步执行，因此只会占用此调度器极少的线程资源。
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