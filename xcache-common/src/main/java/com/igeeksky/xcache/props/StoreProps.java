package com.igeeksky.xcache.props;

import com.igeeksky.xcache.common.ReferenceType;
import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.HashMap;
import java.util.Map;

/**
 * 存储层配置（用于接收用户配置文件）
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/11
 */
public class StoreProps {

    /**
     * 默认构造函数
     */
    public StoreProps() {
    }

    private String provider;

    private Integer initialCapacity;

    private Long maximumSize;

    private Long maximumWeight;

    private Long expireAfterWrite;

    private Long expireAfterAccess;

    private ReferenceType keyStrength;

    private ReferenceType valueStrength;

    private String valueCodec;

    private CompressProps valueCompressor = new CompressProps();

    private Boolean enableRandomTtl;

    private Boolean enableNullValue;

    private Boolean enableGroupPrefix;

    private RedisType redisType;

    private Integer dataSlotSize;

    private final Map<String, Object> params = new HashMap<>();

    /**
     * <b>StoreProviderId</b>
     * <p>
     * 一级缓存默认值：caffeine {@link CacheConstants#DEFAULT_EMBED_STORE_PROVIDER}
     * <p>
     * 二级缓存默认值：NONE {@link CacheConstants#DEFAULT_EXTRA_STORE_PROVIDER}
     * <p>
     * 三级缓存默认值：NONE {@link CacheConstants#DEFAULT_EXTRA_STORE_PROVIDER}
     *
     * @return {@link String} – StoreProviderId
     */
    public String getProvider() {
        return provider;
    }

    /**
     * 设置 StoreProviderId
     *
     * @param provider StoreProviderId
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Redis 数据存储类型
     * <p>
     * 可选值：<br>
     * STRING（默认值） – 优点：可以设置过期时间；缺点：key 长，耗内存，缓存数据清空操作复杂。<br>
     * HASH – 优点：key 更短，省内存，便于清空缓存数据；缺点：无法设置过期时间。
     * <p>
     * {@link CacheConstants#DEFAULT_EXTRA_REDIS_TYPE}
     *
     * @return {@link RedisType} – Redis 数据存储类型
     */
    public RedisType getRedisType() {
        return redisType;
    }

    /**
     * 设置 Redis 数据存储类型
     *
     * @param redisType Redis 数据存储类型
     */
    public void setRedisType(RedisType redisType) {
        this.redisType = redisType;
    }

    /**
     * 数据槽数量
     * <p>
     * 仅用于 Redis 集群模式，且采用 Redis-Hash 作为数据存储，其它模式下此配置无意义。
     * <p>
     * 默认值：16 <br>
     * {@link CacheConstants#DEFAULT_EXTRA_DATA_SLOT_SIZE}
     * <p>
     * 当 Redis 为集群模式时，为了让数据尽可能均匀分布于各个 Redis 节点，会创建多个 HashTable。<br>
     * 读取或保存数据时，使用 crc16 算法计算 key 的哈希值，然后取余 {@code data-slot-size} 以选择使用哪个 HashTable。
     * <p>
     * <b>示例：</b><p>
     * 设 {@code {group: shop, name: user, data-slot-size: 16, enable-group-prefix: true}}，那么 Redis 中会创建
     * {@code ["shop:user:0"、"shop:user:1", "shop:user:2", ……, "shop:user:14", "shop:user:15"]}
     * 共 16 个 HashTable。
     * <p>
     * <b>注意：</b><p>
     * 1、集群节点数越多，槽数量应越大。<br>
     * 2、最小值为 16，最大值为 16384。<br>
     * 3、配置值如非 2 的整数次幂，将自动转换为 2 的整数次幂。<br>
     * 4、配置值过小：会导致数据倾斜。<br>
     * 5、配置值过大：会产生更多的网络请求。<br>
     * 建议 {@code data-slot-size ≈ (主节点数量 × 4)}
     *
     * @return {@link Integer} - 数据槽数量
     */
    public Integer getDataSlotSize() {
        return dataSlotSize;
    }

    /**
     * 设置 数据槽数量
     *
     * @param dataSlotSize 数据槽数量
     */
    public void setDataSlotSize(Integer dataSlotSize) {
        this.dataSlotSize = dataSlotSize;
    }

    /**
     * 初始容量
     * <p>
     * 默认值：65536 <p>
     * 建议与 maximum-size 保持一致，避免扩容。
     * <p>
     * {@link CacheConstants#DEFAULT_EMBED_INITIAL_CAPACITY}
     * <p>
     * 既知使用此配置项的缓存类型：caffeine。
     *
     * @return {@link Integer} – 初始容量
     */
    public Integer getInitialCapacity() {
        return initialCapacity;
    }

