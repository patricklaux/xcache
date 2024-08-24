package com.igeeksky.xcache.extension.jackson;

import java.util.Arrays;

public final class JavaTypeInfo {
    private final Class<?> type;
    private final Class<?>[] params;

    private boolean hashIsZero;
    private int hash;

    public JavaTypeInfo(Class<?> type, Class<?>[] params) {
        this.type = type;
        this.params = params;
    }

    public Class<?> type() {
        return type;
    }

    public Class<?>[] params() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JavaTypeInfo that)) return false;

        return type().equals(that.type()) && Arrays.equals(params(), that.params());
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && !hashIsZero) {
            h = type().hashCode();
            h = 31 * h + Arrays.hashCode(params());
            if (h == 0) {
                hashIsZero = true;
            } else {
                hash = h;
            }
        }
        return h;
    }

    @Override
    public String toString() {
        return "JavaTypeInfo[" +
                "type=" + type + ", " +
                "params=" + Arrays.toString(params) + ']';
    }

}