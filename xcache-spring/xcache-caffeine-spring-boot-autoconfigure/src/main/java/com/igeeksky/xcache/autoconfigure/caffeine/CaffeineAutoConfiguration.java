package com.igeeksky.xcache.autoconfigure.caffeine;

import com.igeeksky.xcache.autoconfigure.CacheAutoConfiguration;
import com.igeeksky.xcache.autoconfigure.register.StoreProviderRegister;
import com.igeeksky.xcache.caffeine.CaffeineExpiryProvider;
import com.igeeksky.xcache.caffeine.CaffeineStoreProvider;
import com.igeeksky.xcache.caffeine.CaffeineWeigherProvider;
import com.igeeksky.xcache.core.SingletonSupplier;
import com.igeeksky.xcache.props.CacheConstants;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import java.util.List;

/**
 * Caffeine 自动配置
 *
 * @author patrick
 * @since 0.0.4 2023/09/18
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@AutoConfigureBefore(CacheAutoConfiguration.class)
@SuppressWarnings("unused")
public class CaffeineAutoConfiguration {

    /**
     * 注册 CaffeineStoreProvider（ID: caffeine）
     *
     * @param expiryProviders  自定义 ExpiryProvider
     * @param weigherProviders 自定义 WeigherProvider
     * @return {@link StoreProviderRegister} – StoreProvider 注册器
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    StoreProviderRegister caffeineStoreRegister(ObjectProvider<CaffeineExpiryProvider> expiryProviders,
                                                ObjectProvider<CaffeineWeigherProvider> weigherProviders) {
        List<CaffeineExpiryProvider> expiryList = expiryProviders.orderedStream().toList();
        List<CaffeineWeigherProvider> weigherList = weigherProviders.orderedStream().toList();
        StoreProviderRegister register = new StoreProviderRegister();
        register.put(CacheConstants.CAFFEINE_STORE, SingletonSupplier.of(() ->
                new CaffeineStoreProvider(expiryList, weigherList))
        );
        return register;
    }

}
