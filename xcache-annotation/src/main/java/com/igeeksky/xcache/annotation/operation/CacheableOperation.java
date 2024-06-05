package com.igeeksky.xcache.annotation.operation;

import com.igeeksky.xtool.core.lang.StringUtils;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-14
 */
public class CacheableOperation extends CacheOperation {

    private final String key;

    protected CacheableOperation(Builder builder) {
        super(builder);
        this.key = builder.key;
    }

    public String getKey() {
        return key;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends CacheOperation.Builder {

        private String key;

        public Builder key(String key) {
            this.key = StringUtils.trim(key);
            return this;
        }

        public CacheableOperation build() {
            return new CacheableOperation(this);
        }
    }

}
