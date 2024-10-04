package com.igeeksky.xcache.extension.contains;

/**
 * ContainsPredicate 配置
 *
 * @param <K> 键类型
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/8
 */
public class PredicateConfig<K> {

    private final String name;

    private final String group;

    private final String provider;

    private final Class<K> keyType;

    private final Class<?>[] keyParams;

    private PredicateConfig(Builder<K> builder) {
        this.name = builder.name;
        this.group = builder.group;
        this.provider = builder.provider;
        this.keyType = builder.keyType;
        this.keyParams = builder.keyParams;
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
        private String group;
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

        public Builder<K> group(String group) {
            this.group = group;
            return this;
        }

        public Builder<K> provider(String provider) {
            this.provider = provider;
            return this;
        }

        public PredicateConfig<K> build() {
            return new PredicateConfig<>(this);
        }

    }

}