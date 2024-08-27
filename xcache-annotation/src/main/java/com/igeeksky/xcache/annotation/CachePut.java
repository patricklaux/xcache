package com.igeeksky.xcache.annotation;

import org.springframework.aot.hint.annotation.Reflective;

import java.lang.annotation.*;

/**
 * 缓存数据更新注解
 * <p>
 * 添加此注解的方法，将更新指定的单个缓存元素。
 * <p>
 * 如果同一类中有多个方法使用同一缓存，则可以使用 {@link CacheConfig} 在类注解中配置
 * name, keyType, keyParams, valueType, valueParams，此注解的这五个属性保持默认即可。
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
     * SpEL表达式，用于从参数中提取待缓存的键。
     * <p>
     * 如果未配置，采用被注解方法的第一个参数作为键。
     */
    String key() default "";

    /**
     * SpEL表达式，用于从参数中提取待缓存的值。
     */
    String value() default "";

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
