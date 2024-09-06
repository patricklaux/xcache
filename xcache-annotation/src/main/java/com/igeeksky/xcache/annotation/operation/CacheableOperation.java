package com.igeeksky.xcache.annotation.operation;

import com.igeeksky.xcache.annotation.Cacheable;
import com.igeeksky.xtool.core.lang.StringUtils;

/**
 * 记录 {@link Cacheable} 注解信息
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-14
 */
public class CacheableOperation extends CacheOperation {

    private final String key;

    private final String condition;

    protected CacheableOperation(Builder builder) {
        super(builder);
        this.key = builder.key;
        this.condition = builder.condition;
    }

    public String getKey() {
        return key;
    }

    public String getCondition() {
        return condition;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends CacheOperation.Builder {

        private String key;

        private String condition;

        public Builder key(String key) {
            this.key = StringUtils.trimToNull(key);
            return this;
        }

        public Builder condition(String condition) {
            this.condition = StringUtils.trimToNull(condition);
            return this;
        }

        public CacheableOperation build() {
            return new CacheableOperation(this);
        }

    }

}