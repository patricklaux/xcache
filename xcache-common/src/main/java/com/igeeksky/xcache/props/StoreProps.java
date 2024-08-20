package com.igeeksky.xcache.props;

import com.igeeksky.xcache.core.ReferenceType;
import com.igeeksky.xcache.core.StoreType;
import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存存储配置
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/11
 */
public class StoreProps {

    /**
     * <b>缓存类型</b>
     * <p>
     * 根据缓存类型，选用不同的默认值
     * <p>
     * 可选值：EMBED 或 EXTRA
     * <p>
     * EMBED：内嵌缓存，如 caffeine； EXTRA：外部缓存，如 redis
     *
     * @see StoreType
     */
    private StoreType storeType;

    /**
     * <b>缓存工厂 id </b>
     * <p>
     * 内嵌缓存默认值：caffeineCacheStoreProvider
     * <p>
     * {@link CacheConstants#DEFAULT_EMBED_STORE_PROVIDER}
     * <p>
     * 外部缓存默认值：lettuceCacheStoreProvider
     * <p>
     * {@link CacheConstants#DEFAULT_EXTRA_STORE_PROVIDER}
     */
    private String provider;

    /**
     * 初始容量
     * <p>
     * 默认值：8192 <p>
     * 建议与 maximum-size 保持一致，避免扩容。
     * <p>
     * {@link CacheConstants#DEFAULT_EMBED_INITIAL_CAPACITY}
     * <p>
     * 用于内嵌缓存，支持此配置项的缓存列表：caffeine
     */
    private Integer initialCapacity;

    /**
     * 最大容量
     * <p>
     * 默认值：8192
     * <p>
     * {@link  CacheConstants#DEFAULT_EMBED_MAXIMUM_SIZE}
     * <p>
     * 用于内嵌缓存，支持此配置项的缓存列表：caffeine
     */
    private Long maximumSize;

    /**
     * 最大权重
     * <p>
     * 默认值：0
     * <p>
     * {@link  CacheConstants#DEFAULT_EMBED_MAXIMUM_WEIGHT}
     * <p>
     * 用于内嵌缓存，支持此配置项的缓存列表：caffeine
     */
    private Long maximumWeight;

    /**
     * 内嵌缓存默认值：3600000 <p>
     * 外部缓存默认值：7200000
     * <p>
     * 单位：毫秒
     * <p>
     * {@link  CacheConstants#DEFAULT_EMBED_EXPIRE_AFTER_WRITE} <p>
     * {@link  CacheConstants#DEFAULT_EXTRA_EXPIRE_AFTER_WRITE}
     */
    private Long expireAfterWrite;

    /**
     * 内嵌缓存默认值：300000 <p>
     * 外部缓存无作用
     * <p>
     * 单位：毫秒
     * <p>
     * {@link  CacheConstants#DEFAULT_EMBED_EXPIRE_AFTER_ACCESS}
     */
    private Long expireAfterAccess;

    /**
     * 键的引用类型
     * <p>
     * 部分内嵌缓存可以根据键的引用类型来执行驱逐策略。
     * 默认值：STRONG
     * <p>
     * Caffeine 可选值：WEAK(弱引用), STRONG(强引用)
     * <p>
     * {@link  CacheConstants#DEFAULT_EMBED_KEY_STRENGTH}
     *
     * @see ReferenceType
     */
    private ReferenceType keyStrength;

    /**
     * 值的引用类型
     * <p>
     * 部分内嵌缓存可以根据值的引用类型来执行驱逐策略。
     * 默认值：STRONG
     * <p>
     * Caffeine 可选值：WEAK(弱引用), SOFT(软引用), STRONG(强引用)
     * <p>
     * {@link CacheConstants#DEFAULT_EMBED_VALUE_STRENGTH}
     *
     * @see ReferenceType
     */
    private ReferenceType valueStrength;

    /**
     * CodecProvider-Id
     * <p>
     * 用于值的序列化操作
     * <p>
     * 内嵌缓存默认值：none <p>
     * 外部缓存默认值：jackson
     * <p>
     * {@link CacheConstants#DEFAULT_EMBED_VALUE_CODEC}
     * {@link CacheConstants#DEFAULT_EXTRA_VALUE_CODEC}
     */
    private String valueCodec;

