package com.igeeksky.xcache.aop;

import com.igeeksky.xcache.annotation.operation.*;
import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheLoadingException;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.StringUtils;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.lang.NonNull;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


/**
 * 缓存操作上下文
 * <p>
 * 根据缓存注解，执行具体的缓存操作（或调用目标方法），并返回最终结果
 *
 * @author patrick
 * @since 0.0.4 2024/2/22
 */
@SuppressWarnings("unchecked")
public class CacheOperationContext {

    private final ParameterNameDiscoverer parameterNameDiscoverer;

    private final Map<Class<? extends CacheOperation>, CacheOperation> operations;

    private final CacheOperationExpressionEvaluator expressionEvaluator;

    private final CacheManager cacheManager;

    private final MethodInvocation invocation;

    private final Object target;

    private final Method method;

    private final AnnotatedElementKey methodKey;

    private final Object[] args;

    /**
     * 记录方法是否已执行，避免重复执行方法：代理方法最多只能执行一次
     */
    private boolean proceed = false;

    private Object result;

    private MethodBasedEvaluationContext context;

    public CacheOperationContext(CacheOperationExpressionEvaluator expressionEvaluator,
                                 Collection<CacheOperation> cacheOperations, CacheManager cacheManager,
                                 MethodInvocation invocation, Method method, Object target, Class<?> targetClass) {
        this.expressionEvaluator = expressionEvaluator;
        this.parameterNameDiscoverer = this.expressionEvaluator.getParameterNameDiscoverer();
        this.operations = HashMap.newHashMap(cacheOperations.size());
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
        CacheableOperation cacheableOperation = (CacheableOperation) operations.get(CacheableOperation.class);
        if (cacheableOperation != null) {
            this.processCacheable(cacheableOperation);
            return this.result;
        }

        CacheableAllOperation cacheableAllOperation = (CacheableAllOperation) operations.get(CacheableAllOperation.class);
        if (cacheableAllOperation != null) {
            this.processCacheableAll(cacheableAllOperation);
            return this.result;
        }

        List<Runnable> afterInvokeRunners = new ArrayList<>();
        CachePutOperation cachePutOperation = (CachePutOperation) operations.get(CachePutOperation.class);
        if (cachePutOperation != null) {
            this.processCachePut(cachePutOperation, afterInvokeRunners);
        }

        CachePutAllOperation cachePutAllOperation = (CachePutAllOperation) operations.get(CachePutAllOperation.class);
        if (cachePutAllOperation != null) {
            this.processCachePutAll(cachePutAllOperation, afterInvokeRunners);
        }

        CacheRemoveOperation cacheRemoveOperation = (CacheRemoveOperation) operations.get(CacheRemoveOperation.class);
        if (cacheRemoveOperation != null) {
            this.processCacheEvict(cacheRemoveOperation, afterInvokeRunners);
        }

        CacheRemoveAllOperation cacheRemoveAllOperation = (CacheRemoveAllOperation) operations.get(CacheRemoveAllOperation.class);
        if (cacheRemoveAllOperation != null) {
            this.processCacheEvictAll(cacheRemoveAllOperation, afterInvokeRunners);
        }

        CacheClearOperation cacheClearOperation = (CacheClearOperation) operations.get(CacheClearOperation.class);
        if (cacheClearOperation != null) {
            this.processCacheClear(cacheClearOperation, afterInvokeRunners);
        }

        this.proceed();

        afterInvokeRunners.forEach(Runnable::run);

        return this.result;
    }

    private void processCacheable(CacheableOperation operation) throws Throwable {
        if (this.conditionNotPassing(operation.getCondition())) {
            this.proceed();
            return;
        }

        // 使用 SpEL 获取 key
        Object key = this.generateKey(operation.getKey(), false);
        if (key == null) {
            this.proceed();
            return;
        }

        // 根据注解获取 cache 实例
        Cache<Object, Object> cache = this.getOrCreateCache(operation);

        // 读取缓存，然后判断是否已缓存值
        Object value = cache.getOrLoad(key, k -> {
            try {
                this.proceed();
                return this.unwrapReturnType(this.result);
            } catch (Throwable e) {
                throw new CacheLoadingException(e);
            }
        });

        if (!this.proceed) {
            this.result = this.wrapReturnType(this.method.getReturnType(), value);
        }
    }

