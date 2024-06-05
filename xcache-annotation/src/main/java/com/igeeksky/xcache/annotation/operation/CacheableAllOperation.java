package com.igeeksky.xcache.annotation.operation;

import com.igeeksky.xtool.core.lang.StringUtils;

/**
 * @author patrick
 * @since 0.0.4 2024/3/3
 */
public class CacheableAllOperation extends CacheOperation {

    private final String keys;

    protected CacheableAllOperation(Builder builder) {
        super(builder);
        this.keys = builder.keys;
    }

    public String getKeys() {
        return keys;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends CacheOperation.Builder {

        private String keys;

        public Builder keys(String keys) {
            this.keys = StringUtils.trim(keys);
            return this;
        }

        public CacheableAllOperation build() {
            return new CacheableAllOperation(this);
        }
    }

}
