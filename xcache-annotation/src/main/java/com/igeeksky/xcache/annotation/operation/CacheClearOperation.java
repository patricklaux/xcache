package com.igeeksky.xcache.annotation.operation;

import com.igeeksky.xcache.annotation.CacheClear;

/**
 * 记录 {@link CacheClear} 注解信息
 *
 * @author patrick
 * @since 0.0.4 2024/4/28
 */
public class CacheClearOperation extends CacheOperation {

    private final boolean beforeInvocation;

    private final String condition;

    private final String unless;

    protected CacheClearOperation(Builder builder) {
        super(builder);
        this.beforeInvocation = builder.beforeInvocation;
        this.condition = builder.condition;
        this.unless = builder.unless;
    }

    public boolean isBeforeInvocation() {
        return beforeInvocation;
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

        private boolean beforeInvocation;

        private String condition;

        private String unless;

        public Builder beforeInvocation(boolean beforeInvocation) {
            this.beforeInvocation = beforeInvocation;
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

        public CacheClearOperation build() {
            return new CacheClearOperation(this);
        }
    }
}
