package com.igeeksky.xcache.annotation;

import org.springframework.aot.hint.annotation.Reflective;

import java.lang.annotation.*;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-13
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface CacheEvictAll {

    /**
     * SpEL表达式，用于从参数中提取 key 集合
     */
    String keys() default "";

    /**
     * SpEL表达式，方法执行前：当表达式结果为 true 时，缓存(get and put)
     */
    String condition() default "";

    /**
     * SpEL表达式，方法执行后：当表达式结果为 true 时，不缓存
     */
    String unless() default "";

    /**
     * true：执行方法前驱逐缓存元素；
     * false：执行方法后驱逐缓存元素；
     * 默认值：false
     */
    boolean beforeInvocation() default false;

    /**
     * 缓存名称
     * <p>
     * 如果类级别已注解 {@link CacheConfig}，name, keyType, keyParams, valueType, valueParams
     * 这五个属性无需再在此配置，将使用 {@link CacheConfig} 中的所有同名属性。<p>
     * 如果类级别未注解 {@link CacheConfig} ，或此方法的 name 与 {@link CacheConfig} 的不同，
     * 则至少需配置 name, keyType, valueType 这三个属性，否则将抛出 {@link IllegalArgumentException}
     * <p>
     * <b>其它异常情况</b><p>
     * 当此注解的 name 为空，或与 {@link CacheConfig} 的 name 相同时：
     * 如果此注解的 keyType, keyParams, valueType, valueParams 存在非默认值，
     * 且与 类注解 {@link CacheConfig} 中的同名属性不同，将抛出 {@link IllegalArgumentException}
     */
    String name() default "";

    /**
     * 键类型
     * <p>
     * 如果未配置，使用 {@link CacheConfig} 中的同名属性。
     */
    Class<?> keyType() default Undefined.class;

    /**
     * 键的泛型参数类型
     * <p>
     * 如果未配置，使用 {@link CacheConfig} 中的同名属性。
     * <p>
     * <b>其它异常情况</b><p>
     * 如果 name 为空，或 name 与 {@link CacheConfig} 相同，
     * 但此属性非默认值且与 {@link CacheConfig} 不同，抛出 {@link IllegalArgumentException}
     */
    Class<?>[] keyParams() default {};

    /**
     * 值类型
     * <p>
     * 如果未配置，使用 {@link CacheConfig} 中的同名属性。
     * <p>
     * <b>其它异常情况</b><p>
     * 如果 name 为空，或 name 与 {@link CacheConfig} 相同，
     * 但此属性非默认值且与 {@link CacheConfig} 不同，抛出 {@link IllegalArgumentException}
     */
    Class<?> valueType() default Undefined.class;

    /**
     * 值的泛型参数类型
     * <p>
     * 如果未配置，使用 {@link CacheConfig} 中的同名属性。
     * <p>
     * <b>其它异常情况</b><p>
     * 如果 name 为空，或 name 与 {@link CacheConfig} 相同，
     * 但此属性非默认值且与 {@link CacheConfig} 不同，抛出 {@link IllegalArgumentException}
     */
    Class<?>[] valueParams() default {};

}
