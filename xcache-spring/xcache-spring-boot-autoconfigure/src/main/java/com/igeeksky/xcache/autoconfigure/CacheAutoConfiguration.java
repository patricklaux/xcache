package com.igeeksky.xcache.autoconfigure;


import com.igeeksky.xcache.autoconfigure.holder.*;
import com.igeeksky.xcache.common.CacheConfigException;
import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xcache.core.CacheManagerImpl;
import com.igeeksky.xcache.core.ComponentRegister;
import com.igeeksky.xcache.props.CacheProps;
import com.igeeksky.xcache.props.Template;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
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
                              ObjectProvider<CacheRefreshProviderHolder> refreshHolders,
                              ObjectProvider<CompressorProviderHolder> compressorHolders,
                              ObjectProvider<ContainsPredicateProviderHolder> predicateHolders,
                              ScheduledExecutorService scheduler) {

        String app = cacheProperties.getApp();
        Map<String, Template> templates = toTemplateMap(cacheProperties.getTemplates());
        Map<String, CacheProps> configs = toCachePropsMap(cacheProperties.getCaches());

        // 管理内嵌组件：LogCacheStatProviderHolder 和 CacheRefreshProviderHolder 延迟注册，避免 scheduler 运行无效任务
        ComponentRegister register = new ComponentRegister(scheduler, statProperties.getPeriod());
        CacheManagerImpl cacheManager = new CacheManagerImpl(app, register, templates, configs);

        for (StoreProviderHolder holder : storeHolders) {
            holder.getAll().forEach(cacheManager::addProvider);
        }

        for (CodecProviderHolder holder : codecHolders) {
            holder.getAll().forEach(cacheManager::addProvider);
        }

        for (CacheSyncProviderHolder holder : syncHolders) {
            holder.getAll().forEach(cacheManager::addProvider);
        }

        for (CacheStatProviderHolder holder : statHolders) {
            holder.getAll().forEach(cacheManager::addProvider);
        }

        for (CacheLockProviderHolder holder : lockHolders) {
            holder.getAll().forEach(cacheManager::addProvider);
        }

        for (CacheLoaderHolder holder : loaderHolders) {
            holder.getAll().forEach(cacheManager::addCacheLoader);
        }

        for (CacheRefreshProviderHolder holder : refreshHolders) {
            holder.getAll().forEach(cacheManager::addProvider);
        }

        for (CompressorProviderHolder holder : compressorHolders) {
            holder.getAll().forEach(cacheManager::addProvider);
        }

        for (ContainsPredicateProviderHolder holder : predicateHolders) {
            holder.getAll().forEach(cacheManager::addProvider);
        }

        return cacheManager;
    }

    private static Map<String, Template> toTemplateMap(List<Template> templates) {
        if (CollectionUtils.isEmpty(templates)) {
            throw new CacheConfigException("Cache-config: No templates found.");
        }

        Map<String, Template> map = Maps.newHashMap(templates.size());
        for (Template template : templates) {
            String id = StringUtils.trimToNull(template.getId());
            requireNonNull(id, "Cache-config: id must not be null or empty.");

            if (map.put(id, template) != null) {
                throw new CacheConfigException("Cache-config: duplicate template: " + id);
            }
        }
        return map;
    }

    private static Map<String, CacheProps> toCachePropsMap(List<CacheProps> caches) {
        if (CollectionUtils.isEmpty(caches)) {
            return Maps.newHashMap(0);
        }

        Map<String, CacheProps> map = Maps.newHashMap(caches.size());
        for (CacheProps props : caches) {
            String name = StringUtils.trimToNull(props.getName());
            requireNonNull(name, "Cache-config: name must not be null or empty");

            if (map.put(name, props) != null) {
                throw new CacheConfigException("Cache-config: duplicate cache: " + name);
            }
        }
        return map;
    }

    private static void requireNonNull(Object obj, String errMsg) {
        if (obj == null) {
            throw new CacheConfigException(errMsg);
        }
    }

}