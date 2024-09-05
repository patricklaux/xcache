package com.igeeksky.xcache.autoconfigure;

import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xcache.spring.SpringCacheManager;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 适配 Spring CacheManager
 * <p>
 * 如希望使用 Spring 缓存注解，则需要配置此 Bean
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/5
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(CacheAutoConfiguration.class)
public class SpringCacheAutoConfiguration {

    @Bean("springCacheManager")
    org.springframework.cache.CacheManager cacheManager(CacheManager cacheManager) {
        return new SpringCacheManager(cacheManager);
    }

}