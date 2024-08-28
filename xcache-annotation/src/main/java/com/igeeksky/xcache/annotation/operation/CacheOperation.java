package com.igeeksky.xcache.annotation.operation;

import com.igeeksky.xcache.annotation.Undefined;
import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * 记录缓存注解公共信息
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-13
 */
public class CacheOperation {

    private final String name;

    private final Class<?> keyType;

    private final Class<?>[] keyParams;

    private final Class<?> valueType;

    private final Class<?>[] valueParams;

    protected CacheOperation(Builder builder) {
        this.name = builder.name;
        this.keyType = builder.keyType;
        this.keyParams = builder.keyParams;
        this.valueType = builder.valueType;
        this.valueParams = builder.valueParams;
    }

    public String getName() {
        return name;
    }

    public Class<?> getKeyType() {
        return keyType;
    }

    public Class<?>[] getKeyParams() {
        return keyParams;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public Class<?>[] getValueParams() {
        return valueParams;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        private Class<?> keyType;

        private Class<?>[] keyParams;

        private Class<?> valueType;

        private Class<?>[] valueParams;

        public Builder name(String name) {
            this.name = StringUtils.trimToNull(name);
            return this;
        }

        public Builder keyType(Class<?> keyType) {
            if (keyType != null && !keyType.isAssignableFrom(Undefined.class)) {
                this.keyType = keyType;
            }
            return this;
        }

        public Builder keyParams(Class<?>[] keyParams) {
            if (keyParams != null && keyParams.length > 0) {
                this.keyParams = keyParams;
            }
            return this;
        }

        public Builder valueType(Class<?> valueType) {
            if (valueType != null && !valueType.isAssignableFrom(Undefined.class)) {
                this.valueType = valueType;
            }
            return this;
        }

        public Builder valueParams(Class<?>[] valueParams) {
            if (valueParams != null && valueParams.length > 0) {
                this.valueParams = valueParams;
            }
            return this;
        }

        public void cacheOperation(CacheOperation operation) {
            if (operation == null) {
                Assert.notNull(name, "name must not be null");
                Assert.notNull(keyType, "keyType must not be null");
                Assert.notNull(valueType, "valueType must not be null");
                return;
            }
            if (this.name != null && !Objects.equals(this.name, operation.name)) {
                Assert.notNull(keyType, "keyType must not be null");
                Assert.notNull(valueType, "valueType must not be null");
                return;
            }
            this.name = operation.name;

            if (this.keyType != null && !Objects.equals(this.keyType, operation.keyType)) {
                throw new IllegalArgumentException("keyType must be keep default or same with CacheConfig.keyType");
            }
            this.keyType = operation.keyType;

            if (this.keyParams != null && !Arrays.equals(this.keyParams, operation.keyParams)) {
                throw new IllegalArgumentException("keyParams must be keep default or same with CacheConfig.keyParams");
            }
            this.keyParams = operation.keyParams;

            if (this.valueType != null && !Objects.equals(this.valueType, operation.valueType)) {
                throw new IllegalArgumentException("valueType must be keep default or same with CacheConfig.valueType");
            }
            this.valueType = operation.valueType;

            if (this.valueParams != null && !Arrays.equals(this.valueParams, operation.valueParams)) {
                throw new IllegalArgumentException("keyParams must be keep default or same with CacheConfig.keyParams");
            }
            this.valueParams = operation.valueParams;
        }

        public CacheOperation build() {
            return new CacheOperation(this);
        }

    }

}