    /**
     * 设置 初始容量
     *
     * @param initialCapacity 初始容量
     */
    public void setInitialCapacity(Integer initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    /**
     * 最大容量
     * <p>
     * 默认值：65536
     * <p>
     * {@link  CacheConstants#DEFAULT_EMBED_MAXIMUM_SIZE}
     * <p>
     * 如果 {@code maximum-size <= 0}，表示不采用基于容量的驱逐策略。
     * <p>
     * 既知使用此配置项的缓存类型：caffeine。
     *
     * @return {@link Long} – 最大容量
     */
    public Long getMaximumSize() {
        return maximumSize;
    }

    /**
     * 设置 最大容量
     *
     * @param maximumSize 最大容量
     */
    public void setMaximumSize(Long maximumSize) {
        this.maximumSize = maximumSize;
    }

    /**
     * 最大权重
     * <p>
     * 默认值：0
     * <p>
     * {@link  CacheConstants#DEFAULT_EMBED_MAXIMUM_WEIGHT}
     * <p>
     * 如果 {@code maximum-weight <= 0}，表示不采用基于权重的驱逐策略。
     * <p>
     * 既知使用此配置项的缓存类型：caffeine。
     *
     * @return {@link Long} – 最大权重
     */
    public Long getMaximumWeight() {
        return maximumWeight;
    }

    /**
     * 设置 最大权重
     *
     * @param maximumWeight 最大权重
     */
    public void setMaximumWeight(Long maximumWeight) {
        this.maximumWeight = maximumWeight;
    }

    /**
     * 数据写入后的存活时间
     * <p>
     * 内嵌缓存默认值：3600000 <br>
     * 外部缓存默认值：7200000
     * <p>
     * 单位：毫秒
     * <p>
     * {@link  CacheConstants#DEFAULT_EMBED_EXPIRE_AFTER_WRITE} <p>
     * {@link  CacheConstants#DEFAULT_EXTRA_EXPIRE_AFTER_WRITE}
     * <p>
     * 既知使用此配置项的缓存类型：caffeine， redis(String)。
     *
     * @return {@link Long} – 数据写入后的存活时间
     */
    public Long getExpireAfterWrite() {
        return expireAfterWrite;
    }

    /**
     * 设置 数据写入后的存活时间
     *
     * @param expireAfterWrite 数据写入后的存活时间
     */
    public void setExpireAfterWrite(Long expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }

    /**
     * 访问后的存活时间
     * <p>
     * 默认值：300000 单位：毫秒 <br>
     * {@link  CacheConstants#DEFAULT_EMBED_EXPIRE_AFTER_ACCESS}
     * <p>
     * 既知使用此配置项的缓存类型：caffeine。
     *
     * @return {@link Long} – 访问后的存活时间
     */
    public Long getExpireAfterAccess() {
        return expireAfterAccess;
    }

    /**
     * 设置 访问后的存活时间
     *
     * @param expireAfterAccess 访问后的存活时间
     */
    public void setExpireAfterAccess(Long expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
    }

    /**
     * 键的引用类型
     * <p>
     * 默认值：STRONG <br>
     * {@link  CacheConstants#DEFAULT_EMBED_KEY_STRENGTH}
     * <p>
     * 基于键的引用类型执行驱逐策略。
     * <p>
     * 既知使用此配置项的缓存类型：caffeine，可选值：WEAK(弱引用)，STRONG(强引用)。
     * <p>
     *
     * @return {@link ReferenceType} – 键的引用类型
     */
    public ReferenceType getKeyStrength() {
        return keyStrength;
    }

    /**
     * 设置 键的引用类型
     *
     * @param keyStrength 键的引用类型
     */
    public void setKeyStrength(ReferenceType keyStrength) {
        this.keyStrength = keyStrength;
    }

    /**
     * 值的引用类型
     * <p>
     * 默认值：STRONG <br>
     * {@link CacheConstants#DEFAULT_EMBED_VALUE_STRENGTH}
     * <p>
     * 基于值的引用类型执行驱逐策略。
     * <p>
     * 既知使用此配置项的缓存类型：caffeine，可选值：WEAK(弱引用)，SOFT(软引用)，STRONG(强引用)。
     *
     * @return {@link ReferenceType} – 值的引用类型
     */
    public ReferenceType getValueStrength() {
        return valueStrength;
    }

    /**
     * 设置 值的引用类型
     *
     * @param valueStrength 值的引用类型
     */
    public void setValueStrength(ReferenceType valueStrength) {
        this.valueStrength = valueStrength;
    }

    /**
     * CodecProviderId
     * <p>
     * 用于值的序列化
     * <p>
     * 默认值：<br>
     * 内嵌缓存：none <br>
     * 外部缓存：jackson（如果是用 spring-cache，请改为 jackson-spring）
     * <p>
     * {@link CacheConstants#DEFAULT_EMBED_VALUE_CODEC}
     * {@link CacheConstants#DEFAULT_EXTRA_VALUE_CODEC}
     * <p>
     * 可选值：jackson，jdk
     * <p>
     * 内嵌缓存的序列化是可选的，外部缓存的序列化是必需的。<br>
     * 序列化有一定性能损失，因此除非有特殊原因，否则内嵌缓存不建议使用序列化。
     *
     * @return {@link String} – CodecProviderId
     */
    public String getValueCodec() {
        return valueCodec;
    }

    /**
     * 设置 CodecProviderId
     *
     * @param valueCodec CodecProviderId
     */
    public void setValueCodec(String valueCodec) {
        this.valueCodec = valueCodec;
    }

    /**
     * 压缩配置
     * <p>
     * 用于缓存值序列化后再压缩（默认不压缩）
     *
     * @return {@link CompressProps} – 压缩配置
     */
    public CompressProps getValueCompressor() {
        return valueCompressor;
    }

    /**
     * 设置 压缩配置
     *
     * @param valueCompressor 压缩配置
     */
    public void setValueCompressor(CompressProps valueCompressor) {
        this.valueCompressor = valueCompressor;
    }

    /**
     * 是否使用随机存活时间
     * <p>
     * 使用随机存活时间，是为了避免大规模的 key 集中过期，然后同时回源查询，造成数据源压力过大。
     * <p>
     * 默认值：<br>
     * 内嵌缓存：true <br>
     * 外部缓存：true
     * <p>
     * {@link CacheConstants#DEFAULT_EMBED_ENABLE_RANDOM_TTL}
     * {@link CacheConstants#DEFAULT_EXTRA_ENABLE_RANDOM_TTL}
     * <p>
     * {@snippet :
     * import com.igeeksky.xtool.core.lang.RandomUtils;
     * if(expireAfterWrite > 0 && enableRandomTtl){
     *      long minTtl = RandomUtils.nextLong(expireAfterWrite * 0.8, expireAfterWrite);
     *      long maxTtl = expireAfterWrite;
     * }
     *}
     *
     * @return {@link Boolean} – 是否使用随机存活时间
     */
    public Boolean getEnableRandomTtl() {
        return enableRandomTtl;
    }

    /**
     * 设置 是否使用随机存活时间
     *
     * @param enableRandomTtl 是否使用随机存活时间
     */
    public void setEnableRandomTtl(Boolean enableRandomTtl) {
        this.enableRandomTtl = enableRandomTtl;
    }

    /**
     * 是否允许保存空值
     * <p>
     * 如果此选项为 true，当 value 为 null 时，也会保存到缓存，目的是为了减少无效的回源查询。
     * <p>
     * 默认值：<br>
     * 内嵌缓存：true <br>
     * 外部缓存：true
     * <p>
     * {@link CacheConstants#DEFAULT_EMBED_ENABLE_NULL_VALUE}
     * {@link CacheConstants#DEFAULT_EXTRA_ENABLE_NULL_VALUE}
     *
     * @return {@link Boolean} – 是否允许保存空值
     */
    public Boolean getEnableNullValue() {
        return enableNullValue;
    }

    /**
     * 设置 是否允许保存空值
     *
     * @param enableNullValue 是否允许保存空值
     */
    public void setEnableNullValue(Boolean enableNullValue) {
        this.enableNullValue = enableNullValue;
    }

    /**
     * 是否附加 group 作为键前缀
     * <p>
     * 当使用外部缓存时，如仅使用 cacheName 作为前缀会导致键冲突，则需再附加 group 作为前缀。
     * <p>
     * 默认值：<br>
     * 内嵌缓存：此选项无效 <br>
     * 外部缓存：true
     * <p>
     * {@link CacheConstants#DEFAULT_EXTRA_ENABLE_GROUP_PREFIX}
     * <p>
     * 如果 enableGroupPrefix 为 true，则完整的键为：{@code group + ":" + cacheName + ":" + key}。<br>
     * 如果 enableGroupPrefix 为 false，则完整的键为：{@code cacheName + ":" + key}。
     *
     * @return {@link Boolean} – 键是否使用前缀
     */
    public Boolean getEnableGroupPrefix() {
        return enableGroupPrefix;
    }

    /**
     * 设置 是否附加 group 作为键前缀
     *
     * @param enableGroupPrefix 是否附加 group 作为键前缀
     */
    public void setEnableGroupPrefix(Boolean enableGroupPrefix) {
        this.enableGroupPrefix = enableGroupPrefix;
    }

    /**
     * 扩展参数
     * <p>
     * 自定义扩展实现时，如需用到额外的未定义参数，可在此配置。
     * <p>
     * 如使用 xcache 内置实现，则无需此配置。<br>
     * 如不使用，请删除，否则会导致 SpringBoot 读取配置错误而启动失败。
     *
     * @return {@code Map<String, Object>} - 扩展参数
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * 设置扩展参数
     *
     * @param params 扩展参数
     */
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
