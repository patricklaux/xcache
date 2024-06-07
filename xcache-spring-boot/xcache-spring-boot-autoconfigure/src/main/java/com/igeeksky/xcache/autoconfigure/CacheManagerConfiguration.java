package com.igeeksky.xcache.autoconfigure;


import com.igeeksky.xcache.autoconfigure.holder.*;
import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xcache.core.CacheManagerImpl;
import com.igeeksky.xcache.core.config.CacheConfigException;
import com.igeeksky.xcache.props.CacheProps;
import com.igeeksky.xcache.props.TemplateProps;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CacheManager 自动配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-29
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(ProxyCacheConfiguration.class)
public class CacheManagerConfiguration {

    private final CacheProperties cacheProperties;

    CacheManagerConfiguration(CacheProperties cacheProperties) {
        System.out.println(cacheProperties);
        this.cacheProperties = cacheProperties;
    }

    @Bean("cacheManager")
    @ConditionalOnMissingBean(CacheManager.class)
    CacheManager cacheManager(ObjectProvider<LocalStoreProviderHolder> localStoreHolders,
                              ObjectProvider<RemoteStoreProviderHolder> remoteStoreHolders,
                              ObjectProvider<KeyConvertorProviderHolder> keyConvertorHolders,
                              ObjectProvider<SerializerProviderHolder> serializerHolders,
                              ObjectProvider<CacheSyncProviderHolder> syncHolders,
                              ObjectProvider<CacheStatProviderHolder> statHolders,
                              ObjectProvider<CacheLockProviderHolder> lockHolders,
                              ObjectProvider<ContainsPredicateProviderHolder> predicateHolders,
                              ObjectProvider<CompressorProviderHolder> compressorHolders) {

        String application = cacheProperties.getApplication();

        List<TemplateProps> templates = cacheProperties.getTemplates();
        Map<String, TemplateProps> templatesMap = Maps.newHashMap(templates.size());
        for (TemplateProps template : templates) {
            String templateId = StringUtils.trimToNull(template.getTemplateId());
            requireNonNull(templateId, "Cache-config: templateId must not be null or empty");
            templatesMap.put(templateId, template);
        }

        Map<String, CacheProps> cachePropsMap = new HashMap<>();
        List<CacheProps> caches = cacheProperties.getCaches();
        for (CacheProps props : caches) {
            String name = StringUtils.trimToNull(props.getName());
            requireNonNull(name, "Cache-config: name must not be null or empty");
            cachePropsMap.put(name, props);
        }

        CacheManagerImpl cacheManager = new CacheManagerImpl(application, templatesMap, cachePropsMap);

        for (LocalStoreProviderHolder holder : localStoreHolders) {
            holder.getAll().forEach(cacheManager::addProvider);
        }

        for (RemoteStoreProviderHolder holder : remoteStoreHolders) {
            holder.getAll().forEach(cacheManager::addProvider);
        }

        for (KeyConvertorProviderHolder holder : keyConvertorHolders) {
            holder.getAll().forEach(cacheManager::addProvider);
        }

        for (SerializerProviderHolder holder : serializerHolders) {
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

        for (ContainsPredicateProviderHolder holder : predicateHolders) {
            holder.getAll().forEach(cacheManager::addProvider);
        }

        for (CompressorProviderHolder holder : compressorHolders) {
            holder.getAll().forEach(cacheManager::addProvider);
        }

        return cacheManager;
    }

    private static void requireNonNull(Object obj, String errMsg) {
        if (obj == null) {
            throw new CacheConfigException(errMsg);
        }
    }

}