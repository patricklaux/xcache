package com.igeeksky.xcache.annotation.operation;

import com.igeeksky.xtool.core.lang.StringUtils;

/**
 * @author patrick
 * @since 0.0.4 2024/3/3
 */
public class CacheableAllOperation extends CacheOperation {

    private final String keys;

    private final String condition;

    private final String unless;

    protected CacheableAllOperation(Builder builder) {
        super(builder);
        this.keys = builder.keys;
        this.condition = builder.condition;
        this.unless = builder.unless;
    }

    public String getKeys() {
        return keys;
    }

    public String getCondition() {
        return condition;
    }

    public String getUnless() {
        return unless;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends CacheOperation.Builder {

        private String keys;

        private String condition;

        private String unless;

        public Builder keys(String keys) {
            this.keys = StringUtils.trim(keys);
            return this;
        }

        public Builder condition(String condition) {
            this.condition = condition;
            return this;
        }

        public Builder unless(String unless) {
            this.unless = unless;
            return this;
        }

        public CacheableAllOperation build() {
            return new CacheableAllOperation(this);
        }
    }

}