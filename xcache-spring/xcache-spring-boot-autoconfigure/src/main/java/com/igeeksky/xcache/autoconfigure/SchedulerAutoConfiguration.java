package com.igeeksky.xcache.autoconfigure;

import com.igeeksky.xtool.core.concurrent.PlatformThreadFactory;
import com.igeeksky.xtool.core.lang.Assert;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * 定时任务调度器自动配置
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/17
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({CacheAutoConfiguration.class})
@EnableConfigurationProperties({SchedulerProperties.class})
public class SchedulerAutoConfiguration {

    private final SchedulerProperties schedulerProperties;

    /**
     * 构造函数，接收一个 SchedulerProperties 对象作为参数。
     *
     * @param schedulerProperties 调度器配置项
     */
    SchedulerAutoConfiguration(SchedulerProperties schedulerProperties) {
        this.schedulerProperties = schedulerProperties;
    }

    /**
     * 创建一个 ScheduledExecutorService 实例，用于执行定时任务。
     *
     * @return ScheduledExecutorService – 调度器
     */
    @Bean
    ScheduledExecutorService scheduler() {
        // 根据配置属性动态确定线程池的核心大小
        int corePoolSize = getCorePoolSize(schedulerProperties.getCorePoolSize());
        // 创建一个线程工厂，用于生成具有统一前缀名称的线程
        ThreadFactory threadFactory = new PlatformThreadFactory("cache-scheduler-");
        // 使用指定的核心线程数、线程工厂和拒绝策略创建ScheduledThreadPoolExecutor实例
        // 当任务提交过多，线程池无法处理时，采取AbortPolicy策略，即抛出RejectedExecutionException异常
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
    }

    private int getCorePoolSize(Integer corePoolSize) {
        if (corePoolSize == null) {
            return Math.max(1, Runtime.getRuntime().availableProcessors() / 8);
        }
        Assert.isTrue(corePoolSize > 0, "corePoolSize must be greater than 0");
        return corePoolSize;
    }

}