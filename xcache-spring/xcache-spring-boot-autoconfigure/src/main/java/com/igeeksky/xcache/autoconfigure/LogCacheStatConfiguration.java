package com.igeeksky.xcache.autoconfigure;

import com.igeeksky.xcache.autoconfigure.holder.CacheStatProviderHolder;
import com.igeeksky.xcache.extension.statistic.LogCacheStatManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-09
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(CacheManagerConfiguration.class)
class LogCacheStatConfiguration {

    @Value("${xcache.stat.log.period}")
    private Long period = 10000L;

    public static final String LOG_CACHE_LOCK_PROVIDER_ID = "logCacheStatManager";

    @Bean
    CacheStatProviderHolder cacheStatProviderHolder() {
        CacheStatProviderHolder holder = new CacheStatProviderHolder();
        holder.put(LOG_CACHE_LOCK_PROVIDER_ID, new LogCacheStatManager(period));
        return holder;
    }

}
