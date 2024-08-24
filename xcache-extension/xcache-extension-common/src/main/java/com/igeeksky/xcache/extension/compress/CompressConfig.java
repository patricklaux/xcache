package com.igeeksky.xcache.extension.compress;

import java.util.HashMap;
import java.util.Map;

/**
 * 压缩配置
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/8
 */
public class CompressConfig {

    private final int level;

    private final boolean nowrap;

    private final String provider;

    private final Map<String, Object> params;

    private CompressConfig(Builder builder) {
        this.level = builder.level;
        this.nowrap = builder.nowrap;
        this.provider = builder.provider;
        this.params = builder.params;
    }

    public int getLevel() {
        return level;
    }

    public boolean isNowrap() {
        return nowrap;
    }

    public String getProvider() {
        return provider;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int level = -1;
        private boolean nowrap = false;
        private String provider;
        private final Map<String, Object> params = new HashMap<>();

        private Builder() {
        }

        public Builder level(int level) {
            this.level = level;
            return this;
        }

        public Builder nowrap(boolean nowrap) {
            this.nowrap = nowrap;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder params(Map<String, Object> params) {
            if (params != null) {
                this.params.putAll(params);
            }
            return this;
        }

        public CompressConfig build() {
            return new CompressConfig(this);
        }

    }

}