package com.igeeksky.xcache.annotation.operation;

import com.igeeksky.xtool.core.lang.StringUtils;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-14
 */
public class CacheEvictOperation extends CacheOperation {

    private final String key;

    private final boolean beforeInvocation;

    protected CacheEvictOperation(Builder builder) {
        super(builder);
        this.key = builder.key;
        this.beforeInvocation = builder.beforeInvocation;
    }

    public String getKey() {
        return key;
    }

    public boolean isBeforeInvocation() {
        return beforeInvocation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends CacheOperation.Builder {

        private String key;

        private boolean beforeInvocation;

        public Builder key(String key) {
            this.key = StringUtils.trim(key);
            return this;
        }

        public Builder beforeInvocation(boolean beforeInvocation) {
            this.beforeInvocation = beforeInvocation;
            return this;
        }

        public CacheEvictOperation build() {
            return new CacheEvictOperation(this);
        }
    }

}
