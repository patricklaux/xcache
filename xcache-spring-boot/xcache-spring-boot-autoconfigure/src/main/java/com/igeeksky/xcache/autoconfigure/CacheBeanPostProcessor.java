package com.igeeksky.xcache.autoconfigure;


import com.igeeksky.xcache.annotation.CacheAutowired;
import com.igeeksky.xcache.core.Cache;
import com.igeeksky.xcache.core.CacheManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.ReflectionUtils;

/**
 * 检测 CacheAutowired 注解，注入 CacheBean
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-09
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(CacheManagerConfiguration.class)
public class CacheBeanPostProcessor implements BeanPostProcessor {

    private final CacheManager cacheManager;

    @Lazy
    public CacheBeanPostProcessor(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            CacheAutowired annotation = field.getAnnotation(CacheAutowired.class);
            if (annotation != null) {
                Class<?> fieldType = field.getType();
                if (!fieldType.isAssignableFrom(Cache.class)) {
                    throw new IllegalArgumentException(field.getDeclaringClass() + ": Can not set field [" + field.getName() + "] to cache");
                }

                String name = annotation.name();
                Class<?> keyType = annotation.keyType();
                Class<?> valueType = annotation.valueType();
                Class<?>[] valueParams = annotation.valueParams();

                Cache<?, ?> cache = cacheManager.getOrCreateCache(name, keyType, valueType, valueParams);
                field.setAccessible(true);
                ReflectionUtils.setField(field, bean, cache);
            }
        });
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

}
