package com.igeeksky.xcache.extension.convertor;

import java.util.function.Function;

/**
 * Key convert to String
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-08
 */
public interface KeyConvertor extends Function<Object, String> {

    /**
     * 原对象 转换成 字符串
     *
     * @param original key(对象)
     * @return String
     */
    @Override
    default String apply(Object original) {
        if (original instanceof CharSequence || original instanceof Number || original instanceof Boolean) {
            return original.toString();
        }
        return doApply(original);
    }

    String doApply(Object original);

}
