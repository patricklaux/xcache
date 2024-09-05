package com.igeeksky.xcache.aop;


import com.igeeksky.xcache.annotation.*;
import com.igeeksky.xcache.annotation.operation.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 解析：缓存注解
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-13
 */
public class CacheAnnotationParser {

    public static Collection<CacheOperation> parseCacheAnnotations(Method method, Class<?> targetClass) {
        CacheOperation operation = processCacheConfig(targetClass);

        List<CacheOperation> results = new ArrayList<>();

        boolean hasCacheable = false;
        if (method.isAnnotationPresent(Cacheable.class)) {
            hasCacheable = true;
            Cacheable cacheable = method.getAnnotation(Cacheable.class);
            results.add(processCacheable(cacheable, operation));
        }

        boolean hasCacheableAll = false;
        if (method.isAnnotationPresent(CacheableAll.class)) {
            if (hasCacheable) {
                throw new IllegalStateException("@CacheableAll and @Cacheable are mutually exclusive");
            }
            hasCacheableAll = true;
            CacheableAll cacheableAll = method.getAnnotation(CacheableAll.class);
            results.add(processCacheableAll(cacheableAll, operation));
        }

        if (method.isAnnotationPresent(CachePut.class)) {
            if (hasCacheable) {
                throw new IllegalStateException("@CachePut and @Cacheable are mutually exclusive");
            }
            if (hasCacheableAll) {
                throw new IllegalStateException("@CachePut and @CacheableAll are mutually exclusive");
            }
            CachePut cachePut = method.getAnnotation(CachePut.class);
            results.add(processCachePut(cachePut, operation));
        }

        if (method.isAnnotationPresent(CachePutAll.class)) {
            if (hasCacheable) {
                throw new IllegalStateException("@CachePutAll and @Cacheable are mutually exclusive");
            }
            if (hasCacheableAll) {
                throw new IllegalStateException("@CachePutAll and @CacheableAll are mutually exclusive");
            }
            CachePutAll cachePutAll = method.getAnnotation(CachePutAll.class);
            results.add(processCachePutAll(cachePutAll, operation));
        }

        if (method.isAnnotationPresent(CacheEvict.class)) {
            if (hasCacheable) {
                throw new IllegalStateException("@CacheEvict and @Cacheable are mutually exclusive");
            }
            if (hasCacheableAll) {
                throw new IllegalStateException("@CacheEvict and @CacheableAll are mutually exclusive");
            }
            CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);
            results.add(processCacheEvict(cacheEvict, operation));
        }

        if (method.isAnnotationPresent(CacheEvictAll.class)) {
            if (hasCacheable) {
                throw new IllegalStateException("@CacheEvictAll and @Cacheable are mutually exclusive");
            }
            if (hasCacheableAll) {
                throw new IllegalStateException("@CacheEvictAll and @CacheableAll are mutually exclusive");
            }
            CacheEvictAll cacheEvictAll = method.getAnnotation(CacheEvictAll.class);
            results.add(processCacheEvictAll(cacheEvictAll, operation));
        }

        if (method.isAnnotationPresent(CacheClear.class)) {
            if (hasCacheable) {
                throw new IllegalStateException("@CacheClear and @Cacheable are mutually exclusive");
            }
            if (hasCacheableAll) {
                throw new IllegalStateException("@CacheClear and @CacheableAll are mutually exclusive");
            }
            CacheClear cacheClear = method.getAnnotation(CacheClear.class);
            results.add(processCacheClear(cacheClear, operation));
        }

