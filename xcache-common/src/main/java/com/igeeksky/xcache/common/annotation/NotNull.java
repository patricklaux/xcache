package com.igeeksky.xcache.common.annotation;

import java.lang.annotation.*;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-10-03
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
public @interface NotNull {
}
