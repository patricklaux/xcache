package com.igeeksky.xcache.aop;


import com.igeeksky.xcache.annotation.*;
import com.igeeksky.xcache.annotation.operation.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-13
 */
public class CacheAnnotationParser {

    public static Collection<CacheOperation> parseCacheAnnotations(Method method) {
        List<CacheOperation> results = new ArrayList<>();
        Cacheable cacheable = method.getAnnotation(Cacheable.class);
        if (cacheable != null) {
            results.add(processCacheable(cacheable));
        }

        CacheableAll cacheableAll = method.getAnnotation(CacheableAll.class);
        if (cacheableAll != null) {
            results.add(processCacheableAll(cacheableAll));
        }

        CachePut cachePut = method.getAnnotation(CachePut.class);
        if (cachePut != null) {
            results.add(processCachePut(cachePut));
        }

        CachePutAll cachePutAll = method.getAnnotation(CachePutAll.class);
        if (cachePutAll != null) {
            results.add(processCachePutAll(cachePutAll));
        }

        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);
        if (cacheEvict != null) {
            results.add(processCacheEvict(cacheEvict));
        }

        CacheEvictAll cacheEvictAll = method.getAnnotation(CacheEvictAll.class);
        if (cacheEvictAll != null) {
            results.add(processCacheEvictAll(cacheEvictAll));
        }

        CacheClear cacheClear = method.getAnnotation(CacheClear.class);
        if (cacheClear != null) {
            results.add(processCacheClear(cacheClear));
        }

        return results;
    }

    private static CacheableOperation processCacheable(Cacheable cacheable) {
        CacheableOperation.Builder builder = CacheableOperation.builder();
        builder.name(cacheable.name());
        builder.keyType(cacheable.keyType());
        builder.valueType(cacheable.valueType());
        builder.valueParams(cacheable.valueParams());

        builder.condition(cacheable.condition());
        builder.unless(cacheable.unless());
        return builder.key(cacheable.key()).build();
    }

    private static CacheableAllOperation processCacheableAll(CacheableAll cacheableAll) {
        CacheableAllOperation.Builder builder = CacheableAllOperation.builder();
        builder.name(cacheableAll.name());
        builder.keyType(cacheableAll.keyType());
        builder.valueType(cacheableAll.valueType());
        builder.valueParams(cacheableAll.valueParams());

        builder.condition(cacheableAll.condition());
        builder.unless(cacheableAll.unless());
        return builder.keys(cacheableAll.keys()).build();
    }

    private static CachePutOperation processCachePut(CachePut cachePut) {
        CachePutOperation.Builder builder = CachePutOperation.builder();
        builder.name(cachePut.name());
        builder.keyType(cachePut.keyType());
        builder.valueType(cachePut.valueType());
        builder.valueParams(cachePut.valueParams());

        builder.value(cachePut.value());
        builder.condition(cachePut.condition());
        builder.unless(cachePut.unless());
        return builder.key(cachePut.key()).build();
    }

    private static CachePutAllOperation processCachePutAll(CachePutAll cachePutAll) {
        CachePutAllOperation.Builder builder = CachePutAllOperation.builder();
        builder.name(cachePutAll.name());
        builder.keyType(cachePutAll.keyType());
        builder.valueType(cachePutAll.valueType());
        builder.valueParams(cachePutAll.valueParams());

        builder.condition(cachePutAll.condition());
        builder.unless(cachePutAll.unless());
        return builder.keyValues(cachePutAll.keyValues()).build();
    }

    private static CacheEvictOperation processCacheEvict(CacheEvict cacheEvict) {
        CacheEvictOperation.Builder builder = CacheEvictOperation.builder();
        builder.name(cacheEvict.name());
        builder.keyType(cacheEvict.keyType());
        builder.valueType(cacheEvict.valueType());
        builder.valueParams(cacheEvict.valueParams());

        builder.key(cacheEvict.key());
        builder.condition(cacheEvict.condition());
        builder.unless(cacheEvict.unless());
        return builder.beforeInvocation(cacheEvict.beforeInvocation()).build();
    }

    private static CacheEvictAllOperation processCacheEvictAll(CacheEvictAll cacheEvictAll) {
        CacheEvictAllOperation.Builder builder = CacheEvictAllOperation.builder();
        builder.name(cacheEvictAll.name());
        builder.keyType(cacheEvictAll.keyType());
        builder.valueType(cacheEvictAll.valueType());
        builder.valueParams(cacheEvictAll.valueParams());

        builder.keys(cacheEvictAll.keys());
        builder.condition(cacheEvictAll.condition());
        builder.unless(cacheEvictAll.unless());
        return builder.beforeInvocation(cacheEvictAll.beforeInvocation()).build();
    }

    private static CacheClearOperation processCacheClear(CacheClear cacheClear) {
        CacheClearOperation.Builder builder = CacheClearOperation.builder();
        builder.name(cacheClear.name());
        builder.keyType(cacheClear.keyType());
        builder.valueType(cacheClear.valueType());
        builder.valueParams(cacheClear.valueParams());

        builder.condition(cacheClear.condition());
        builder.unless(cacheClear.unless());
        return builder.beforeInvocation(cacheClear.beforeInvocation()).build();
    }

}