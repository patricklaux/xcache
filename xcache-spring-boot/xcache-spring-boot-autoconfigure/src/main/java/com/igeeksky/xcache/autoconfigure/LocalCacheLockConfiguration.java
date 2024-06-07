package com.igeeksky.xcache.autoconfigure;

import com.igeeksky.xcache.autoconfigure.holder.CacheLockProviderHolder;
import com.igeeksky.xcache.extension.lock.LocalCacheLockProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-09
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(CacheManagerConfiguration.class)
class LocalCacheLockConfiguration {

    public static final String LOCAL_CACHE_LOCK_PROVIDER_ID = "localCacheLockProvider";

    @Bean
    CacheLockProviderHolder cacheLockProviderHolder() {
        CacheLockProviderHolder holder = new CacheLockProviderHolder();
        holder.put(LOCAL_CACHE_LOCK_PROVIDER_ID, new LocalCacheLockProvider());
        return holder;
    }

}
