package com.igeeksky.xcache.aop;

import com.igeeksky.xcache.annotation.operation.CacheOperation;
import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.lang.Assert;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.NonNull;

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
    public Object invoke(@NonNull MethodInvocation invocation) throws Throwable {
        // 1. 获取包含缓存注解的方法
        Method method = invocation.getMethod();
        Object target = invocation.getThis();
        Assert.notNull(target, "Target must not be null");

        if (operationSource != null) {
            // 2. 根据注解类型，执行缓存操作
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
            Collection<CacheOperation> cacheOperations = operationSource.getCacheOperations(method, targetClass);
            if (CollectionUtils.isNotEmpty(cacheOperations)) {
                CacheOperationContext context = new CacheOperationContext(expressionEvaluator, cacheOperations,
                        cacheManager, invocation, method, target, targetClass);
                return context.execute();
            }
        }

        // 3. 返回缓存对象，或方法执行对象
        return invocation.proceed();
    }
}
