package com.igeeksky.xcache.annotation;

import org.springframework.aot.hint.annotation.Reflective;

import java.lang.annotation.*;

/**
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-12
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface Cacheable {

    /**
     * SpEL表达式，用于从参数中提取 key，如果为空，则使用被注解方法的第一个参数
     */
    String key() default "";

    /**
     * SpEL表达式，方法执行前：当表达式结果为 true 时，执行缓存方法
     */
    String condition() default "";

    /**
     * SpEL表达式，方法执行后：当表达式结果为 true 时，不缓存
     */
    String unless() default "";

    /**
     * 缓存名称
     * <p>
     * 如果类级别有注解 {@link CacheConfig}，且正是此注解的目标缓存, 则 name 属性无需在此配置；<p>
     * 如果类级别无注解 {@link CacheConfig}，或并非此注解的目标缓存，则 name 属性必须在此配置。
     * <b>其它异常情况</b><p>
     * 当此注解的 name 为空，或与 {@link CacheConfig} 的 name 相同时：
     * 如果此注解的 keyType, keyParams, valueType, valueParams 存在非默认值，
     * 且与 类注解 {@link CacheConfig} 中的同名属性不同，将抛出 {@link IllegalArgumentException}
     */
    String name() default "";

    /**
     * 键类型
     * <p>
     * 如果类级别有注解 {@link CacheConfig}，且正是此注解的目标缓存, 则 keyType 属性无需在此配置；<p>
     * 如果类级别无注解 {@link CacheConfig}，或并非此注解的目标缓存，则 keyType 属性必须在此配置。
     * <p>
     * 如果此注解的 name 为空，或 name 与 {@link CacheConfig} 配置的相同，
     * 但此属性并非默认值，且与 {@link CacheConfig} 配置的值不同，抛出 {@link IllegalArgumentException}
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