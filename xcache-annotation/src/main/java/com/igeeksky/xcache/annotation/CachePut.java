package com.igeeksky.xcache.annotation;

import org.springframework.aot.hint.annotation.Reflective;

import java.lang.annotation.*;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 缓存数据更新注解
 * <p>
 * 添加此注解的方法，将更新指定的单个缓存元素。
 * <p>
 * 如果一个类中有多个同名缓存的注解，name, keyType, valueType，
 * 这三个公共属性可用类注解 {@link CacheConfig} 配置，此注解保持默认即可。
 * <p>
 * <b>注意</b>：<p>
 * 1. 请勿与 {@link Cacheable} 或 {@link CacheableAll} 注解用于同一方法。<p>
 * 2. 如使用 SpEL表达式通过参数名获取数据，项目编译时必须使用 -parameters 记录方法参数信息，否则无法正确解析 SpEL表达式。<br>
 * 如使用 maven-compiler-plugin，必须配置：{@code <parameters>true</parameters> }
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-12
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface CachePut {

    /**
     * SpEL表达式，用于从参数中提取键。
     * <p>
     * 如果未配置，采用被注解方法的第一个参数作为键。
     */
    String key() default "";

    /**
     * SpEL表达式，用于从参数中提取待缓存的值。
     * <p>
     * 如果未配置，默认采用方法返回值作为缓存值。
     * <p>
     * 值可以是 {@link CompletableFuture} 或 {@link Optional} 类型，缓存框架会自动拆装获取原值并缓存。
     */
    String value() default "";

    /**
     * SpEL表达式，用于判断是否执行缓存操作。
     * <p>
     * 如果未配置，默认为 true。
     * <p>
     * 调用被注解方法前解析此表达式，如 condition 表达式结果为 false，不执行缓存操作。
     */
    String condition() default "";

    /**
     * SpEL表达式
     * <p>
     * 如果未配置，默认为 false。
     * <p>
     * 调用被注解方法后解析此表达式，如 unless 表达式结果为 true，不执行缓存操作。
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
     * 值类型
     */
    Class<?> valueType() default Undefined.class;

}
