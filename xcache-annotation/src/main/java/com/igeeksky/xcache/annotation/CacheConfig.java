package com.igeeksky.xcache.annotation;

import org.springframework.aot.hint.annotation.Reflective;

import java.lang.annotation.*;

/**
 * 缓存公共配置
 * <p>
 * 同一个类中，大多数情况下不同方法会使用同一个缓存，
 * 为了避免在多个方法级注解中重复配置相同参数，可以采用此注解。
 * <p>
 * 如果方法注解使用的缓存与此注解配置的相同，则方法级注解的所有同名属性保持默认即可，无需再配置。
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-12
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface CacheConfig {

    /**
     * 缓存名称
     */
    String name();

    /**
     * 键的类型
     */
    Class<?> keyType();

    /**
     * 键的泛型参数类型
     */
    Class<?>[] keyParams() default {};

    /**
     * 值的类型
     */
    Class<?> valueType();

    /**
     * 值的泛型的类型
     */
    Class<?>[] valueParams() default {};

}