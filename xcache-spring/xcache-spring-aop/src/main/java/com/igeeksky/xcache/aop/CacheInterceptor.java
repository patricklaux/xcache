package com.igeeksky.xcache.aop;

import com.igeeksky.xcache.annotation.operation.CacheOperation;
import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.lang.Assert;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-13
 */
public class CacheInterceptor implements MethodInterceptor, Serializable {

    public static final String CACHE_INTERCEPTOR_BEAN_NAME = "com.igeeksky.xcache.aop.cacheInterceptor";

    private final CacheOperationExpressionEvaluator expressionEvaluator;

    private final CacheManager cacheManager;

    /**
     * 获取方法上的缓存注解及对应操作
     */
    private CacheOperationSource operationSource;

    public CacheInterceptor(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        // Parser 的 compiler 配置
        this.expressionEvaluator = new CacheOperationExpressionEvaluator(new SpelExpressionParser());
    }

    public void setCacheOperationSource(CacheOperationSource operationSource) {
        this.operationSource = operationSource;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (operationSource == null) {
            return invocation.proceed();
        }

        // 1. 获取可能存在缓存注解的方法
        Method method = invocation.getMethod();
        Object target = invocation.getThis();
        Assert.notNull(target, "Target must not be null");

        // 2. 获取缓存注解的操作类型集合（一个方法可能有多个缓存注解）
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
        Collection<CacheOperation> cacheOperations = operationSource.getCacheOperations(method, targetClass);
        if (CollectionUtils.isEmpty(cacheOperations)) {
            return invocation.proceed();
        }

        // 3. 执行缓存操作，或反射调用方法，返回具体结果
        CacheOperationContext context = new CacheOperationContext(expressionEvaluator, cacheOperations,
                cacheManager, invocation, method, target, targetClass);
        return context.execute();
    }
}
