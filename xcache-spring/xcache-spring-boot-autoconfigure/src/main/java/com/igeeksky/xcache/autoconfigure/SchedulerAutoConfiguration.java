package com.igeeksky.xcache.autoconfigure;

import com.igeeksky.xtool.core.concurrent.PlatformThreadFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/17
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({CacheAutoConfiguration.class})
@AutoConfigureAfter({SchedulerProperties.class})
public class SchedulerAutoConfiguration {

    private final SchedulerProperties schedulerProperties;

    public SchedulerAutoConfiguration(SchedulerProperties schedulerProperties) {
        this.schedulerProperties = schedulerProperties;
    }

    /**
     * 创建一个 ScheduledExecutorService 实例，用于执行定时任务。<p>
     *
     * @return ScheduledExecutorService 用于执行定时任务。
     */
    @Bean
    public ScheduledExecutorService scheduler() {
        // 根据配置属性动态确定线程池的核心大小
        int coreSize = getCorePoolSize(schedulerProperties.getCorePoolSize());
        // 创建一个线程工厂，用于生成具有统一前缀名称的线程
        ThreadFactory threadFactory = new PlatformThreadFactory("cache-scheduler-thread-");
        // 使用指定的核心线程数、线程工厂和拒绝策略创建ScheduledThreadPoolExecutor实例
        // 当任务提交过多，线程池无法处理时，采取AbortPolicy策略，即抛出RejectedExecutionException异常
        return new ScheduledThreadPoolExecutor(coreSize, threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    private int getCorePoolSize(Integer corePoolSize) {
        if (corePoolSize == null || corePoolSize <= 0) {
            return Math.max(1, Runtime.getRuntime().availableProcessors() / 8);
        }
        return corePoolSize;
    }

}