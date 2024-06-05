package com.igeeksky.xcache.config;

import com.igeeksky.xcache.common.ReferenceType;
import com.igeeksky.xcache.extension.compress.Compressor;
import com.igeeksky.xcache.extension.serializer.Serializer;
import com.igeeksky.xtool.core.json.SimpleJSON;

/**
 * 本地缓存配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
public class LocalConfig<V> {

    private String storeName;
    private int initialCapacity;
    private long maximumSize;
    private long maximumWeight;
    private ReferenceType keyStrength;
    private ReferenceType valueStrength;
    private long expireAfterWrite;
    private long expireAfterAccess;
    private boolean enableRandomTtl;
    private boolean enableNullValue;
    private boolean enableCompressValue;
    private boolean enableSerializeValue;
    private Compressor valueCompressor;
    private Serializer<V> valueSerializer;

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public int getInitialCapacity() {
        return initialCapacity;
    }

    public void setInitialCapacity(int initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    public long getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(long maximumSize) {
        this.maximumSize = maximumSize;
    }

    public long getMaximumWeight() {
        return maximumWeight;
    }

    public void setMaximumWeight(long maximumWeight) {
        this.maximumWeight = maximumWeight;
    }

    public ReferenceType getKeyStrength() {
        return keyStrength;
    }

    public void setKeyStrength(ReferenceType keyStrength) {
        this.keyStrength = keyStrength;
    }

    public ReferenceType getValueStrength() {
        return valueStrength;
    }

    public void setValueStrength(ReferenceType valueStrength) {
        this.valueStrength = valueStrength;
    }

    public long getExpireAfterWrite() {
        return expireAfterWrite;
    }

    public void setExpireAfterWrite(long expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }

    public long getExpireAfterAccess() {
        return expireAfterAccess;
    }

    public void setExpireAfterAccess(long expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
    }

    public boolean isEnableNullValue() {
        return enableNullValue;
    }

    public void setEnableNullValue(boolean enableNullValue) {
        this.enableNullValue = enableNullValue;
    }

    public boolean isEnableRandomTtl() {
        return enableRandomTtl;
    }

    public void setEnableRandomTtl(boolean enableRandomTtl) {
        this.enableRandomTtl = enableRandomTtl;
    }

    public boolean isEnableCompressValue() {
        return enableCompressValue;
    }

    public void setEnableCompressValue(boolean enableCompressValue) {
        this.enableCompressValue = enableCompressValue;
    }

    public boolean isEnableSerializeValue() {
        return enableSerializeValue;
    }

    public void setEnableSerializeValue(boolean enableSerializeValue) {
        this.enableSerializeValue = enableSerializeValue;
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
