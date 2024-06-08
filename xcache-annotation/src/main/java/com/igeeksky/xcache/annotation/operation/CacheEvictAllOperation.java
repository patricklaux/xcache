package com.igeeksky.xcache.annotation.operation;

import com.igeeksky.xtool.core.lang.StringUtils;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-14
 */
public class CacheEvictAllOperation extends CacheOperation {

    private final String keys;

    private final boolean beforeInvocation;

    protected CacheEvictAllOperation(Builder builder) {
        super(builder);
        this.keys = builder.keys;
        this.beforeInvocation = builder.beforeInvocation;
    }

    public String getKeys() {
        return keys;
    }

    public boolean isBeforeInvocation() {
        return beforeInvocation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends CacheOperation.Builder {

        private String keys;

        private boolean beforeInvocation;

        public Builder keys(String keys) {
            this.keys = StringUtils.trim(keys);
            return this;
        }

        public Builder beforeInvocation(boolean beforeInvocation) {
            this.beforeInvocation = beforeInvocation;
            return this;
        }

        public CacheEvictAllOperation build() {
            return new CacheEvictAllOperation(this);
        }
    }

}
