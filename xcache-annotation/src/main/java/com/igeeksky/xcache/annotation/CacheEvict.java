package com.igeeksky.xcache.annotation;

import org.springframework.aot.hint.annotation.Reflective;

import java.lang.annotation.*;

/**
 * 缓存注解
 * <p>
 * 添加此注解的方法，将逐出特定的缓存元素。
 * <p>
 * 如果同一类中有多个方法使用同一缓存，则可以使用 {@link CacheConfig} 在类注解中配置
 * name, keyType, keyParams, valueType, valueParams，此注解的这五个属性保持默认即可。
 * <p>
 * 注意：请勿与 {@link Cacheable} 或 {@link CacheableAll} 注解同时使用。
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-12
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface CacheEvict {

    /**
     * SpEL表达式，用于从参数中提取键。
     * <p>
     * 如果未配置，采用被注解方法的第一个参数作为键。
     */
    String key() default "";

    /**
     * SpEL表达式
     * <p>
     * 如果 condition 表达式结果为 true，beforeInvocation 为 true ，调用被注解方法前执行缓存操作 (evict) ：<p>
     * 如果 condition 表达式结果为 false，无论 unless 表达式结果是否为 false，一定不会执行缓存操作。
     * <p>
     * 如果未配置，表达式结果默认为 true
     */
    String condition() default "";

    /**
     * SpEL表达式
     * <p>
     * 如果 condition 表达式结果为 true，beforeInvocation 为 false，
     * 且 unless 表达式结果为 false，调用被注解方法后执行缓存操作 (evict)
     * <p>
     * 如果未配置，表达式结果默认为 false
     */
    String unless() default "";

    /**
     * true： 调用被注解方法前驱逐缓存元素；<p>
     * false：调用被注解方法后驱逐缓存元素。
     * <p>
     * 默认值：false
     */
    boolean beforeInvocation() default false;

    /**
     * 缓存名称
     */
    String name() default "";

    /**
     * 键类型
     */
    Class<?> keyType() default Undefined.class;

    /**
     * 键泛型参数
     */
    Class<?>[] keyParams() default {};

    /**
     * 值类型
     */
    Class<?> valueType() default Undefined.class;

    /**
     * 值泛型参数
     */
    Class<?>[] valueParams() default {};

}
