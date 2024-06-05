package com.igeeksky.xcache.props;

import com.igeeksky.xcache.common.ReferenceType;
import com.igeeksky.xtool.core.json.SimpleJSON;


/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-20
 */
public class LocalProps implements Cloneable {

    /**
     * 默认值：caffeineCacheStoreProvider
     *
     * @see com.igeeksky.xcache.config.CacheConstants#DEFAULT_LOCAL_CACHE_STORE
     */
    private String cacheStore;

    /**
     * 默认值：caffeine
     *
     * @see com.igeeksky.xcache.config.CacheConstants#DEFAULT_LOCAL_STORE_NAME
     */
    private String storeName;

    /**
     * 默认值：caffeine
     *
     * @see com.igeeksky.xcache.config.CacheConstants#DEFAULT_LOCAL_STORE_NAME
     */
    private Integer initialCapacity;


    private Long maximumSize;


    private Long maximumWeight;


    private Long expireAfterWrite;


    private Long expireAfterAccess;

    /**
     * 键的引用类型
     * <p>
     * 部分本地缓存可以根据键的引用类型来执行不同的驱逐策略。
     * 默认值：STRONG
     * <p>
     * Caffeine 可选值：WEAK（弱引用）, STRONG（强引用）
     *
     * @see ReferenceType
     * @see com.igeeksky.xcache.config.CacheConstants#DEFAULT_LOCAL_KEY_STRENGTH
     */
    private ReferenceType keyStrength;


    private ReferenceType valueStrength;


    private String valueSerializer;


    private String valueCompressor;


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

    public Integer getInitialCapacity() {
        return initialCapacity;
    }

    public void setInitialCapacity(Integer initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    public Long getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(Long maximumSize) {
        this.maximumSize = maximumSize;
    }

    public Long getMaximumWeight() {
        return maximumWeight;
    }

    public void setMaximumWeight(Long maximumWeight) {
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

    public Long getExpireAfterWrite() {
        return expireAfterWrite;
    }

    public void setExpireAfterWrite(Long expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }

    public Long getExpireAfterAccess() {
        return expireAfterAccess;
    }

    public void setExpireAfterAccess(Long expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
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
    public LocalProps clone() throws CloneNotSupportedException {
        return (LocalProps) super.clone();
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }
}
