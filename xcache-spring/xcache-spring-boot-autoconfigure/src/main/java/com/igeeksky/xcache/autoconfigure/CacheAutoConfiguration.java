package com.igeeksky.xcache.autoconfigure;


import com.igeeksky.xcache.autoconfigure.register.*;
import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xcache.core.CacheManagerConfig;
import com.igeeksky.xcache.core.CacheManagerImpl;
import com.igeeksky.xcache.core.ComponentManager;
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
@AutoConfigureAfter({CacheProperties.class})
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
    CacheManager cacheManager(ObjectProvider<StoreProviderRegister> storeHolders,
                              ObjectProvider<CodecProviderRegister> codecHolders,
                              ObjectProvider<CacheSyncProviderRegister> syncHolders,
                              ObjectProvider<CacheStatProviderRegister> statHolders,
                              ObjectProvider<CacheLockProviderRegister> lockHolders,
                              ObjectProvider<CacheLoaderRegister> loaderHolders,
                              ObjectProvider<CacheRefreshProviderRegister> refreshHolders,
                              ObjectProvider<CompressorProviderRegister> compressorHolders,
                              ObjectProvider<ContainsPredicateRegister> predicateHolders,
                              ScheduledExecutorService scheduler) {

        ComponentManager componentManager = new ComponentManager(scheduler, cacheProperties.getStat());

        for (CacheLoaderRegister holder : loaderHolders) {
            holder.getAll().forEach(componentManager::addCacheLoader);
        }

        for (ContainsPredicateRegister holder : predicateHolders) {
            holder.getAll().forEach(componentManager::addContainsPredicate);
        }

        for (StoreProviderRegister holder : storeHolders) {
            holder.getAll().forEach(componentManager::addStoreProvider);
        }

        for (CodecProviderRegister holder : codecHolders) {
            holder.getAll().forEach(componentManager::addCodecProvider);
        }

        for (CacheSyncProviderRegister holder : syncHolders) {
            holder.getAll().forEach(componentManager::addSyncProvider);
        }

        for (CacheStatProviderRegister holder : statHolders) {
            holder.getAll().forEach(componentManager::addStatProvider);
        }

        for (CacheLockProviderRegister holder : lockHolders) {
            holder.getAll().forEach(componentManager::addLockProvider);
        }

        for (CacheRefreshProviderRegister holder : refreshHolders) {
            holder.getAll().forEach(componentManager::addRefreshProvider);
        }

        for (CompressorProviderRegister holder : compressorHolders) {
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

}