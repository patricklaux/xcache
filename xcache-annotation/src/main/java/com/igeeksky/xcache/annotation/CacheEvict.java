package com.igeeksky.xcache.annotation;

import org.springframework.aot.hint.annotation.Reflective;

import java.lang.annotation.*;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-12
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface CacheEvict {

    /**
     * SpEL表达式，用于从参数中提取 key
     */
    String key() default "";

    /**
     * SpEL表达式，方法执行前：当表达式结果为 true 时，缓存(get and put)
     */
    String condition() default "";

    /**
     * SpEL表达式，方法执行后：当表达式结果为 true 时，不缓存
     */
    String unless() default "";

    /**
     * 缓存名称
     */
    String name();

    /**
     * 键类型
     */
    Class<?> keyType() default Object.class;

    /**
     * 值类型
     */
    Class<?> valueType();

    /**
     * 值的泛型参数
     */
    Class<?>[] valueParams() default {};

    /**
     * true：执行方法前驱逐缓存元素；
     * false：执行方法后驱逐缓存元素；
     * 默认值：false
     */
    boolean beforeInvocation() default false;

}
