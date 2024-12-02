package com.igeeksky.xcache.core.store;

import com.igeeksky.xcache.common.ReferenceType;
import com.igeeksky.xcache.props.RedisType;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.compress.Compressor;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 存储层配置类
 *
 * @param <V> 缓存值类型
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/13
 */
public class StoreConfig<V> {

    private final String name;

    private final String group;

    private final Charset charset;

    private final Class<V> valueType;

    private final String provider;

    // embed
    private final long maximumSize;

    // embed
    private final long maximumWeight;

    // embed
    private final int initialCapacity;

    // embed
    private final ReferenceType keyStrength;

    // embed
    private final ReferenceType valueStrength;

    // embed
    private final long expireAfterAccess;

    // embed & extra
    private final long expireAfterWrite;

    // embed & extra
    private final boolean enableRandomTtl;

    // embed & extra
    private final boolean enableNullValue;

    // Remote
    private final boolean enableGroupPrefix;

    // embed & extra
    private final boolean enableCompressValue;

    // embed
    private final boolean enableSerializeValue;

    // embed & extra
    private final Compressor valueCompressor;

    // embed & extra
    private final Codec<V> valueCodec;

    // extra
    private final RedisType redisType;

    /**
     * 扩展参数0，用于自定义的 StoreProvider
     * <p>
     * 自定义实现 StoreProvider 时，如果以上参数名称均无法满足需求，可以使用 params[0-9]，
     * 然后在 StoreProvider 中获取并使用即可
     */
    private final Map<String, Object> params;

    private StoreConfig(Builder<V> builder) {
        this.name = builder.name;
        this.group = builder.group;
        this.charset = builder.charset;
        this.valueType = builder.valueType;
        this.provider = builder.provider;
        this.initialCapacity = builder.initialCapacity;
        this.maximumSize = builder.maximumSize;
        this.maximumWeight = builder.maximumWeight;
        this.keyStrength = builder.keyStrength;
        this.valueStrength = builder.valueStrength;
        this.expireAfterAccess = builder.expireAfterAccess;
        this.expireAfterWrite = builder.expireAfterWrite;
        this.enableGroupPrefix = builder.enableGroupPrefix;
        this.enableRandomTtl = builder.enableRandomTtl;
        this.enableNullValue = builder.enableNullValue;
        this.enableCompressValue = builder.enableCompressValue;
        this.enableSerializeValue = builder.enableSerializeValue;
        this.valueCompressor = builder.valueCompressor;
        this.valueCodec = builder.valueCodec;
        this.redisType = builder.redisType;
        this.params = builder.params;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public Charset getCharset() {
        return charset;
    }

    public Class<V> getValueType() {
        return valueType;
    }

    public String getProvider() {
        return provider;
    }

    public int getInitialCapacity() {
        return initialCapacity;
    }

    public long getMaximumSize() {
        return maximumSize;
    }

    public long getMaximumWeight() {
        return maximumWeight;
    }

    public ReferenceType getKeyStrength() {
        return keyStrength;
    }

    public ReferenceType getValueStrength() {
        return valueStrength;
    }

    public long getExpireAfterAccess() {
        return expireAfterAccess;
    }

    public long getExpireAfterWrite() {
        return expireAfterWrite;
    }

    public boolean isEnableGroupPrefix() {
        return enableGroupPrefix;
    }

    public boolean isEnableRandomTtl() {
        return enableRandomTtl;
    }

    public boolean isEnableNullValue() {
        return enableNullValue;
    }

    public boolean isEnableCompressValue() {
        return enableCompressValue;
    }

    public boolean isEnableSerializeValue() {
        return enableSerializeValue;
    }

    public Compressor getValueCompressor() {
        return valueCompressor;
    }

    public Codec<V> getValueCodec() {
        return valueCodec;
    }

    public RedisType getRedisType() {
        return redisType;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public static <V> Builder<V> builder(Class<V> valueType) {
        return new Builder<>(valueType);
    }

    public static class Builder<V> {

        private String name;

        private String group;

        private Charset charset;

        private final Class<V> valueType;

        private String provider;

        private int initialCapacity;

        private long maximumSize;

        private long maximumWeight;

        private ReferenceType keyStrength;

        private ReferenceType valueStrength;

        private long expireAfterAccess;

        private long expireAfterWrite;

        private boolean enableRandomTtl;

        private boolean enableNullValue;

        private boolean enableGroupPrefix;

        private boolean enableCompressValue;

        private boolean enableSerializeValue;

        private Compressor valueCompressor;

        private Codec<V> valueCodec;

        private RedisType redisType;

        private final Map<String, Object> params = new HashMap<>();

        private Builder(Class<V> valueType) {
            this.valueType = valueType;
        }

        public Builder<V> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<V> group(String group) {
            this.group = group;
            return this;
        }

        public Builder<V> charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder<V> provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder<V> initialCapacity(int initialCapacity) {
            this.initialCapacity = initialCapacity;
            return this;
        }

        public Builder<V> maximumSize(long maximumSize) {
            this.maximumSize = maximumSize;
            return this;
        }

        public Builder<V> maximumWeight(long maximumWeight) {
            this.maximumWeight = maximumWeight;
            return this;
        }

        public Builder<V> keyStrength(ReferenceType keyStrength) {
            this.keyStrength = keyStrength;
            return this;
        }

        public Builder<V> valueStrength(ReferenceType valueStrength) {
            this.valueStrength = valueStrength;
            return this;
        }

        public Builder<V> expireAfterAccess(long expireAfterAccess) {
            this.expireAfterAccess = expireAfterAccess;
            return this;
        }

        public Builder<V> expireAfterWrite(long expireAfterWrite) {
            this.expireAfterWrite = expireAfterWrite;
            return this;
        }

        public Builder<V> enableRandomTtl(boolean enableRandomTtl) {
            this.enableRandomTtl = enableRandomTtl;
            return this;
        }

        public Builder<V> enableNullValue(boolean enableNullValue) {
            this.enableNullValue = enableNullValue;
            return this;
        }

        public Builder<V> enableGroupPrefix(boolean enableGroupPrefix) {
            this.enableGroupPrefix = enableGroupPrefix;
            return this;
        }

        public Builder<V> valueCodec(Codec<V> valueCodec) {
            this.valueCodec = valueCodec;
            this.enableSerializeValue = (valueCodec != null);
            return this;
        }

        public Builder<V> valueCompressor(Compressor valueCompressor) {
            this.valueCompressor = valueCompressor;
            this.enableCompressValue = (valueCompressor != null);
            return this;
        }

        public Builder<V> redisType(RedisType redisType) {
            this.redisType = redisType;
            return this;
        }

        public Builder<V> params(Map<String, Object> params) {
            if (params != null) {
                this.params.putAll(params);
            }
            return this;
        }

        public StoreConfig<V> build() {
            return new StoreConfig<>(this);
        }
    }
}
