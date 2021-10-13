package com.igeeksky.xcache.util;

import java.util.Arrays;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-05
 */
public abstract class ArraysUtils {

    public static <T> boolean isEmpty(T[] array) {
        return (null == array || array.length == 0);
    }

    public static <T> T getLast(T[] src) {
        return src[src.length - 1];
    }

    @SafeVarargs
    public static <T> T[] merge(T[]... arrays) {
        int total = 0;
        for (T[] array : arrays) {
            total += array.length;
        }

        T[] first = arrays[0];
        T[] dest = Arrays.copyOf(first, total);

        int offset = first.length;

        for (int i = 1; i < arrays.length; i++) {
            T[] src = arrays[i];
            System.arraycopy(src, 0, dest, offset, src.length);
            offset += src.length;
        }

        return dest;
    }

    public static <T> T[] requireNonEmpty(T[] array, String message) {
        if (isEmpty(array))
            throw new NullPointerException(message);
        return array;
    }
}
