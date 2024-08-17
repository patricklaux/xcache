package com.igeeksky.xcache.autoconfigure.caffeine;

import com.igeeksky.xcache.autoconfigure.CacheAutoConfiguration;
import com.igeeksky.xcache.autoconfigure.holder.StoreProviderHolder;
import com.igeeksky.xcache.caffeine.CaffeineExpiryProvider;
import com.igeeksky.xcache.caffeine.CaffeineStoreProvider;
import com.igeeksky.xcache.caffeine.CaffeineWeigherProvider;
import com.igeeksky.xcache.props.CacheConstants;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author patrick
 * @since 0.0.4 2023/09/18
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(CacheAutoConfiguration.class)
public class CaffeineConfiguration {

    public static final String CAFFEINE_BEAN_ID = CacheConstants.CAFFEINE_STORE;

    @Bean
    StoreProviderHolder caffeineStoreProviderHolder(ObjectProvider<CaffeineExpiryProvider> expiryProviders,
                                                    ObjectProvider<CaffeineWeigherProvider> weigherProviders) {

        List<CaffeineExpiryProvider> expiryProviderList = new ArrayList<>();
        expiryProviders.orderedStream().forEach(expiryProviderList::add);

        List<CaffeineWeigherProvider> weigherProviderList = new ArrayList<>();
        weigherProviders.orderedStream().forEach(weigherProviderList::add);

        StoreProviderHolder holder = new StoreProviderHolder();
        holder.put(CAFFEINE_BEAN_ID, new CaffeineStoreProvider(expiryProviderList, weigherProviderList));
        return holder;
    }

}
