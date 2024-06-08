package com.igeeksky.xcache.props;

import com.igeeksky.xtool.core.json.SimpleJSON;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-20
 */
public class RemoteProps implements Cloneable {

    /**
     * 默认值：lettuceCacheStoreProvider
     *
     * @see com.igeeksky.xcache.props.CacheConstants#DEFAULT_REMOTE_CACHE_STORE
     */
    private String cacheStore;

    /**
     * 默认值：redis-string
     *
     * @see com.igeeksky.xcache.props.CacheConstants#DEFAULT_REMOTE_STORE_NAME
     */
    private String storeName;


    private Long expireAfterWrite;


    private String valueSerializer;


    private String valueCompressor;


    private Boolean enableKeyPrefix;


    private Boolean enableRandomTtl;


    private Boolean enableNullValue;

    public String getCacheStore() {
        return cacheStore;
    }

    public void setCacheStore(String cacheStore) {
        this.cacheStore = cacheStore;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public Long getExpireAfterWrite() {
        return expireAfterWrite;
    }

    public void setExpireAfterWrite(Long expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }

    public String getValueSerializer() {
        return valueSerializer;
    }

    public void setValueSerializer(String valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    public String getValueCompressor() {
        return valueCompressor;
    }

    public void setValueCompressor(String valueCompressor) {
        this.valueCompressor = valueCompressor;
    }

    public Boolean getEnableKeyPrefix() {
        return enableKeyPrefix;
    }

    public void setEnableKeyPrefix(Boolean enableKeyPrefix) {
        this.enableKeyPrefix = enableKeyPrefix;
    }

    public Boolean getEnableRandomTtl() {
        return enableRandomTtl;
    }

    public void setEnableRandomTtl(Boolean enableRandomTtl) {
        this.enableRandomTtl = enableRandomTtl;
    }

    public Boolean getEnableNullValue() {
        return enableNullValue;
    }

    public void setEnableNullValue(Boolean enableNullValue) {
        this.enableNullValue = enableNullValue;
    }

    @Override
    public RemoteProps clone() throws CloneNotSupportedException {
        return (RemoteProps) super.clone();
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }
}
