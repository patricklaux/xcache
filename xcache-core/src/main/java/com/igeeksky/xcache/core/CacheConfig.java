package com.igeeksky.xcache.core;

import com.igeeksky.xtool.core.lang.Assert;
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

    /**
     * 应用实例 ID
     */
    private final String sid;

    /**
     * 缓存名称
     */
    private final String name;

    /**
     * 应用名称
     */
    private final String group;

    /**
     * 字符集
     */
    private final Charset charset;

    /**
     * 键类型
     */
    private final Class<K> keyType;

    /**
     * 键泛型参数
     */
    private final Class<?>[] keyParams;

    /**
     * 值类型
     */
    private final Class<V> valueType;

    /**
     * 值泛型参数
     */
    private final Class<?>[] valueParams;

    public CacheConfig(Builder<K, V> builder) {
        this.sid = builder.sid;
        this.name = builder.name;
        this.group = builder.group;
        this.charset = builder.charset;
        this.keyType = builder.keyType;
        this.keyParams = builder.keyParams;
        this.valueType = builder.valueType;
        this.valueParams = builder.valueParams;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
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

        private String sid;

        private String name;

        private String group;

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

        public Builder<K, V> sid(String sid) {
            this.sid = sid;
            return this;
        }

        public Builder<K, V> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<K, V> group(String group) {
            this.group = group;
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
            Assert.notNull(sid, "sid must not be null");
            Assert.notNull(name, "name must not be null");
            Assert.notNull(group, "group must not be null");
            Assert.notNull(charset, "charset must not be null");
            Assert.notNull(keyType, "keyType must not be null");
            Assert.notNull(valueType, "valueType must not be null");
            return new CacheConfig<>(this);
        }

    }

}