package com.igeeksky.xcache.annotation.operation;

import com.igeeksky.xtool.core.lang.StringUtils;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-14
 */
public class CachePutAllOperation extends CacheOperation {

    private final String keyValues;

    protected CachePutAllOperation(Builder builder) {
        super(builder);
        this.keyValues = builder.keyValues;
    }

    public String getKeyValues() {
        return keyValues;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends CacheOperation.Builder {

        private String keyValues;

        public Builder keyValues(String keyValues) {
            this.keyValues = StringUtils.trim(keyValues);
            return this;
        }

        public CachePutAllOperation build() {
            return new CachePutAllOperation(this);
        }
    }

}
