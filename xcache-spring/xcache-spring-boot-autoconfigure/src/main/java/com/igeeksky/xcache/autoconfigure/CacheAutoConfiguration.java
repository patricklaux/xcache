package com.igeeksky.xcache.autoconfigure;


import com.igeeksky.xcache.autoconfigure.register.*;
import com.igeeksky.xcache.core.*;
import com.igeeksky.xcache.extension.refresh.EmbedCacheRefreshProvider;
import com.igeeksky.xcache.extension.stat.LogCacheStatProvider;
import com.igeeksky.xcache.props.CacheConstants;
import com.igeeksky.xtool.core.lang.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledExecutorService;

/**
 * CacheManager 自动配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-29
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter({CacheProperties.class})
@EnableConfigurationProperties({CacheProperties.class})
public class CacheAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CacheAutoConfiguration.class);

    private final CacheProperties cacheProperties;

    CacheAutoConfiguration(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
        if (log.isDebugEnabled()) {
            log.debug("CacheProperties: {}", cacheProperties);
        }
    }

    @Bean("xcacheManager")
    @ConditionalOnMissingBean(CacheManager.class)
    CacheManager cacheManager(ObjectProvider<CacheLoaderRegister> loaderRegisters,
                              ObjectProvider<StoreProviderRegister> storeRegisters,
                              ObjectProvider<CodecProviderRegister> codecRegisters,
                              ObjectProvider<CacheSyncProviderRegister> syncRegisters,
                              ObjectProvider<CacheStatProviderRegister> statRegisters,
                              ObjectProvider<CacheLockProviderRegister> lockRegisters,
                              ObjectProvider<ContainsPredicateRegister> predicateRegisters,
                              ObjectProvider<CacheRefreshProviderRegister> refreshRegisters,
                              ObjectProvider<CompressorProviderRegister> compressorRegisters) {

        ComponentManager componentManager = new ComponentManager();

        for (CacheLoaderRegister holder : loaderRegisters) {
            holder.getAll().forEach(componentManager::addCacheLoader);
        }

        for (ContainsPredicateRegister holder : predicateRegisters) {
            holder.getAll().forEach(componentManager::addContainsPredicate);
        }

        for (StoreProviderRegister holder : storeRegisters) {
            holder.getAll().forEach(componentManager::addStoreProvider);
        }

        for (CodecProviderRegister holder : codecRegisters) {
            holder.getAll().forEach(componentManager::addCodecProvider);
        }

        for (CacheSyncProviderRegister holder : syncRegisters) {
            holder.getAll().forEach(componentManager::addSyncProvider);
        }

        for (CacheStatProviderRegister holder : statRegisters) {
            holder.getAll().forEach(componentManager::addStatProvider);
        }

        for (CacheLockProviderRegister holder : lockRegisters) {
            holder.getAll().forEach(componentManager::addLockProvider);
        }

        for (CacheRefreshProviderRegister holder : refreshRegisters) {
            holder.getAll().forEach(componentManager::addRefreshProvider);
        }

        for (CompressorProviderRegister holder : compressorRegisters) {
            holder.getAll().forEach(componentManager::addCompressorProvider);
        }

        CacheManagerConfig managerConfig = CacheManagerConfig.builder()
                .group(cacheProperties.getGroup())
                .componentManager(componentManager)
                .templates(cacheProperties.getTemplate())
                .caches(cacheProperties.getCache())
                .build();

        return new CacheManagerImpl(managerConfig);
    }

    @Bean(destroyMethod = "shutdown")
    CacheStatProviderRegister logCacheStatProviderRegister(ScheduledExecutorService scheduler) {
        long interval = (cacheProperties.getLogStatInterval() != null) ?
                cacheProperties.getLogStatInterval() :
                CacheConstants.DEFAULT_STAT_INTERVAL;
        Assert.isTrue(interval > 0, "log-stat-interval must be greater than 0");

        CacheStatProviderRegister register = new CacheStatProviderRegister();
        register.put(CacheConstants.LOG_CACHE_STAT,
                SingletonSupplier.of(() -> new LogCacheStatProvider(scheduler, interval)));
        return register;
    }

    @Bean(destroyMethod = "shutdown")
    CacheRefreshProviderRegister embedCacheRefreshProviderRegister(ScheduledExecutorService scheduler) {
        CacheRefreshProviderRegister register = new CacheRefreshProviderRegister();
        register.put(CacheConstants.EMBED_CACHE_REFRESH,
                SingletonSupplier.of(() -> new EmbedCacheRefreshProvider(scheduler)));
        return register;
    }

}