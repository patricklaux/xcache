package com.igeeksky.xcache.core.config;

import com.igeeksky.xcache.extension.compress.Compressor;
import com.igeeksky.xcache.extension.serializer.Serializer;
import com.igeeksky.xtool.core.json.SimpleJSON;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
public class RemoteConfig<V> {

    // Local & Remote
    private String storeName;

    // Local & Remote
    private long expireAfterWrite;

    // Remote String
    private boolean enableKeyPrefix;

    // Local & Remote
    private boolean enableRandomTtl;

    // Local & Remote
    private boolean enableNullValue;

    // Local & Remote
    private boolean enableCompressValue;

    // Local & Remote
    private Compressor valueCompressor;

    // Local & Remote
    private Serializer<V> valueSerializer;

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public long getExpireAfterWrite() {
        return expireAfterWrite;
    }

    public void setExpireAfterWrite(long expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }

    public boolean isEnableKeyPrefix() {
        return enableKeyPrefix;
    }

    public void setEnableKeyPrefix(boolean enableKeyPrefix) {
        this.enableKeyPrefix = enableKeyPrefix;
    }

    public boolean isEnableRandomTtl() {
        return enableRandomTtl;
    }

    public void setEnableRandomTtl(boolean enableRandomTtl) {
        this.enableRandomTtl = enableRandomTtl;
    }

    public boolean isEnableNullValue() {
        return enableNullValue;
    }

    public void setEnableNullValue(boolean enableNullValue) {
        this.enableNullValue = enableNullValue;
    }

    public boolean isEnableCompressValue() {
        return enableCompressValue;
    }

    public void setEnableCompressValue(boolean enableCompressValue) {
        this.enableCompressValue = enableCompressValue;
    }

    public Compressor getValueCompressor() {
        return valueCompressor;
    }

    public void setValueCompressor(Compressor valueCompressor) {
        this.valueCompressor = valueCompressor;
    }

    public Serializer<V> getValueSerializer() {
        return valueSerializer;
    }

    public void setValueSerializer(Serializer<V> valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }
}
