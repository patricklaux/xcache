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

        if (method.isAnnotationPresent(CacheRemove.class)) {
            if (hasCacheable) {
                throw new IllegalStateException("@CacheEvict and @Cacheable are mutually exclusive");
            }
            if (hasCacheableAll) {
                throw new IllegalStateException("@CacheEvict and @CacheableAll are mutually exclusive");
            }
            CacheRemove cacheRemove = method.getAnnotation(CacheRemove.class);
            results.add(processCacheEvict(cacheRemove, operation));
        }

        if (method.isAnnotationPresent(CacheRemoveAll.class)) {
            if (hasCacheable) {
                throw new IllegalStateException("@CacheEvictAll and @Cacheable are mutually exclusive");
            }
            if (hasCacheableAll) {
                throw new IllegalStateException("@CacheEvictAll and @CacheableAll are mutually exclusive");
            }
            CacheRemoveAll cacheRemoveAll = method.getAnnotation(CacheRemoveAll.class);
            results.add(processCacheEvictAll(cacheRemoveAll, operation));
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
                    .build();
        }
        return null;
    }

    private static CacheableOperation processCacheable(Cacheable cacheable, CacheOperation operation) {
        CacheableOperation.Builder builder = CacheableOperation.builder();
        builder.name(cacheable.name())
                .keyType(cacheable.keyType())
                .valueType(cacheable.valueType())
                .cacheOperation(operation);
        return builder.condition(cacheable.condition())
                .key(cacheable.key())
                .build();
    }

    private static CacheableAllOperation processCacheableAll(CacheableAll cacheableAll, CacheOperation operation) {
        CacheableAllOperation.Builder builder = CacheableAllOperation.builder();
        builder.name(cacheableAll.name())
                .keyType(cacheableAll.keyType())
                .valueType(cacheableAll.valueType())
                .cacheOperation(operation);

        return builder.condition(cacheableAll.condition())
                .keys(cacheableAll.keys())
                .build();
    }

    private static CachePutOperation processCachePut(CachePut cachePut, CacheOperation operation) {
        CachePutOperation.Builder builder = CachePutOperation.builder();
        builder.name(cachePut.name())
                .keyType(cachePut.keyType())
                .valueType(cachePut.valueType())
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
                .valueType(cachePutAll.valueType())
                .cacheOperation(operation);

        return builder.condition(cachePutAll.condition())
                .unless(cachePutAll.unless())
                .keyValues(cachePutAll.keyValues())
                .build();
    }

    private static CacheRemoveOperation processCacheEvict(CacheRemove cacheRemove, CacheOperation operation) {
        CacheRemoveOperation.Builder builder = CacheRemoveOperation.builder();
        builder.name(cacheRemove.name())
                .keyType(cacheRemove.keyType())
                .valueType(cacheRemove.valueType())
                .cacheOperation(operation);

        return builder.key(cacheRemove.key())
                .condition(cacheRemove.condition())
                .unless(cacheRemove.unless())
                .beforeInvocation(cacheRemove.beforeInvocation())
                .build();
    }

    private static CacheRemoveAllOperation processCacheEvictAll(CacheRemoveAll cacheRemoveAll, CacheOperation operation) {
        CacheRemoveAllOperation.Builder builder = CacheRemoveAllOperation.builder();
        builder.name(cacheRemoveAll.name())
                .keyType(cacheRemoveAll.keyType())
                .valueType(cacheRemoveAll.valueType())
                .cacheOperation(operation);

        return builder.keys(cacheRemoveAll.keys())
                .condition(cacheRemoveAll.condition())
                .unless(cacheRemoveAll.unless())
                .beforeInvocation(cacheRemoveAll.beforeInvocation())
                .build();
    }

    private static CacheClearOperation processCacheClear(CacheClear cacheClear, CacheOperation operation) {
        CacheClearOperation.Builder builder = CacheClearOperation.builder();
        builder.name(cacheClear.name())
                .keyType(cacheClear.keyType())
                .valueType(cacheClear.valueType())
                .cacheOperation(operation);

        return builder.condition(cacheClear.condition())
                .unless(cacheClear.unless())
                .beforeInvocation(cacheClear.beforeInvocation())
                .build();
    }

}