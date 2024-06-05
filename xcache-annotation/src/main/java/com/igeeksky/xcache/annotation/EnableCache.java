package com.igeeksky.xcache.annotation;

import com.igeeksky.xcache.aop.CacheConfigSelector;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.lang.annotation.*;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-13
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({CacheConfigSelector.class})
public @interface EnableCache {

    String[] basePackages();

    int order() default Ordered.LOWEST_PRECEDENCE;

    AdviceMode mode() default AdviceMode.PROXY;

}
