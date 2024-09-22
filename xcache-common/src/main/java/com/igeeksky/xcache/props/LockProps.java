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

    private String infix;

    private Integer initialCapacity;

    private Long leaseTime;

    private final Map<String, Object> params = new HashMap<>();

    /**
     * LockProviderId
     * <p>
     * 默认值：embed
     * <p>
     * {@link CacheConstants#DEFAULT_LOCK_PROVIDER}
     *
     * @return String - LockProviderId
     */
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * 中缀
     * <p>
     * 分布式锁，不同应用的缓存锁需要通过中缀加以区分，代码如下：
     * <pre>{@code
     * if (infix == null) {
     *     prefix = "lock:" + application + ":" + cacheName + ":";
     * } else {
     *     if (Objects.equals("NONE", StringUtils.toUpperCase(infix))) {
     *          prefix = "lock:" + cacheName + ":";
     *     } else {
     *          prefix = "lock:" + infix + ":" + cacheName + ":";
     *     }
     * }
     * }</pre>
     *
     * @return String - 中缀
     */
    public String getInfix() {
        return infix;
    }

    public void setInfix(String infix) {
        this.infix = infix;
    }

    /**
     * Xcache 使用 HashMap 维护缓存锁对象，因此可定义初始的 HashMap 大小
     * <p>
     * 默认值：256
     * <p>
     * {@link CacheConstants#DEFAULT_LOCK_INITIAL_CAPACITY}
     *
     * @return Integer - 初始的 HashMap 大小
     */
    public Integer getInitialCapacity() {
        return initialCapacity;
    }

    public void setInitialCapacity(Integer initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    /**
     * 锁租用时间
     * <p>
     * 默认值：1000  单位：毫秒
     * <p>
     * {@link CacheConstants#DEFAULT_LOCK_LEASE_TIME}
     * <p>
     * 既知 {@code RedisSpinLock} 有用此配置，{@code EmbedLock} 无需此配置
     *
     * @return Long - 锁租用时间
     */
    public Long getLeaseTime() {
        return leaseTime;
    }

    public void setLeaseTime(Long leaseTime) {
        this.leaseTime = leaseTime;
    }

    /**
     * 扩展参数
     *
     * @return {@code Map<String, Object>} - 扩展参数
     */
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
