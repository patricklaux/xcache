package com.igeeksky.xcache.props;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.HashMap;
import java.util.Map;

/**
 * 锁配置
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/12
 */
public class LockProps {

    private String provider;

    private Long leaseTime;

    private Integer initialCapacity;

    private Boolean enableGroupPrefix;

    private final Map<String, Object> params = new HashMap<>();

    /**
     * LockProviderId
     * <p>
     * 默认值：embed
     * <p>
     * {@link CacheConstants#DEFAULT_LOCK_PROVIDER}
     *
     * @return {@code String} - LockProviderId
     */
    public String getProvider() {
        return provider;
    }

    /**
     * 设置 LockProviderId
     *
     * @param provider LockProviderId
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * 锁租期
     * <p>
     * 默认值：1000  单位：毫秒
     * <p>
     * {@link CacheConstants#DEFAULT_LOCK_LEASE_TIME}
     * <p>
     * 既知 {@code RedisSpinLock} 使用此配置，{@code EmbedLock} 无需此配置。
     * <p>
     * 对于分布式锁，设置租期是为了避免因为异常原因（进程意外终止、网络故障……）无法主动释放锁，而导致死锁。
     * <p>
     * 锁租期内，如果持有锁的线程申请续期，则锁会继续被该线程持有；<p>
     * 锁租期内，如果持有锁的线程未申请续期，则到期后锁将自动释放；<p>
     * 锁租期内，如果持有锁的线程主动释放锁，则锁会被释放。
     *
     * @return {@link Long} - 锁租期
     */
    public Long getLeaseTime() {
        return leaseTime;
    }

    /**
     * 设置锁租期
     *
     * @param leaseTime 锁租期
     */
    public void setLeaseTime(Long leaseTime) {
        this.leaseTime = leaseTime;
    }

    /**
     * HashMap 初始容量
     * <p>
     * xcache 使用 HashMap 保证 相同的 key 获取到同一个锁对象，可在此配置初始容量。<br>
     * 另，不同名缓存，存放锁对象的 HashMap 不同，因此可以根据 cacheName 分配不同的初始容量。
     * <p>
     * 默认值：256
     * <p>
     * {@link CacheConstants#DEFAULT_LOCK_INITIAL_CAPACITY}
     * <p>
     * 设置此值仅仅是为了避免或减少 Map 扩容，对大多数应用来说，保持默认值即可。
     * <p>
     * 可根据单次回源时间内，一个缓存实例的回源次数，并结合网络延迟、数据源压力等因素综合考虑，设置此值。
     *
     * @return Integer -  HashMap 初始容量
     */
    public Integer getInitialCapacity() {
        return initialCapacity;
    }

    /**
     * 设置 HashMap 初始容量
     *
     * @param initialCapacity HashMap 初始容量
     */
    public void setInitialCapacity(Integer initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    /**
     * 是否添加 group 作为前缀
     * <p>
     * 当使用分布式锁时，如仅使用 cacheName 作为前缀会导致键冲突，则需再附加 group 作为前缀。
     * <p>
     * 默认值：true <br>
     * {@link CacheConstants#DEFAULT_ENABLE_GROUP_PREFIX}
     * <p>
     * 如果为 true，则完整的键为：{@code "lock:" + group + ":" + cacheName + ":" + key}。<br>
     * 如果为 false，则完整的键为：{@code "lock:" + cacheName + ":" + key}。
     *
     * @return {@link Boolean} - 是否添加 group 作为前缀
     */
    public Boolean getEnableGroupPrefix() {
        return enableGroupPrefix;
    }

    /**
     * 设置是否添加 group 作为前缀
     *
     * @param enableGroupPrefix 是否添加 group 作为前缀
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
