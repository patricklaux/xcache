package com.igeeksky.xcache.aop;

import com.igeeksky.xcache.annotation.operation.*;
import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.StringUtils;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.*;


/**
 * @author patrick
 * @since 0.0.4 2024/2/22
 */
@SuppressWarnings("unchecked")
public class CacheOperationContext {

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private final Map<Class<? extends CacheOperation>, CacheOperation> operations;

    private final CacheOperationExpressionEvaluator expressionEvaluator;

    private final CacheManager cacheManager;

    private final MethodInvocation invocation;

    private final Object target;

    private final Method method;

    private final AnnotatedElementKey methodKey;

    private final Object[] args;

    /**
     * 记录方法是否已执行，避免重复执行方法：代理的方法仅能执行一次
     */
    private boolean proceed = false;

    private Object result;

    public CacheOperationContext(CacheOperationExpressionEvaluator expressionEvaluator, Collection<CacheOperation> cacheOperations, CacheManager cacheManager, MethodInvocation invocation, Method method, Object target, Class<?> targetClass) {
        this.expressionEvaluator = expressionEvaluator;
        this.operations = new HashMap<>(cacheOperations.size());
        for (CacheOperation cacheOperation : cacheOperations) {
            this.operations.put(cacheOperation.getClass(), cacheOperation);
        }
        this.cacheManager = cacheManager;
        this.invocation = invocation;
        this.target = target;
        this.method = BridgeMethodResolver.findBridgedMethod(method);
        this.methodKey = new AnnotatedElementKey(method, targetClass);
        this.args = invocation.getArguments();
    }

    public Object execute() throws Throwable {
        List<Runnable> afterInvokeRunners = new LinkedList<>();

        // TODO cacheable & cacheableAll 不能与其它注解共同用于同一个方法
        CacheableOperation cacheableOperation = (CacheableOperation) operations.get(CacheableOperation.class);
        if (cacheableOperation != null) {
            processCacheable(cacheableOperation, afterInvokeRunners);
        }

        CacheableAllOperation cacheableAllOperation = (CacheableAllOperation) operations.get(CacheableAllOperation.class);
        if (cacheableAllOperation != null) {
            processCacheableAll(cacheableAllOperation, afterInvokeRunners);
        }

        CachePutOperation cachePutOperation = (CachePutOperation) operations.get(CachePutOperation.class);
        if (cachePutOperation != null) {
            processCachePut(cachePutOperation, afterInvokeRunners);
        }

        CachePutAllOperation cachePutAllOperation = (CachePutAllOperation) operations.get(CachePutAllOperation.class);
        if (cachePutAllOperation != null) {
            processCachePutAll(cachePutAllOperation, afterInvokeRunners);
        }

        CacheEvictOperation cacheEvictOperation = (CacheEvictOperation) operations.get(CacheEvictOperation.class);
        if (cacheEvictOperation != null) {
            processCacheEvict(cacheEvictOperation, afterInvokeRunners);
        }

        CacheEvictAllOperation cacheEvictAllOperation = (CacheEvictAllOperation) operations.get(CacheEvictAllOperation.class);
        if (cacheEvictAllOperation != null) {
            processCacheEvictAll(cacheEvictAllOperation, afterInvokeRunners);
        }

        CacheClearOperation cacheClearOperation = (CacheClearOperation) operations.get(CacheClearOperation.class);
        if (cacheClearOperation != null) {
            processCacheClear(cacheClearOperation, afterInvokeRunners);
        }

        proceed();

        afterInvokeRunners.forEach(Runnable::run);

        return result;
    }

    private void processCacheable(CacheableOperation operation, List<Runnable> afterInvokeRunners) {
        // 提取 Key
        Object key = this.getKey(operation.getCondition(), operation.getKey());
        if (key == null) {
            return;
        }

        // 根据注解获取 cache 实例
        Cache<Object, Object> cache = this.getOrCreateCache(operation);

        // 读取缓存，然后判断是否已缓存值
        CacheValue<Object> cacheValue = cache.get(key);
        if (cacheValue != null) {
            this.result = cacheValue.getValue();
            this.proceed = true;
            return;
        }

        afterInvokeRunners.add(() -> {
            // 根据注解 unless 判断方法执行结果是否缓存
            if (this.unlessPassing(operation.getUnless())) {
                cache.put(key, result);
            }
        });
    }

    private void processCacheableAll(CacheableAllOperation operation, List<Runnable> afterInvokeRunners) {
        // 使用 SpEL 获取 keys 集合
        Set<Object> keys = (Set<Object>) this.getKey(operation.getCondition(), operation.getKeys());
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }

        // 根据注解获取对应的 cache 实例，并从缓存读取数据
        Cache<Object, Object> cache = this.getOrCreateCache(operation);
        Map<Object, CacheValue<Object>> cachedKeyValues = cache.getAll(keys);

        // 移除已缓存的键，然后再从数据源查询数据
        keys.removeAll(cachedKeyValues.keySet());

        Map<Object, Object> temp = new HashMap<>(cachedKeyValues.size() / 3 * 4 + 1);
        cachedKeyValues.forEach((k, cv) -> {
            if (cv.hasValue()) {
                temp.put(k, cv.getValue());
            }
        });

        if (keys.isEmpty()) {
            this.result = temp;
            this.proceed = true;
            return;
        }

