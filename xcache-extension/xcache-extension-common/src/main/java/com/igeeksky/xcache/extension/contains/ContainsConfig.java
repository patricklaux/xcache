package com.igeeksky.xcache.extension.contains;

/**
 * ContainsPredicate 配置
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/8
 */
public class ContainsConfig<K> {

    private final String name;

    private final String application;

    private final String provider;

    private final Class<K> keyType;

    private final Class<?>[] keyParams;

    private ContainsConfig(Builder<K> builder) {
        this.name = builder.name;
        this.application = builder.application;
        this.provider = builder.provider;
        this.keyType = builder.keyType;
        this.keyParams = builder.keyParams;
    }

    public String getName() {
        return name;
    }

    public String getApplication() {
        return application;
    }

    public String getProvider() {
        return provider;
    }

    public Class<K> getKeyType() {
        return keyType;
    }

    public Class<?>[] getKeyParams() {
        return keyParams;
    }

    public static <K> Builder<K> builder(Class<K> keyType, Class<?>[] keyParams) {
        return new Builder<>(keyType, keyParams);
    }

    public static class Builder<K> {

        private String name;
        private String application;
        private String provider;
        private final Class<K> keyType;
        private final Class<?>[] keyParams;

        private Builder(Class<K> keyType, Class<?>[] keyParams) {
            this.keyType = keyType;
            this.keyParams = keyParams;
        }

        public Builder<K> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<K> application(String application) {
            this.application = application;
            return this;
        }

        public Builder<K> provider(String provider) {
            this.provider = provider;
            return this;
        }

        public ContainsConfig<K> build() {
            return new ContainsConfig<>(this);
        }

    }

}