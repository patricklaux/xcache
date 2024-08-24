package com.igeeksky.xcache.caffeine;

import com.igeeksky.xcache.common.ReferenceType;
import com.igeeksky.xcache.core.store.StoreConfig;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.compress.Compressor;


/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/13
 */
public class CaffeineConfig<V> {

    private final String name;

    private final int initialCapacity;

    private final long maximumSize;

    private final long maximumWeight;

    private final ReferenceType keyStrength;

    private final ReferenceType valueStrength;

    private final long expireAfterAccess;

    private final long expireAfterWrite;

    private final boolean enableRandomTtl;

    private final boolean enableNullValue;

    private final boolean enableCompressValue;

    private final boolean enableSerializeValue;

    private final Compressor valueCompressor;

    private final Codec<V> valueCodec;

    public CaffeineConfig(StoreConfig<V> storeConfig) {
        this.name = storeConfig.getName();
        this.initialCapacity = storeConfig.getInitialCapacity();
        this.maximumSize = storeConfig.getMaximumSize();
        this.maximumWeight = storeConfig.getMaximumWeight();
        this.keyStrength = storeConfig.getKeyStrength();
        this.valueStrength = storeConfig.getValueStrength();
        this.expireAfterAccess = storeConfig.getExpireAfterAccess();
        this.expireAfterWrite = storeConfig.getExpireAfterWrite();
        this.enableRandomTtl = storeConfig.isEnableRandomTtl();
        this.enableNullValue = storeConfig.isEnableNullValue();
        this.enableCompressValue = storeConfig.isEnableCompressValue();
        this.enableSerializeValue = storeConfig.isEnableSerializeValue();
        this.valueCompressor = storeConfig.getValueCompressor();
        this.valueCodec = storeConfig.getValueCodec();
    }

    public String getName() {
        return name;
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
}
