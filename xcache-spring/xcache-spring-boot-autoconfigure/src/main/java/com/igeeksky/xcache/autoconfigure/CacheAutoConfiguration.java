package com.igeeksky.xcache.autoconfigure;


import com.igeeksky.xcache.autoconfigure.holder.*;
import com.igeeksky.xcache.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
@AutoConfigureAfter({CacheProperties.class, CacheStatProperties.class})
public class CacheAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CacheAutoConfiguration.class);

    private final CacheProperties cacheProperties;
    private final CacheStatProperties statProperties;

    CacheAutoConfiguration(CacheProperties cacheProperties, CacheStatProperties statProperties) {
        this.cacheProperties = cacheProperties;
        this.statProperties = statProperties;
        if (log.isDebugEnabled()) {
            log.debug("CacheProperties: {}", cacheProperties);
        }
    }

    @Bean("xcacheManager")
    @ConditionalOnMissingBean(CacheManager.class)
    CacheManager cacheManager(ObjectProvider<StoreProviderHolder> storeHolders,
                              ObjectProvider<CodecProviderHolder> codecHolders,
                              ObjectProvider<CacheSyncProviderHolder> syncHolders,
                              ObjectProvider<CacheStatProviderHolder> statHolders,
                              ObjectProvider<CacheLockProviderHolder> lockHolders,
                              ObjectProvider<CacheLoaderHolder> loaderHolders,
                              ObjectProvider<CacheWriterHolder> writerHolders,
                              ObjectProvider<CacheRefreshProviderHolder> refreshHolders,
                              ObjectProvider<CompressorProviderHolder> compressorHolders,
                              ObjectProvider<ContainsPredicateProviderHolder> predicateHolders,
                              ScheduledExecutorService scheduler) {

        ComponentManager componentManager = new ComponentManagerImpl(scheduler, statProperties.getPeriod());

        for (StoreProviderHolder holder : storeHolders) {
            holder.getAll().forEach(componentManager::addProvider);
        }

        for (CodecProviderHolder holder : codecHolders) {
            holder.getAll().forEach(componentManager::addProvider);
        }

        for (CacheSyncProviderHolder holder : syncHolders) {
            holder.getAll().forEach(componentManager::addProvider);
        }

        for (CacheStatProviderHolder holder : statHolders) {
            holder.getAll().forEach(componentManager::addProvider);
        }

        for (CacheLockProviderHolder holder : lockHolders) {
            holder.getAll().forEach(componentManager::addProvider);
        }

        for (CacheLoaderHolder holder : loaderHolders) {
            holder.getAll().forEach(componentManager::addCacheLoader);
        }

        for (CacheWriterHolder holder : writerHolders) {
            holder.getAll().forEach(componentManager::addCacheWriter);
        }

        for (CacheRefreshProviderHolder holder : refreshHolders) {
            holder.getAll().forEach(componentManager::addProvider);
        }

        for (CompressorProviderHolder holder : compressorHolders) {
            holder.getAll().forEach(componentManager::addProvider);
        }

        for (ContainsPredicateProviderHolder holder : predicateHolders) {
            holder.getAll().forEach(componentManager::addProvider);
        }

        CacheManagerConfig managerConfig = CacheManagerConfig.builder()
                .app(cacheProperties.getApp())
                .componentManager(componentManager)
                .templates(cacheProperties.getTemplates())
                .caches(cacheProperties.getCaches())
                .build();

        return new CacheManagerImpl(managerConfig);
    }

}