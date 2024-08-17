package com.igeeksky.xcache.props;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存刷新配置
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/6
 */
public class RefreshProps {

    /**
     * 中缀 <p>
     * 用于构建一系列需要使用的 Key，如果为空，则默认用 application 替代
     * <pre>{@code
     *     // 用于保存所有访问记录
     *     String refreshKey = "refresh:" + infix + cacheName
     *     // 用于避免同一缓存的多个实例同时执行刷新任务
     *     String refreshLockKey = "refresh:lock:" + infix + cacheName
     *     // 用于保证同一缓存的多个实例一个周期内仅执行一次刷新任务
     *     String refreshPeriodKey = "refresh:period" + infix + cacheName
     * }</pre>
     */
    private String infix;

    /**
     * CacheRefreshProvider - id
     * <p>
     * 默认值：NONE
     * <p>
     * {@link CacheConstants#DEFAULT_REFRESH_PROVIDER}
     */
    private String provider;

    /**
     * 刷新间隔周期 <p>
     * 默认值：1800000 单位：毫秒
     */
    private Long period;

    /**
     * 停止刷新时限 <p>
     * 默认值：7200000 单位：毫秒 <p>
     * 最后一次访问后，超过此时限则不再刷新
     */
    private Long stopAfterAccess;

    /**
     * 自定义扩展属性
     */
    private final Map<String, Object> params = new HashMap<>();

    public String getInfix() {
        return infix;
    }

    public void setInfix(String infix) {
        this.infix = infix;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Long getPeriod() {
        return period;
    }

    public void setPeriod(Long period) {
        this.period = period;
    }

    public Long getStopAfterAccess() {
        return stopAfterAccess;
    }

    public void setStopAfterAccess(Long stopAfterAccess) {
        this.stopAfterAccess = stopAfterAccess;
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