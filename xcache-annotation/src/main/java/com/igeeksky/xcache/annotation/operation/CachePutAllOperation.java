package com.igeeksky.xcache.annotation.operation;

import com.igeeksky.xtool.core.lang.StringUtils;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-14
 */
public class CachePutAllOperation extends CacheOperation {

    private final String keyValues;

    private final String condition;

    private final String unless;

    protected CachePutAllOperation(Builder builder) {
        super(builder);
        this.keyValues = builder.keyValues;
        this.condition = builder.condition;
        this.unless = builder.unless;
    }

    public String getKeyValues() {
        return keyValues;
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

        private String keyValues;

        private String condition;

        private String unless;

        public Builder keyValues(String keyValues) {
            this.keyValues = StringUtils.trim(keyValues);
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

        public CachePutAllOperation build() {
            return new CachePutAllOperation(this);
        }
    }

}
