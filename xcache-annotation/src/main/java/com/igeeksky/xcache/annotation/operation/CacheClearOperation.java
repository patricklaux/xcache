package com.igeeksky.xcache.annotation.operation;

/**
 * @author patrick
 * @since 0.0.4 2024/4/28
 */
public class CacheClearOperation extends CacheOperation {

    private final boolean beforeInvocation;

    protected CacheClearOperation(Builder builder) {
        super(builder);
        this.beforeInvocation = builder.beforeInvocation;
    }

    public boolean isBeforeInvocation() {
        return beforeInvocation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends CacheOperation.Builder {

        private boolean beforeInvocation;

        public Builder beforeInvocation(boolean beforeInvocation) {
            this.beforeInvocation = beforeInvocation;
            return this;
        }

        public CacheClearOperation build() {
            return new CacheClearOperation(this);
        }
    }
}
