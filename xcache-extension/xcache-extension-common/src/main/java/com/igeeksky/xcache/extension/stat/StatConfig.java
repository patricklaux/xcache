package com.igeeksky.xcache.extension.stat;

/**
 * 统计配置
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/9
 */
public class StatConfig {

    private final String app;

    private final String name;

    private final String provider;

    private StatConfig(Builder builder) {
        this.app = builder.app;
        this.name = builder.name;
        this.provider = builder.provider;
    }

    public String getApp() {
        return app;
    }

    public String getName() {
        return name;
    }

    public String getProvider() {
        return provider;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String app;

        private String name;

        private String provider;

        private Builder() {
        }

        public Builder app(String app) {
            this.app = app;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public StatConfig build() {
            return new StatConfig(this);
        }

    }

}