package com.igeeksky.xcache.props;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/12
 */
public class LockProps {

    /**
     * LockProvider - id
     * <p>
     * 默认值：localCacheLockProvider
     * <p>
     * {@link CacheConstants#DEFAULT_LOCK_PROVIDER}
     */
    private String provider;

    /**
     * 中缀
     * <p>
     * 如果是分布式锁，不同应用不同缓存的锁需要通过前缀加以区分，前缀通过中缀生成，代码如下：
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
     */
    private String infix;

    /**
     * 本机缓存锁使用 HashMap 来存储锁，初始的 HashMap 大小
     * <p>
     * {@link CacheConstants#DEFAULT_LOCK_INITIAL_CAPACITY}
     */
    private Integer initialCapacity;

    /**
     * 锁最长租用时间
     * <p>
     * 默认值：1000  单位：毫秒
     * <p>
     * {@link CacheConstants#DEFAULT_LOCK_LEASE_TIME}
     */
    private Long leaseTime;

    /**
     * 自定义扩展属性
     */
    private final Map<String, Object> params = new HashMap<>();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getInfix() {
        return infix;
    }

    public void setInfix(String infix) {
        this.infix = infix;
    }

    public Integer getInitialCapacity() {
        return initialCapacity;
    }

    public void setInitialCapacity(Integer initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    public Long getLeaseTime() {
        return leaseTime;
    }

    public void setLeaseTime(Long leaseTime) {
        this.leaseTime = leaseTime;
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
