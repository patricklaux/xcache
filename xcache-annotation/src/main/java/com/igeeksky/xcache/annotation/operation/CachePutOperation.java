package com.igeeksky.xcache.annotation.operation;

import com.igeeksky.xtool.core.lang.StringUtils;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-14
 */
public class CachePutOperation extends CacheOperation {

    private final String key;

    private final String value;

    protected CachePutOperation(Builder builder) {
        super(builder);
        this.key = builder.key;
        this.value = builder.value;
    }

    public String getKey() {
        return key;
    }


    public String getValue() {
        return value;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends CacheOperation.Builder {

        private String key;

        private String value;

        public Builder key(String key) {
            this.key = StringUtils.trim(key);
            return this;
        }

        public Builder value(String value) {
            this.value = StringUtils.trim(value);
            return this;
        }

        public CachePutOperation build() {
            return new CachePutOperation(this);
        }
    }

}
