package com.igeeksky.xcache.aop;

import com.igeeksky.xcache.core.CacheManager;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;


/**
 * 缓存动态代理自动配置类
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-13
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration(proxyBeanMethods = false)
class ProxyCacheConfiguration implements ImportAware {

    private AnnotationAttributes enableCache;

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean(CacheOperationAdvisor.CACHE_ADVISOR_BEAN_NAME)
    CacheOperationAdvisor cacheOperationAdvisor(CacheOperationSource source, CacheInterceptor advice) {
        // CacheAnnotationParser
        CacheOperationAdvisor advisor = new CacheOperationAdvisor(source, advice);
        advisor.setAdviceBeanName(CacheInterceptor.CACHE_INTERCEPTOR_BEAN_NAME);
        if (this.enableCache != null) {
            advisor.setOrder(enableCache.<Integer>getNumber("order"));
            String[] basePackages = enableCache.getStringArray("basePackages");
            advisor.setBasePackages(basePackages);
        }
        return advisor;
    }

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean(CacheOperationSource.CACHE_OPERATION_SOURCE_BEAN_NAME)
    CacheOperationSource cacheOperationSource() {
        return new CacheOperationSource();
    }

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean(CacheInterceptor.CACHE_INTERCEPTOR_BEAN_NAME)
    CacheInterceptor cacheInterceptor(CacheManager cacheManager) {
        return new CacheInterceptor(cacheManager);
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableCache = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableCache.class.getName()));
        if (this.enableCache == null) {
            throw new IllegalArgumentException(
                    "@EnableCache is not present on importing class " + importMetadata.getClassName());
        }
    }

}