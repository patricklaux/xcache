package com.igeeksky.xcache.annotation;

import org.springframework.aot.hint.annotation.Reflective;

import java.lang.annotation.*;

/**
 * 缓存注解
 * <p>
 * 批量更新特定的缓存元素。
 * <p>
 * 如果一个类中使用多个缓存注解，name, keyType, keyParams, valueType, valueParams
 * 这五个公共属性可用类注解 {@link CacheConfig} 配置，此注解保持默认即可。
 * <p>
 * <b>注意</b>：请勿与 {@link Cacheable} 或 {@link CacheableAll} 注解用于同一方法。
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface CachePutAll {

    /**
     * SpEL表达式，用于从参数中提取键值对集合 (Map)。
     * <p>
     * 如果未配置，采用被注解方法的第一个参数作为键值对集合 (Map)。
     */
    String keyValues() default "";

    /**
     * SpEL表达式
     * <p>
     * 如果未配置，condition 表达式结果默认为 true。
     * <p>
     * 如果 condition 表达式结果为 true，且 unless 表达式结果为 false，调用被注解方法后执行缓存操作 (putAll)
     */
    String condition() default "";

    /**
     * SpEL表达式
     * <p>
     * 如果未配置，unless 表达式结果默认为 false。
     * <p>
     * 如果 condition 表达式结果为 true，且 unless 表达式结果为 false，调用被注解方法后执行缓存操作 (putAll)
     */
    String unless() default "";

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
