package com.igeeksky.xcache.redis.store;

import com.igeeksky.xcache.core.store.StoreConfig;
import com.igeeksky.xcache.props.RedisType;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.compress.Compressor;

import java.nio.charset.Charset;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/13
 */
public class RedisStoreConfig<V> {

    private final String name;

    private final String group;

    private final Charset charset;

    private final RedisType redisType;

    private final long expireAfterWrite;

    private final int keySequenceSize;

    private final boolean enableGroupPrefix;

    private final boolean enableRandomTtl;

    private final boolean enableNullValue;

    private final boolean enableCompressValue;

    private final Codec<V> valueCodec;

    private final Compressor valueCompressor;

    public RedisStoreConfig(StoreConfig<V> storeConfig) {
        this.name = storeConfig.getName();
        this.group = storeConfig.getGroup();
        this.charset = storeConfig.getCharset();
        this.redisType = storeConfig.getRedisType();
        this.expireAfterWrite = storeConfig.getExpireAfterWrite();
        this.enableGroupPrefix = storeConfig.isEnableGroupPrefix();
        this.enableRandomTtl = storeConfig.isEnableRandomTtl();
        this.enableNullValue = storeConfig.isEnableNullValue();
        this.enableCompressValue = storeConfig.isEnableCompressValue();
        this.valueCodec = storeConfig.getValueCodec();
        this.valueCompressor = storeConfig.getValueCompressor();
        this.keySequenceSize = storeConfig.getKeySequenceSize();
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

    public RedisType getRedisType() {
        return redisType;
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

    public Compressor getValueCompressor() {
        return valueCompressor;
    }

    public Codec<V> getValueCodec() {
        return valueCodec;
    }

    public int getKeySequenceSize() {
        return keySequenceSize;
    }

}