        return results;
    }

    private static CacheOperation processCacheConfig(Class<?> targetClass) {
        if (targetClass.isAnnotationPresent(CacheConfig.class)) {
            CacheConfig cacheConfig = targetClass.getAnnotation(CacheConfig.class);
            return CacheOperation.builder()
                    .name(cacheConfig.name())
                    .keyType(cacheConfig.keyType())
                    .valueType(cacheConfig.valueType())
                    .keyParams(cacheConfig.keyParams())
                    .valueParams(cacheConfig.valueParams())
                    .build();
        }
        return null;
    }

    private static CacheableOperation processCacheable(Cacheable cacheable, CacheOperation operation) {
        CacheableOperation.Builder builder = CacheableOperation.builder();
        builder.name(cacheable.name())
                .keyType(cacheable.keyType())
                .keyParams(cacheable.keyParams())
                .keyParams(cacheable.keyParams())
                .valueParams(cacheable.valueParams())
                .cacheOperation(operation);
        return builder.condition(cacheable.condition())
                .unless(cacheable.unless())
                .key(cacheable.key())
                .build();
    }

    private static CacheableAllOperation processCacheableAll(CacheableAll cacheableAll, CacheOperation operation) {
        CacheableAllOperation.Builder builder = CacheableAllOperation.builder();
        builder.name(cacheableAll.name())
                .keyType(cacheableAll.keyType())
                .keyParams(cacheableAll.keyParams())
                .valueType(cacheableAll.valueType())
                .valueParams(cacheableAll.valueParams())
                .cacheOperation(operation);

        return builder.condition(cacheableAll.condition())
                .unless(cacheableAll.unless())
                .keys(cacheableAll.keys())
                .build();
    }

    private static CachePutOperation processCachePut(CachePut cachePut, CacheOperation operation) {
        CachePutOperation.Builder builder = CachePutOperation.builder();
        builder.name(cachePut.name())
                .keyType(cachePut.keyType())
                .keyParams(cachePut.keyParams())
                .valueType(cachePut.valueType())
                .valueParams(cachePut.valueParams())
                .cacheOperation(operation);

        return builder.value(cachePut.value())
                .condition(cachePut.condition())
                .unless(cachePut.unless())
                .key(cachePut.key())
                .build();
    }

    private static CachePutAllOperation processCachePutAll(CachePutAll cachePutAll, CacheOperation operation) {
        CachePutAllOperation.Builder builder = CachePutAllOperation.builder();
        builder.name(cachePutAll.name())
                .keyType(cachePutAll.keyType())
                .keyParams(cachePutAll.keyParams())
                .valueType(cachePutAll.valueType())
                .valueParams(cachePutAll.valueParams())
                .cacheOperation(operation);

        return builder.condition(cachePutAll.condition())
                .unless(cachePutAll.unless())
                .keyValues(cachePutAll.keyValues())
                .build();
    }

    private static CacheEvictOperation processCacheEvict(CacheEvict cacheEvict, CacheOperation operation) {
        CacheEvictOperation.Builder builder = CacheEvictOperation.builder();
        builder.name(cacheEvict.name())
                .keyType(cacheEvict.keyType())
                .keyParams(cacheEvict.keyParams())
                .valueType(cacheEvict.valueType())
                .valueParams(cacheEvict.valueParams())
                .cacheOperation(operation);

        return builder.key(cacheEvict.key())
                .condition(cacheEvict.condition())
                .unless(cacheEvict.unless())
                .beforeInvocation(cacheEvict.beforeInvocation())
                .build();
    }

    private static CacheEvictAllOperation processCacheEvictAll(CacheEvictAll cacheEvictAll, CacheOperation operation) {
        CacheEvictAllOperation.Builder builder = CacheEvictAllOperation.builder();
        builder.name(cacheEvictAll.name())
                .keyType(cacheEvictAll.keyType())
                .keyParams(cacheEvictAll.keyParams())
                .valueType(cacheEvictAll.valueType())
                .valueParams(cacheEvictAll.valueParams())
                .cacheOperation(operation);

        return builder.keys(cacheEvictAll.keys())
                .condition(cacheEvictAll.condition())
                .unless(cacheEvictAll.unless())
                .beforeInvocation(cacheEvictAll.beforeInvocation())
                .build();
    }

    private static CacheClearOperation processCacheClear(CacheClear cacheClear, CacheOperation operation) {
        CacheClearOperation.Builder builder = CacheClearOperation.builder();
        builder.name(cacheClear.name())
                .keyType(cacheClear.keyType())
                .keyParams(cacheClear.keyParams())
                .valueType(cacheClear.valueType())
                .valueParams(cacheClear.valueParams())
                .cacheOperation(operation);

        return builder.condition(cacheClear.condition())
                .unless(cacheClear.unless())
                .beforeInvocation(cacheClear.beforeInvocation())
                .build();
    }

}