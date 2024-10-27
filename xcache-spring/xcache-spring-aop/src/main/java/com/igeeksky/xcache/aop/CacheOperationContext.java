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
        this.operations = Maps.newHashMap(cacheOperations.size());
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

        CacheEvictOperation cacheEvictOperation = (CacheEvictOperation) operations.get(CacheEvictOperation.class);
        if (cacheEvictOperation != null) {
            this.processCacheEvict(cacheEvictOperation, afterInvokeRunners);
        }

        CacheEvictAllOperation cacheEvictAllOperation = (CacheEvictAllOperation) operations.get(CacheEvictAllOperation.class);
        if (cacheEvictAllOperation != null) {
            this.processCacheEvictAll(cacheEvictAllOperation, afterInvokeRunners);
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
        // 使用 SpEL 获取 key
        Object key = this.getKey(operation.getKey(), operation.getCondition());
        if (key == null) {
            this.proceed();
            return;
        }

        // 根据注解获取 cache 实例
        Cache<Object, Object> cache = this.getOrCreateCache(operation);

        // 读取缓存，然后判断是否已缓存值
        Object value = cache.get(key, k -> {
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
        // 使用 SpEL 获取 keys 集合
        Set<Object> keys = (Set<Object>) this.getKey(operation.getCondition(), operation.getKeys());
        if (CollectionUtils.isEmpty(keys)) {
            this.proceed();
            return;
        }

        // 根据注解获取对应的缓存实例，并从缓存读取数据
        Cache<Object, Object> cache = this.getOrCreateCache(operation);
        Map<Object, CacheValue<Object>> cacheHits = cache.getAll(keys);

        // 移除已缓存的键
        keys.removeAll(cacheHits.keySet());
        // 如果集合为空，说明所有数据均已缓存，不执行方法，直接将缓存结果集作为最终结果并返回
        if (keys.isEmpty()) {
            this.result = wrapReturnType(this.method.getReturnType(), toCachedResult(cacheHits));
            return;
        }

        // 预创建待缓存数据集
        Map<Object, Object> cachePuts = createNullValueCachePuts(keys);
        // 执行方法
        this.proceed();
        Map<Object, Object> resultMap = (Map<Object, Object>) unwrapReturnType(this.result);
        // 方法结果集替换原有空值，并存入到缓存
        saveToCache(cache, cachePuts, resultMap);

        // 如果缓存命中数据为空，直接返回方法结果集
        if (Maps.isEmpty(cacheHits)) {
            return;
        }

        // 如果方法结果集为空，直接返回缓存命中数据
        Map<Object, Object> cachedResult = toCachedResult(cacheHits);
        if (Maps.isEmpty(resultMap)) {
            this.result = wrapReturnType(this.method.getReturnType(), cachedResult);
            return;
        }

        // 合并缓存结果集和方法结果集，形成最终结果集
        resultMap.putAll(cachedResult);
    }

    /**
     * 将含值的缓存数据转换为缓存结果集
     *
     * @param cacheHits 缓存命中的数据
     * @return 缓存结果集
     */
    private static Map<Object, Object> toCachedResult(Map<Object, CacheValue<Object>> cacheHits) {
        Map<Object, Object> cachedResult = Maps.newHashMap(cacheHits.size());
        cacheHits.forEach((k, cv) -> {
            if (cv.hasValue()) {
                cachedResult.put(k, cv.getValue());
            }
        });
        return cachedResult;
    }

    /**
     * 为了避免执行方法时删除键，先将所有待存入缓存的值都预设为空
     *
     * @param keys 待存入缓存的键集合
     * @return 预设为空的键值对集合
     */
    private static Map<Object, Object> createNullValueCachePuts(Set<Object> keys) {
        Map<Object, Object> cachePuts = Maps.newHashMap(keys.size());
        keys.forEach(k -> cachePuts.put(k, null));
        return cachePuts;
    }

    /**
     * 将待存入缓存的数据批量写入缓存
     * <p>
     * 如果方法执行结果不为空，替换对应空值
     *
     * @param cache     缓存实例
     * @param cachePuts 待存入缓存的数据（值为空）
     * @param result    方法执行结果
     */
    private static void saveToCache(Cache<Object, Object> cache, Map<Object, Object> cachePuts, Map<Object, Object> result) {
        if (Maps.isNotEmpty(result)) {
            cachePuts.putAll(result);
        }
        cache.putAll(cachePuts);
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
            Map<Object, Object> keyValues = (Map<Object, Object>) this.computeValue(operation.getKeyValues());
            if (Maps.isEmpty(keyValues)) {
                return;
            }
            Cache<Object, Object> cache = this.getOrCreateCache(operation);
            cache.putAll(keyValues);
        });
    }

    private void processCacheEvict(CacheEvictOperation operation, List<Runnable> afterInvokeRunners) {
        if (!this.conditionPassing(operation.getCondition())) {
            return;
        }

        if (operation.isBeforeInvocation()) {
            doEvict(operation);
            return;
        }

        afterInvokeRunners.add(() -> {
            if (this.unlessPassing(operation.getUnless())) {
                doEvict(operation);
            }
        });
    }

    private void doEvict(CacheEvictOperation operation) {
        Object key = this.generateKey(operation.getKey());
        if (key == null) {
            return;
        }
        this.getOrCreateCache(operation).evict(key);
    }

    private void processCacheEvictAll(CacheEvictAllOperation operation, List<Runnable> afterInvokeRunners) {
        if (!this.conditionPassing(operation.getCondition())) {
            return;
        }

        if (operation.isBeforeInvocation()) {
            doEvictAll(operation);
            return;
        }

        afterInvokeRunners.add(() -> {
            if (this.unlessPassing(operation.getUnless())) {
                doEvictAll(operation);
            }
        });
    }

    private void doEvictAll(CacheEvictAllOperation operation) {
        Set<Object> keys = (Set<Object>) this.generateKey(operation.getKeys());
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        Cache<Object, Object> cache = this.getOrCreateCache(operation);
        cache.evictAll(keys);
    }


    private void processCacheClear(CacheClearOperation operation, List<Runnable> afterInvokeRunners) {
        if (!this.conditionPassing(operation.getCondition())) {
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

    private Object getKey(String condition, String key) {
        if (this.conditionPassing(condition)) {
            return this.generateKey(key);
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
            MethodBasedEvaluationContext context = this.createEvaluationContext();
            context.setVariable("result", this.result);
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
    private boolean conditionPassing(String expression) {
        if (StringUtils.hasText(expression)) {
            MethodBasedEvaluationContext context = createEvaluationContext();
            return this.expressionEvaluator.condition(expression, this.methodKey, context);
        }
        return true;
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
                operation.getKeyType(), operation.getKeyParams(),
                operation.getValueType(), operation.getValueParams());
    }

}