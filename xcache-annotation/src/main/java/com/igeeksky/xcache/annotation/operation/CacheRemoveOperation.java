package com.igeeksky.xcache.annotation.operation;

import com.igeeksky.xcache.annotation.CacheRemove;
import com.igeeksky.xtool.core.lang.StringUtils;

/**
 * 记录 {@link CacheRemove} 注解信息
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-14
 */
public class CacheRemoveOperation extends CacheOperation {

    private final String key;

    private final boolean beforeInvocation;

    private final String condition;

    private final String unless;

    protected CacheRemoveOperation(Builder builder) {
        super(builder);
        this.key = builder.key;
        this.beforeInvocation = builder.beforeInvocation;
        this.condition = builder.condition;
        this.unless = builder.unless;
    }

    public String getKey() {
        return key;
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

        private String key;

        private boolean beforeInvocation;

        private String condition;

        private String unless;

        public Builder key(String key) {
            this.key = StringUtils.trim(key);
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

        public CacheRemoveOperation build() {
            return new CacheRemoveOperation(this);
        }
    }

}