    /**
     * CompressorProvider-Id
     * <p>
     * 用于值序列化后再压缩（节省空间），默认不压缩
     * <p>
     * 内嵌缓存默认值：NONE <p>
     * 外部缓存默认值：NONE
     * <p>
     * {@link CacheConstants#DEFAULT_EMBED_VALUE_COMPRESSOR}
     * {@link CacheConstants#DEFAULT_VALUE_COMPRESSOR}
     */
    private CompressProps valueCompressor = new CompressProps();

    /**
     * 是否使用随机存活时间
     * <p>
     * Random.nextLong(expireAfterWrite*0.8, expireAfterWrite) <p>
     * 如果此选项设置为 true，且 expireAfterWrite &gt; 0，则会使用随机时间：
     * 最小存活时间为 expireAfterWrite * 0.8，最大存活时间为 expireAfterWrite。
     * <p>
     * 使用随机存活时间的目的是为了避免大规模的 key 同时过期，需要回源查询，造成数据源压力过大。
     * <p>
     * 内嵌缓存默认值：true <p>
     * 外部缓存默认值：true
     * <p>
     * {@link CacheConstants#DEFAULT_EMBED_ENABLE_RANDOM_TTL}
     * {@link CacheConstants#DEFAULT_EXTRA_ENABLE_RANDOM_TTL}
     */
    private Boolean enableRandomTtl;

    /**
     * 是否允许保存空值
     * <p>
     * 如果此选项设置为 true，当 key 对应的 value 为空，也会将 key 保存到缓存中。
     * <p>
     * 如果缓存中存储的是空值，通常表示数据源无有效数据，因此可以避免无效的回源查询。<p>
     * 例：get(key) 方法的返回值为 cacheValue，可通过如下类似代码来判断是否需要回源。
     * <pre>{@code
     * if (cacheValue == null) {
     *     缓存中未存储值（需要回源）;
     * } else {
     *     if (cacheValue.hasValue) {
     *         缓存中存储的是有效值（无需回源）;
     *     } else {
     *         缓存中存储的是空值（无需回源）;
     *     }
     * }
     * }</pre>
     * 内嵌缓存默认值：true <p>
     * 外部缓存默认值：true
     * <p>
     * {@link CacheConstants#DEFAULT_EMBED_ENABLE_NULL_VALUE}
     * {@link CacheConstants#DEFAULT_EXTRA_ENABLE_NULL_VALUE}
     */
    private Boolean enableNullValue;

    /**
     * key 是否使用前缀
     * <p>
     * 如果此选项设置为 true，完整的键为：cacheName + ":" + key。<p>
     * 对于外部缓存，譬如 Redis，通常是多个应用共享，或者一个应用中也会有多种类型的数据，极有可能出现键冲突。<p>
     * 为了避免冲突，因此需要加上前缀进行区分。<p>
     * 如果确定不会出现冲突，则可以设置为 false。<p>
     * 对于内部缓存，譬如 caffeine，每一个 cacheName 对应一个 caffeine 实例，因此不会出现冲突。
     * <p>
     * 内嵌缓存无需配置 <p>
     * 外部缓存默认值：true
     * <p>
     * {@link CacheConstants#DEFAULT_EXTRA_ENABLE_KEY_PREFIX}
     */
    private Boolean enableKeyPrefix;

    /**
     * Redis 命令类型
     * <p>
     * 可选值：STRING 或 HASH
     * <p>
     * 默认值：STRING
     * <p>
     * {@link CacheConstants#DEFAULT_EXTRA_REDIS_TYPE}
     * <p>
     * 用于 Redis 缓存，支持此配置项的缓存列表：lettuce, jedis
     */
    private RedisType redisType;

    /**
     * 自定义扩展属性
     */
    private final Map<String, Object> params = new HashMap<>();

    public StoreType getStoreType() {
        return storeType;
    }

    public void setStoreType(StoreType storeType) {
        this.storeType = storeType;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public RedisType getRedisType() {
        return redisType;
    }

    public void setRedisType(RedisType redisType) {
        this.redisType = redisType;
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

    public String getValueCodec() {
        return valueCodec;
    }

    public void setValueCodec(String valueCodec) {
        this.valueCodec = valueCodec;
    }

    public CompressProps getValueCompressor() {
        return valueCompressor;
    }

    public void setValueCompressor(CompressProps valueCompressor) {
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

    public Boolean getEnableKeyPrefix() {
        return enableKeyPrefix;
    }

    public void setEnableKeyPrefix(Boolean enableKeyPrefix) {
        this.enableKeyPrefix = enableKeyPrefix;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        if (params != null) {
            this.params.putAll(params);
        }
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }
}