        afterInvokeRunners.add(() -> {
            // 根据注解 unless 判断是否缓存方法执行结果
            Map<Object, Object> resultMap = ((Map<Object, Object>) result);
            if (Maps.isNotEmpty(resultMap)) {
                if (this.unlessPassing(operation.getUnless())) {
                    cache.putAll(resultMap);
                }
                if (!temp.isEmpty()) {
                    resultMap.putAll(temp);
                }
            } else {
                this.result = temp;
            }
        });
    }

    private void processCachePut(CachePutOperation operation, List<Runnable> afterInvokeRunners) {
        if (!this.conditionPassing(operation.getCondition())) {
            return;
        }
        afterInvokeRunners.add(() -> {
            if (!this.unlessPassing(operation.getUnless())) {
                return;
            }
            Object key = this.generateKey(operation.getKey());
            if (key == null) {
                return;
            }
            Cache<Object, Object> cache = this.getOrCreateCache(operation);
            cache.put(key, this.computeValue(operation.getValue()));
        });
    }

    private void processCachePutAll(CachePutAllOperation operation, List<Runnable> afterInvokeRunners) {
        if (!this.conditionPassing(operation.getCondition())) {
            return;
        }
        afterInvokeRunners.add(() -> {
            if (!this.unlessPassing(operation.getUnless())) {
                return;
            }
            Map<Object, Object> keyValues = (Map<Object, Object>) this.generateKey(operation.getKeyValues());
            if (Maps.isEmpty(keyValues)) {
                return;
            }
            Cache<Object, Object> cache = this.getOrCreateCache(operation);
            cache.putAll(keyValues);
        });
    }

    private void processCacheEvict(CacheEvictOperation operation, List<Runnable> afterInvokeRunners) {
        Object key = getKey(operation.getCondition(), operation.getKey());
        if (key == null) {
            return;
        }

        if (operation.isBeforeInvocation()) {
            Cache<Object, Object> cache = getOrCreateCache(operation);
            cache.evict(key);
            return;
        }

        afterInvokeRunners.add(() -> {
            if (unlessPassing(operation.getUnless())) {
                Cache<Object, Object> cache = getOrCreateCache(operation);
                cache.evict(key);
            }
        });
    }

    private void processCacheEvictAll(CacheEvictAllOperation operation, List<Runnable> afterInvokeRunners) {
        // 使用 SpEL 提取 keys 集合
        Set<Object> keys = (Set<Object>) getKey(operation.getCondition(), operation.getKeys());
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }

        Cache<Object, Object> cache = getOrCreateCache(operation);

        if (operation.isBeforeInvocation()) {
            cache.evictAll(keys);
            return;
        }

        afterInvokeRunners.add(() -> {
            if (this.unlessPassing(operation.getUnless())) {
                cache.evictAll(keys);
            }
        });
    }


    private void processCacheClear(CacheClearOperation operation, List<Runnable> afterInvokeRunners) {
        if (!conditionPassing(operation.getCondition())) {
            return;
        }
        if (operation.isBeforeInvocation()) {
            Cache<Object, Object> cache = getOrCreateCache(operation);
            cache.clear();
            return;
        }

        afterInvokeRunners.add(() -> {
            if (this.unlessPassing(operation.getUnless())) {
                Cache<Object, Object> cache = getOrCreateCache(operation);
                cache.clear();
            }
        });
    }

    private Object getKey(String condition, String key) {
        if (conditionPassing(condition)) {
            return generateKey(key);
        }
        return null;
    }

    /**
     * 根据表达式生成一个唯一键
     *
     * @param expression 表达式如果为空或无文本内容，则返回方法参数中的第一个对象
     * @return 如果无法根据表达式生成键，则返回参数中的第一个对象
     */
    private Object generateKey(String expression) {
        // 检查表达式是否有文本内容
        if (StringUtils.hasText(expression)) {
            // 创建一个基于方法的评估上下文，用于解析表达式
            MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(this.target, this.method,
                    this.args, this.parameterNameDiscoverer);
            // 使用表达式评估器生成键
            return this.expressionEvaluator.key(expression, this.methodKey, context);
        }
        // 如果表达式为空或无文本内容，直接返回参数中的第一个对象
        return this.args[0];
    }

    private Object computeValue(String expression) {
        if (StringUtils.hasText(expression)) {
            MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(this.target, this.method,
                    this.args, this.parameterNameDiscoverer);
            context.setVariable("result", this.result);
            return this.expressionEvaluator.value(expression, this.methodKey, context);
        }
        return this.args[1];
    }

    /**
     * 根据 condition 判断是否需要缓存操作
     */
    private boolean conditionPassing(String expression) {
        if (StringUtils.hasText(expression)) {
            MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(this.target, this.method,
                    this.args, this.parameterNameDiscoverer);
            return this.expressionEvaluator.condition(expression, this.methodKey, context);
        }
        return true;
    }

    private boolean unlessPassing(String expression) {
        if (StringUtils.hasText(expression)) {
            MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(this.target, this.method,
                    this.args, this.parameterNameDiscoverer);
            context.setVariable("result", this.result);
            return !this.expressionEvaluator.unless(expression, this.methodKey, context);
        }
        return true;
    }

    private void proceed() throws Throwable {
        if (!this.proceed) {
            this.result = this.invocation.proceed();
            this.proceed = true;
        }
    }

    private Cache<Object, Object> getOrCreateCache(CacheOperation operation) {
        return (Cache<Object, Object>) this.cacheManager.getOrCreateCache(operation.getName(),
                operation.getKeyType(), operation.getKeyParams(),
                operation.getValueType(), operation.getValueParams());
    }

}