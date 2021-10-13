package com.igeeksky.xcache.util;

import java.util.function.Supplier;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-21
 */
public class Assert {

    public void hasText(String text, Supplier<String> supplier) {
        if (StringUtils.isEmpty(text)) {
            throw new NullPointerException(supplier.get());
        }
    }

}
