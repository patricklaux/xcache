package com.igeeksky.xcache.annotation.operation;

import com.igeeksky.xcache.annotation.CacheRemoveAll;
import com.igeeksky.xtool.core.lang.StringUtils;

/**
 * 记录 {@link CacheRemoveAll} 注解信息
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-14
 */
public class CacheRemoveAllOperation extends CacheOperation {

    private final String keys;

    private final boolean beforeInvocation;

    private final String condition;

    private final String unless;

    protected CacheRemoveAllOperation(Builder builder) {
        super(builder);
        this.keys = builder.keys;
        this.beforeInvocation = builder.beforeInvocation;
        this.condition = builder.condition;
        this.unless = builder.unless;
    }

    public String getKeys() {
        return keys;
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

        private String keys;

        private boolean beforeInvocation;

        private String condition;

        private String unless;

        public Builder keys(String keys) {
            this.keys = StringUtils.trim(keys);
            return this;
        }

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

        public CacheRemoveAllOperation build() {
            return new CacheRemoveAllOperation(this);
        }
    }

}
