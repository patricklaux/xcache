package com.igeeksky.xcache.annotation;

import org.springframework.aot.hint.annotation.Reflective;

import java.lang.annotation.*;

/**
 * 缓存注解
 * <p>
 * 如果数据未缓存，则反射执行方法并缓存；
 * 如果数据已缓存，则直接返回已缓存数据。
 * <p>
 * {@link Cacheable} 仅缓存单个元素，{@link CacheableAll} 支持批量缓存多个元素，
 * 对应 cache.getAll(keySet) 和 cache.putAll(kvMap) 方法。
 * <p>
 * 如果一个类中使用多个缓存注解，name, keyType, keyParams, valueType, valueParams
 * 这五个公共属性可用类注解 {@link CacheConfig} 配置，此注解保持默认即可。
 * <p>
 * <b>注意</b>：请勿与其它缓存注解共用于同一方法！<p>
 * 因为当成功获取缓存数据时，此注解的目标方法不会被调用。
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface CacheableAll {

    /**
     * SpEL表达式，用于从参数中提取键集。
     * <p>
     * 如果未配置，使用被注解方法的第一个参数。
     * <p>
     * 注意：执行此表达式提取出来的键集必须为 set 类型
     */
    String keys() default "";

    /**
     * SpEL表达式
     * <p>
     * 如果未配置，condition 表达式结果默认为 true。
     * <p>
     * 如果 condition 表达式结果为 true，调用被注解方法前执行缓存操作 (getAll)，<p>
     * 1. 缓存中有全部值：不再调用被注解方法，直接返回缓存的值；<p>
     * 2. 缓存中无值或仅有部分值：调用被注解方法，然后判断 unless 表达式是否为 false：<p>
     * 2.1. 如果 unless 表达式结果为 false，缓存被注解方法执行结果；如果 unless 表达式为 true，不缓存。<p>
     * 2.2. 返回：被注解方法执行结果 + 缓存结果。
     */
    String condition() default "";

    /**
     * SpEL表达式
     * <p>
     * 如果未配置，unless 表达式结果默认为 false。
     * <p>
     * 当缓存中无值或仅有部分值，如果 condition 表达式结果为 true，且 unless 表达式结果为 false，
     * 调用被注解方法后执行缓存操作 (putAll)
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
