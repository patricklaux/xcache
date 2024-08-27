package com.igeeksky.xcache.annotation;

import org.springframework.aot.hint.annotation.Reflective;

import java.lang.annotation.*;

/**
 * 缓存注解
 * <p>
 * 批量更新特定的缓存元素。
 * <p>
 * 如果同一类中有多个方法使用同一缓存，则可以使用 {@link CacheConfig} 在类注解中配置
 * name, keyType, keyParams, valueType, valueParams，此注解的这五个属性保持默认即可。
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
     * SpEL表达式，用于从参数中提取 Map < Key, value >
     */
    String keyValues() default "";

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
