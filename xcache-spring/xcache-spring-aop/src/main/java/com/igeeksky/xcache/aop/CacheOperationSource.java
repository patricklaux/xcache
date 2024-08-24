package com.igeeksky.xcache.aop;


import com.igeeksky.xcache.annotation.operation.CacheOperation;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.AnnotatedElementKey;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取方法上的缓存注解及对应操作
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-13
 */
public class CacheOperationSource {

    public static final String CACHE_OPERATION_SOURCE_BEAN_NAME = "com.igeeksky.xcache.aop.cacheOperationSource";

    private static final Logger logger = LoggerFactory.getLogger(CacheOperationSource.class);

    private static final Collection<CacheOperation> NULL_CACHING_ATTRIBUTE = Collections.emptyList();

    private final Map<AnnotatedElementKey, Collection<CacheOperation>> attributeCache = new ConcurrentHashMap<>(1024);

    /**
     * 获取方法上的缓存注解及对应操作
     *
     * @param method      方法
     * @param targetClass 类
     * @return 操作
     */
    public Collection<CacheOperation> getCacheOperations(Method method, Class<?> targetClass) {
        if (method.getDeclaringClass() == Object.class) {
            return null;
        }

        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);

        Collection<CacheOperation> cached = this.attributeCache.computeIfAbsent(methodKey, key -> {
            Collection<CacheOperation> cacheOps = computeCacheOperations(method, targetClass);
            if (cacheOps != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Adding cacheable method '{}' with attribute: {}", method.getName(), cacheOps);
                }
                return cacheOps;
            } else {
                return NULL_CACHING_ATTRIBUTE;
            }
        });

        return (cached != NULL_CACHING_ATTRIBUTE) ? cached : null;
    }

    private Collection<CacheOperation> computeCacheOperations(Method method, Class<?> targetClass) {
        // 如果是非公开方法，不处理缓存注解
        if (!Modifier.isPublic(method.getModifiers())) {
            return null;
        }

        // 特定的目标方法
        // 如果 targetClass 为空 或 没有实现类，则返回原方法。此时，specificMethod == method
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

        /*
         * 缓存注解有可能是在接口方法中声明，而不是在目标实现方法，需要先判断实现方法中是否也有注解
         * 如果实现方法和接口方法都有注解，以实现方法中的注解为准。
         */
        // 1.先从 specificMethod 获取注解
        Collection<CacheOperation> operations = cacheOperations(specificMethod, targetClass);
        if (CollectionUtils.isNotEmpty(operations)) {
            return operations;
        }

        // 2.如果注解不存在，判断 specificMethod 与 method 是否相同：
        // 2.1 如果相同，返回空；
        if (specificMethod == method) {
            return null;
        }

        // 2.1 如果不相同，从 method 获取注解。
        return cacheOperations(method, targetClass);
    }

    private Collection<CacheOperation> cacheOperations(Method method, Class<?> targetClass) {
        return CacheAnnotationParser.parseCacheAnnotations(method, targetClass);
    }

}