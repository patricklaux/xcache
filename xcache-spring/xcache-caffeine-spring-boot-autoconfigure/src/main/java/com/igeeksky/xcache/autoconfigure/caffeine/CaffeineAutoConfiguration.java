package com.igeeksky.xcache.autoconfigure.caffeine;

import com.igeeksky.xcache.autoconfigure.CacheManagerConfiguration;
import com.igeeksky.xcache.autoconfigure.holder.LocalStoreProviderHolder;
import com.igeeksky.xcache.caffeine.CaffeineExpiryProvider;
import com.igeeksky.xcache.caffeine.CaffeineStoreProvider;
import com.igeeksky.xcache.caffeine.CaffeineWeigherProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author patrick
 * @since 0.0.4 2023/09/18
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(CacheManagerConfiguration.class)
public class CaffeineAutoConfiguration {

    public static final String CAFFEINE_BEAN_ID = "caffeineCacheStoreProvider";

    @Bean
    LocalStoreProviderHolder caffeineStoreProviderHolder(ObjectProvider<CaffeineExpiryProvider> expiryProviders,
                                                         ObjectProvider<CaffeineWeigherProvider> weigherProviders) {

        CaffeineExpiryProvider expiryProvider = expiryProviders.getIfAvailable();
        CaffeineWeigherProvider weigherProvider = weigherProviders.getIfAvailable();

        LocalStoreProviderHolder holder = new LocalStoreProviderHolder();
        holder.put(CAFFEINE_BEAN_ID, new CaffeineStoreProvider(expiryProvider, weigherProvider));
        return holder;
    }

}