    private void processCacheableAll(CacheableAllOperation operation) throws Throwable {
        if (this.conditionNotPassing(operation.getCondition())) {
            this.proceed();
            return;
        }
        // 使用 SpEL 获取 keys 集合
        Set<Object> keys = (Set<Object>) this.generateKey(operation.getKeys(), false);
        if (CollectionUtils.isEmpty(keys)) {
            this.proceed();
            return;
        }
        // 根据注解获取对应的缓存实例，并从缓存读取数据
        Cache<Object, Object> cache = this.getOrCreateCache(operation);
        Map<Object, CacheValue<Object>> cacheHits = cache.getAllCacheValues(keys);

        // 全部未命中缓存---------
        // 调用方法，并将方法结果集存入缓存，然后返回
        if (Maps.isEmpty(cacheHits)) {
            this.proceed();
            saveToCache(cache, keys, (Map<Object, Object>) unwrapReturnType(this.result));
            return;
        }

        // 移除已命中缓存的键
        keys.removeAll(cacheHits.keySet());
        Map<Object, Object> cacheResults = toCacheResults(cacheHits);
        // 全部命中缓存---------
        // 如果键集合为空，说明全部命中缓存，不再执行方法，直接将缓存结果集作为最终结果返回
        if (keys.isEmpty()) {
            this.result = wrapReturnType(this.method.getReturnType(), cacheResults);
            return;
        }

        // 部分命中缓存---------
        // 预创建未命中缓存的键集（避免调用方法后键被删除）
        List<Object> cacheMisses = new ArrayList<>(keys);
        this.proceed();
        Map<Object, Object> methodResults = (Map<Object, Object>) unwrapReturnType(this.result);
        // 如方法结果集为空，也可能要保存空值到缓存
        saveToCache(cache, cacheMisses, methodResults);

        // 当方法结果集为空，缓存结果集不为空，返回缓存结果集
        if (Maps.isEmpty(methodResults)) {
            if (Maps.isNotEmpty(cacheResults)) {
                this.result = wrapReturnType(this.method.getReturnType(), cacheResults);
            }
            return;
        }
        // 合并缓存结果集和方法结果集，形成最终结果集
        // 方法结果集不为空，那么不可能是无法添加元素的 Collections.emptyMap()，所以直接使用 methodResults 存放所有数据。
        methodResults.putAll(cacheResults);
    }

    /**
     * 将包含值的缓存数据转换为缓存结果集
     *
     * @param cacheHits 缓存命中的数据
     * @return 缓存结果集
     */
    private static Map<Object, Object> toCacheResults(Map<Object, CacheValue<Object>> cacheHits) {
        Map<Object, Object> cachedResult = HashMap.newHashMap(cacheHits.size());
        cacheHits.forEach((key, cacheValue) -> {
            if (cacheValue != null && cacheValue.hasValue()) {
                cachedResult.put(key, cacheValue.getValue());
            }
        });
        return cachedResult;
    }

    /**
     * 方法命中结果批量写入缓存
     *
     * @param cache         缓存实例
     * @param cacheMisses   未命中缓存的键集
     * @param methodResults 方法结果集
     */
    private static void saveToCache(Cache<Object, Object> cache, Collection<Object> cacheMisses,
                                    Map<Object, Object> methodResults) {
        Map<Object, Object> keyValues = HashMap.newHashMap(cacheMisses.size());
        // 提示：这里不能只保存有值的键集。
        // 如果缓存配置为支持保存空值，那么回源查询后依然无值的键集需缓存空值。
        if (Maps.isNotEmpty(methodResults)) {
            cacheMisses.forEach(key -> keyValues.put(key, methodResults.get(key)));
        } else {
            cacheMisses.forEach(key -> keyValues.put(key, null));
        }
        cache.putAll(keyValues);
    }

    private void processCachePut(CachePutOperation operation, List<Runnable> afterInvokeRunners) {
        if (this.conditionNotPassing(operation.getCondition())) {
            return;
        }

        afterInvokeRunners.add(() -> {
            if (!this.unlessPassing(operation.getUnless())) {
                return;
            }
            Object key = this.generateKey(operation.getKey(), true);
            if (key == null) {
                return;
            }
            Cache<Object, Object> cache = this.getOrCreateCache(operation);
            Object computed = this.computeValue(operation.getValue());
            if (computed instanceof CompletableFuture<?> future) {
                future.whenComplete((value, t) -> {
                    if (t == null) {
                        cache.put(key, value);
                    }
                });
                return;
            }
            cache.put(key, ObjectUtils.unwrapOptional(computed));
        });
    }

    private void processCachePutAll(CachePutAllOperation operation, List<Runnable> afterInvokeRunners) {
        if (this.conditionNotPassing(operation.getCondition())) {
            return;
        }

        afterInvokeRunners.add(() -> {
            if (!this.unlessPassing(operation.getUnless())) {
                return;
            }
            Object computed = this.computeValue(operation.getKeyValues());
            if (computed == null) {
                return;
            }
            if (computed instanceof CompletableFuture<?> future) {
                future.whenComplete((value, t) -> {
                    if (t == null) {
                        Map<Object, Object> keyValues = (Map<Object, Object>) value;
                        if (Maps.isNotEmpty(keyValues)) {
                            Cache<Object, Object> cache = this.getOrCreateCache(operation);
                            cache.putAll(keyValues);
                        }
                    }
                });
                return;
            }
            Map<Object, Object> keyValues = (Map<Object, Object>) ObjectUtils.unwrapOptional(computed);
            if (Maps.isNotEmpty(keyValues)) {
                Cache<Object, Object> cache = this.getOrCreateCache(operation);
                cache.putAll(keyValues);
            }
        });
    }

