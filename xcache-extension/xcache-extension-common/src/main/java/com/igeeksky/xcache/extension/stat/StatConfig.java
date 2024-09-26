package com.igeeksky.xcache.extension.stat;

/**
 * 统计配置
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/9
 */
public class StatConfig {

    private final String name;

    private final String group;

    private final String provider;

    private StatConfig(Builder builder) {
        this.name = builder.name;
        this.group = builder.group;
        this.provider = builder.provider;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getProvider() {
        return provider;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;

        private String group;

        private String provider;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder group(String group) {
            this.group = group;
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