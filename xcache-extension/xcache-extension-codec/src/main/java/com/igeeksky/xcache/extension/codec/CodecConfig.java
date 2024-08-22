package com.igeeksky.xcache.extension.codec;

import java.nio.charset.Charset;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/7
 */
public class CodecConfig<T> {

    /**
     * 缓存名称
     */
    private final String name;

    /**
     * CodecProvider - id
     */
    private final String provider;

    /**
     * 字符集
     */
    private final Charset charset;

    /**
     * 数据类型
     */
    private final Class<T> type;

    /**
     * 泛型参数
     */
    private final Class<?>[] params;

    private CodecConfig(Builder<T> builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.params = builder.params;
        this.charset = builder.charset;
        this.provider = builder.provider;
    }

    public String getName() {
        return name;
    }

    public String getProvider() {
        return provider;
    }

    public Charset getCharset() {
        return charset;
    }

    public Class<T> getType() {
        return type;
    }

    public Class<?>[] getParams() {
        return params;
    }

    public static <T> Builder<T> builder(Class<T> type) {
        return new Builder<>(type);
    }

    public static <T> Builder<T> builder(Class<T> type, Class<?>[] params) {
        return new Builder<>(type, params);
    }

    public static class Builder<T> {

        private String name;
        private String provider;
        private Charset charset;
        private final Class<T> type;
        private Class<?>[] params;

        private Builder(Class<T> type) {
            this.type = type;
        }

        private Builder(Class<T> type, Class<?>[] params) {
            this.type = type;
            this.params = params;
        }

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder<T> charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public CodecConfig<T> build() {
            return new CodecConfig<>(this);
        }

    }

}