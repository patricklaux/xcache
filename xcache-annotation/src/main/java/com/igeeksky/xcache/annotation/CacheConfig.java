package com.igeeksky.xcache.annotation;

import java.lang.annotation.*;

/**
 * 缓存公共配置
 * <p>
 * 如果一个类的多个方法使用同一缓存，可用此注解配置公共属性，方法级注解的所有同名属性保持默认即可。
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-12
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheConfig {

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