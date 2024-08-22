package com.igeeksky.xcache.aop;

import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author patrick
 * @since 0.0.4 2024/3/28
 */
public class CacheOperationExpressionEvaluator extends CachedExpressionEvaluator {

    private final Map<ExpressionKey, Expression> keyCache = new ConcurrentHashMap<>(64);

    private final Map<ExpressionKey, Expression> conditionCache = new ConcurrentHashMap<>(64);

    private final Map<ExpressionKey, Expression> unlessCache = new ConcurrentHashMap<>(64);

    public CacheOperationExpressionEvaluator(SpelExpressionParser parser) {
        super(parser);
    }

    public Object key(String keyExpression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
        return getExpression(this.keyCache, methodKey, keyExpression).getValue(evalContext);
    }

    public Object value(String valueExpression, AnnotatedElementKey methodKey, MethodBasedEvaluationContext evalContext) {
        return getExpression(this.keyCache, methodKey, valueExpression).getValue(evalContext);
    }

    public boolean condition(String conditionExpression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
        return (Boolean.TRUE.equals(getExpression(this.conditionCache, methodKey, conditionExpression).getValue(
                evalContext, Boolean.class)));
    }

    public boolean unless(String unlessExpression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
        return (Boolean.TRUE.equals(getExpression(this.unlessCache, methodKey, unlessExpression).getValue(
                evalContext, Boolean.class)));
    }

}
