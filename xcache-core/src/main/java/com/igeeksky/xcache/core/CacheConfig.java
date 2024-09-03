package com.igeeksky.xcache.core;

import com.igeeksky.xtool.core.lang.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 缓存配置类
 *
 * @param <K> 缓存键类型
 * @param <V> 缓存值类型
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-09
 */
public class CacheConfig<K, V> {

    private final String name;

    private final String app;

    private final String sid;

    private final Charset charset;

    private final Class<K> keyType;

    private final Class<?>[] keyParams;

    private final Class<V> valueType;

    private final Class<?>[] valueParams;

    public CacheConfig(Builder<K, V> builder) {
        this.name = builder.name;
        this.app = builder.app;
        this.sid = builder.sid;
        this.charset = builder.charset;
        this.keyType = builder.keyType;
        this.keyParams = builder.keyParams;
        this.valueType = builder.valueType;
        this.valueParams = builder.valueParams;
    }

    public String getName() {
        return name;
    }

    public String getApp() {
        return app;
    }

    public String getSid() {
        return sid;
    }

    public Charset getCharset() {
        return charset;
    }

    public Class<K> getKeyType() {
        return keyType;
    }

    public Class<?>[] getKeyParams() {
        return keyParams;
    }

    public Class<V> getValueType() {
        return valueType;
    }

    public Class<?>[] getValueParams() {
        return valueParams;
    }

    public static <K, V> Builder<K, V> builder(Class<K> keyType, Class<?>[] keyParams,
                                               Class<V> valueType, Class<?>[] valueParams) {
        return new Builder<>(keyType, keyParams, valueType, valueParams);
    }

    public static class Builder<K, V> {

        private String name;

        private String app;

        private String sid;

        private Charset charset;

        private final Class<K> keyType;

        private final Class<?>[] keyParams;

        private final Class<V> valueType;

        private final Class<?>[] valueParams;

        private Builder(Class<K> keyType, Class<?>[] keyParams, Class<V> valueType, Class<?>[] valueParams) {
            this.keyType = keyType;
            this.keyParams = keyParams;
            this.valueType = valueType;
            this.valueParams = valueParams;
        }

        public Builder<K, V> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<K, V> app(String app) {
            this.app = app;
            return this;
        }

        public Builder<K, V> sid(String sid) {
            this.sid = sid;
            return this;
        }

        public Builder<K, V> charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder<K, V> charset(String charset) {
            String charsetName = StringUtils.toUpperCase(charset);
            if (charsetName != null) {
                this.charset = Charset.forName(charsetName);
            } else {
                this.charset = StandardCharsets.UTF_8;
            }
            return this;
        }

        public CacheConfig<K, V> build() {
            return new CacheConfig<>(this);
        }

    }

}