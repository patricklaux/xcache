package com.igeeksky.xcache.annotation.operation;

import com.igeeksky.xcache.annotation.CacheableAll;
import com.igeeksky.xtool.core.lang.StringUtils;

/**
 * 记录 {@link CacheableAll} 注解信息
 *
 * @author patrick
 * @since 0.0.4 2024/3/3
 */
public class CacheableAllOperation extends CacheOperation {

    private final String keys;

    private final String condition;

    protected CacheableAllOperation(Builder builder) {
        super(builder);
        this.keys = builder.keys;
        this.condition = builder.condition;
    }

    public String getKeys() {
        return keys;
    }

    public String getCondition() {
        return condition;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends CacheOperation.Builder {

        private String keys;

        private String condition;

        public Builder keys(String keys) {
            this.keys = StringUtils.trim(keys);
            return this;
        }

        public Builder condition(String condition) {
            this.condition = condition;
            return this;
        }

        public CacheableAllOperation build() {
            return new CacheableAllOperation(this);
        }
    }

}