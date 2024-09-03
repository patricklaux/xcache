package com.igeeksky.xcache.core;

import com.igeeksky.xcache.extension.refresh.EmbedCacheRefreshProvider;
import com.igeeksky.xcache.extension.stat.LogCacheStatProvider;
import com.igeeksky.xcache.props.CacheConstants;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 集中管理内嵌基础组件注册
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/18
 */
public class ComponentRegister {

    private final Lock lock = new ReentrantLock();

    private final long statPeriod;
    private final ScheduledExecutorService scheduler;

    private volatile LogCacheStatProvider logCacheStatProvider;
    private volatile EmbedCacheRefreshProvider embedCacheRefreshProvider;

    public ComponentRegister(ScheduledExecutorService scheduler, Long statPeriod) {
        this.scheduler = scheduler;
        this.statPeriod = (statPeriod == null) ? CacheConstants.DEFAULT_STAT_PERIOD : statPeriod;
    }

    /**
     * 添加 EmbedCacheRefreshProvider 到缓存管理器
     * <p>
     * 本方法的目的是为了启用内嵌的缓存刷新机制。它通过创建一个 EmbedCacheRefreshProvider 实例，
     * 并将其注册到缓存管理器中，使得缓存管理器能够管理和调度缓存的刷新任务。
     *
     * @param beanId       用于检查是否与预定义标识符匹配
     * @param cacheManager 缓存管理器实例，用于添加 EmbedCacheRefreshProvider。
     * @return EmbedCacheRefreshProvider 实例，如果 beanId 与 {@link CacheConstants#EMBED_CACHE_REFRESH} 匹配，则返回注册的实例，否则返回 null。
     */
    public EmbedCacheRefreshProvider embedCacheRefresh(String beanId, CacheManagerImpl cacheManager) {
        String lowerBeanId = StringUtils.toLowerCase(beanId);
        if (Objects.equals(CacheConstants.EMBED_CACHE_REFRESH, lowerBeanId)) {
            if (this.embedCacheRefreshProvider == null) {
                lock.lock();
                try {
                    if (this.embedCacheRefreshProvider == null) {
                        this.embedCacheRefreshProvider = new EmbedCacheRefreshProvider(scheduler);
                    }
                } finally {
                    lock.unlock();
                }
            }
            cacheManager.addProvider(CacheConstants.EMBED_CACHE_REFRESH, this.embedCacheRefreshProvider);
            return this.embedCacheRefreshProvider;
        }
        return null;
    }

    /**
     * 添加 LogCacheStatProvider 到缓存管理器
     * 该方法用于将日志缓存统计提供者绑定到指定的缓存管理器上。
     * 如果 beanId 指定的是日志方式的缓存统计，则会尝试创建并注册 LogCacheStatProvider 实例，
     * 并将该实例添加到缓存管理器中。
     * <p>
     * 此方法确保了 LogCacheStatProvider 的单例性质，多次调用添加的是同一实例。
     *
     * @param beanId       bean 标识符，用于判断是否为日志缓存统计。
     * @param cacheManager 缓存管理器实例，用于添加 LogCacheStatProvider
     * @return 如果成功注册了LogCacheStatProvider，则返回该实例；否则返回null。
     */
    public LogCacheStatProvider logCacheStat(String beanId, CacheManagerImpl cacheManager) {
        String lowerBeanId = StringUtils.toLowerCase(beanId);
        if (Objects.equals(CacheConstants.LOG_CACHE_STAT, lowerBeanId)) {
            if (this.logCacheStatProvider == null) {
                lock.lock();
                try {
                    if (this.logCacheStatProvider == null) {
                        this.logCacheStatProvider = new LogCacheStatProvider(scheduler, statPeriod);
                    }
                } finally {
                    lock.unlock();
                }
            }
            cacheManager.addProvider(CacheConstants.LOG_CACHE_STAT, this.logCacheStatProvider);
            return this.logCacheStatProvider;
        }
        return null;
    }

}