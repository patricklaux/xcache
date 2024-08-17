package com.igeeksky.xcache.annotation.operation;

import com.igeeksky.xtool.core.lang.StringUtils;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-14
 */
public class CacheableOperation extends CacheOperation {

    private final String key;

    private final String condition;

    private final String unless;

    protected CacheableOperation(Builder builder) {
        super(builder);
        this.key = builder.key;
        this.condition = builder.condition;
        this.unless = builder.unless;
    }

    public String getKey() {
        return key;
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

        private String key;

        private String condition;

        private String unless;

        public Builder key(String key) {
            this.key = StringUtils.trimToNull(key);
            return this;
        }

        public Builder condition(String condition) {
            this.condition = StringUtils.trimToNull(condition);
            return this;
        }

        public Builder unless(String unless) {
            this.unless = StringUtils.trimToNull(unless);
            return this;
        }

        public CacheableOperation build() {
            return new CacheableOperation(this);
        }

    }

}