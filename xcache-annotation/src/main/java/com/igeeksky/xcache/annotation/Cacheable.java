package com.igeeksky.xcache.annotation;

import org.springframework.aot.hint.annotation.Reflective;

import java.lang.annotation.*;

/**
 * 缓存注解
 * <p>
 * 对应 V value = cache.get(K key, CacheLoader loader) 方法 <p>
 * 如果数据未缓存，则反射执行方法并缓存；
 * 如果数据已缓存，则直接返回已缓存数据。
 * <p>
 * 五个公共属性 name, keyType, keyParams, valueType, valueParams
 * 可用类注解 {@link CacheConfig} 配置，此注解保持默认即可。
 * <p>
 * <b>注意</b>：<p>
 * 1. 请勿与其它缓存注解用于同一方法！<br>
 * 因为当成功从缓存获取数据时，目标方法将不会被调用。<p>
 * 2. 如使用 SpEL表达式，项目编译时需使用 {@code -parameters } 记录方法参数名信息，否则无法正确解析。<br>
 * 如使用 maven-compiler-plugin，需配置：{@code <parameters>true</parameters> } <p>
 * 3. 目标方法的返回值除了返回缓存值类型，也可以是 {@code Optional } 类型，如：{@code Optional<User> }，
 * 或是 {@code CompletableFuture } 类型，如：{@code CompletableFuture<User> }。 <br>
 * 缓存实现中会通过 {@code CompletableFuture.get()} 或 {@code Optional.orElse(null) } 方法获取真正的值再缓存。<br>
 * 需要注意的是，如果目标方法返回值是 {@code Optional<V> } ，其包含的值可以为 null，但 Optional 本身不能为 null；
 * {@code CompletableFuture } 亦如是。
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
     * 如果 condition 表达式结果为 true，调用被注解方法前执行缓存操作 (get)，<p>
     * 1. 缓存中有值：不再调用被注解方法，直接返回缓存的值；<p>
     * 2. 缓存中无值：调用被注解方法，然后缓存被注解方法执行结果，返回被注解方法执行结果。
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
     * 键泛型参数
     * <p>
     * 用于较复杂的带泛型参数的键类型的序列化处理
     */
    Class<?>[] keyParams() default {};

    /**
     * 值类型
     */
    Class<?> valueType() default Undefined.class;

    /**
     * 值泛型参数
     * <p>
     * 用于较复杂的带泛型参数的值类型的序列化处理
     */
    Class<?>[] valueParams() default {};

}