    private void processCacheEvict(CacheRemoveOperation operation, List<Runnable> afterInvokeRunners) {
        if (this.conditionNotPassing(operation.getCondition())) {
            return;
        }

        if (operation.isBeforeInvocation()) {
            doEvict(operation, false);
            return;
        }

        afterInvokeRunners.add(() -> {
            if (this.unlessPassing(operation.getUnless())) {
                doEvict(operation, true);
            }
        });
    }

    private void doEvict(CacheRemoveOperation operation, boolean afterInvocation) {
        Object key = this.generateKey(operation.getKey(), afterInvocation);
        if (key == null) {
            return;
        }
        this.getOrCreateCache(operation).remove(key);
    }

    private void processCacheEvictAll(CacheRemoveAllOperation operation, List<Runnable> afterInvokeRunners) {
        if (this.conditionNotPassing(operation.getCondition())) {
            return;
        }

        if (operation.isBeforeInvocation()) {
            doEvictAll(operation, false);
            return;
        }

        afterInvokeRunners.add(() -> {
            if (this.unlessPassing(operation.getUnless())) {
                doEvictAll(operation, true);
            }
        });
    }

    private void doEvictAll(CacheRemoveAllOperation operation, boolean afterInvocation) {
        Set<Object> keys = (Set<Object>) this.generateKey(operation.getKeys(), afterInvocation);
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        Cache<Object, Object> cache = this.getOrCreateCache(operation);
        cache.removeAll(keys);
    }

    private void processCacheClear(CacheClearOperation operation, List<Runnable> afterInvokeRunners) {
        if (this.conditionNotPassing(operation.getCondition())) {
            return;
        }

        if (operation.isBeforeInvocation()) {
            this.getOrCreateCache(operation).clear();
            return;
        }

        afterInvokeRunners.add(() -> {
            if (this.unlessPassing(operation.getUnless())) {
                this.getOrCreateCache(operation).clear();
            }
        });
    }

    /**
     * 根据表达式生成一个唯一键
     *
     * @param expression 表达式如果为空或无文本内容，则返回方法参数中的第一个对象
     * @return 如果无法根据表达式生成键，则返回参数中的第一个对象
     */
    private Object generateKey(String expression, boolean afterInvocation) {
        // 检查表达式是否有文本内容
        if (StringUtils.hasText(expression)) {
            // 创建一个基于方法的评估上下文，用于解析表达式
            MethodBasedEvaluationContext context = this.createEvaluationContext();
            if (afterInvocation) {
                context.setVariable("result", this.result);
            }
            // 使用表达式评估器生成键
            return this.expressionEvaluator.key(expression, this.methodKey, context);
        }
        // 如果表达式为空或无文本内容，直接返回参数中的第一个对象
        return this.args[0];
    }

    private Object computeValue(String expression) {
        if (StringUtils.hasText(expression)) {
            MethodBasedEvaluationContext context = this.createEvaluationContext();
            context.setVariable("result", this.result);
            return this.expressionEvaluator.value(expression, this.methodKey, context);
        }
        return result;
    }

    /**
     * 根据 condition 判断是否需要缓存操作
     */
    private boolean conditionNotPassing(String expression) {
        if (StringUtils.hasText(expression)) {
            MethodBasedEvaluationContext context = createEvaluationContext();
            return !this.expressionEvaluator.condition(expression, this.methodKey, context);
        }
        return false;
    }

    private boolean unlessPassing(String expression) {
        if (StringUtils.hasText(expression)) {
            MethodBasedEvaluationContext context = createEvaluationContext();
            context.setVariable("result", this.result);
            return !this.expressionEvaluator.unless(expression, this.methodKey, context);
        }
        return true;
    }

    @NonNull
    private MethodBasedEvaluationContext createEvaluationContext() {
        if (this.context == null) {
            this.context = new MethodBasedEvaluationContext(this.target, this.method,
                    this.args, this.parameterNameDiscoverer);
        }
        return this.context;
    }

    /**
     * 执行被注解方法
     *
     * @throws Throwable 被注解方法执行过程中可能抛出的异常
     */
    private void proceed() throws Throwable {
        if (!this.proceed) {
            this.result = this.invocation.proceed();
            this.proceed = true;
        }
    }

    private Object unwrapReturnType(Object returnValue) throws ExecutionException, InterruptedException {
        if (returnValue == null) {
            return null;
        }
        if (returnValue instanceof CompletableFuture) {
            return ((CompletableFuture<?>) returnValue).get();
        }
        return ObjectUtils.unwrapOptional(returnValue);
    }

    private Object wrapReturnType(Class<?> returnType, Object cacheHitValue) {
        if (CompletableFuture.class.isAssignableFrom(returnType)) {
            return CompletableFuture.completedFuture(cacheHitValue);
        }
        if (Optional.class.isAssignableFrom(returnType)) {
            return Optional.ofNullable(cacheHitValue);
        }
        return cacheHitValue;
    }

    private Cache<Object, Object> getOrCreateCache(CacheOperation operation) {
        return (Cache<Object, Object>) this.cacheManager.getOrCreateCache(operation.getName(),
                operation.getKeyType(), operation.getValueType());
    }

}