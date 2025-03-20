package com.igeeksky.xcache.annotation;

import org.springframework.aot.hint.annotation.Reflective;

import java.lang.annotation.*;

/**
 * 缓存注解
 * <p>
 * 如果数据未缓存，则反射执行方法并缓存；<br>
 * 如果数据已缓存，则直接返回已缓存数据。
 * <p>
 * 如果一个类中有多个同名缓存实例的注解，{@code name}, {@code keyType}, {@code valueType}，
 * 这三个公共属性可用类注解 {@link CacheConfig} 配置，此注解保持默认即可。
 * <p>
 * <b>注意</b>：
 * <p>
 * 1. 请勿与其它缓存注解用于同一方法！<br>
 * 当成功从缓存获取数据时，被注解方法将不会被调用。
 * <p>
 * 2. 如使用 SpEL表达式通过参数名获取数据，项目编译时需使用 {@code -parameters } 记录方法参数名信息，否则无法正确解析。<br>
 * 如使用 maven-compiler-plugin，需配置：{@code <parameters>true</parameters> }
 * <p>
 * 3. 被注解方法的返回值可以是 {@code Optional } 或 {@code CompletableFuture } 类型。 <br>
 * 缓存框架会通过 {@code CompletableFuture.get()} 或 {@code Optional.orElse(null) } 方法获取包含的值再缓存。
 * <p>
 * 4. 另：{@code Optional } 或 {@code CompletableFuture } 包含的值可以为 {@code null}，
 * 但自身不建议为 {@code null}，否则方法返回值会时有时无：<br>
 * 命中缓存时不为 {@code null}，
 * 未命中缓存时为 {@code null}。
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-12
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface Cacheable {

    /**
     * SpEL表达式，用于从参数中提取键。
     * <p>
     * 如果未配置，采用被注解方法的第一个参数作为键。
     */
    String key() default "";

    /**
     * SpEL表达式，用于判断是否执行缓存操作。
     * <p>
     * 如果未配置，condition 表达式结果默认为 true。
     * <p>
     * 调用被注解方法前解析此表达式，如 condition 表达式结果为 false，不执行缓存操作。
     */
    String condition() default "";

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