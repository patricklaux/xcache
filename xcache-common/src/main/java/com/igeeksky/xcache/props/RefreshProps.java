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

    private String infix;

    private String provider;

    private Long period;

    private Long stopAfterAccess;

    private final Map<String, Object> params = new HashMap<>();

    /**
     * 中缀 <p>
     * 用于构建一系列需要使用的 Key。
     * 如果未配置，则默认用 xcache.app 的配置值替代.
     * 如果配置为 none，则不使用中缀。
     * {@snippet :
     *     // 用于保存所有访问记录
     *     String refreshKey = "refresh:" + infix + ":" + cacheName;
     *     // 用于避免同一缓存的多个实例同时执行刷新任务
     *     String refreshLockKey = "refresh:lock:" + infix + ":" + cacheName;
     *     // 用于保证同一缓存的多个实例一个周期内仅执行一次刷新任务
     *     String refreshPeriodKey = "refresh:period" + infix + ":" + cacheName;
     *}
     *
     * @return {@link String} - 中缀
     */
    public String getInfix() {
        return infix;
    }

    public void setInfix(String infix) {
        this.infix = infix;
    }

    /**
     * CacheRefreshProviderId
     * <p>
     * 默认值：none
     * <p>
     * {@link CacheConstants#DEFAULT_REFRESH_PROVIDER}
     *
     * @return {@link String} - CacheRefreshProviderId
     */
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * 刷新间隔周期 <p>
     * 默认值：1800000 单位：毫秒
     *
     * @return {@link Long} - 刷新间隔周期
     */
    public Long getPeriod() {
        return period;
    }

    public void setPeriod(Long period) {
        this.period = period;
    }

    /**
     * 停止刷新时限 <p>
     * 默认值：7200000 单位：毫秒 <p>
     * 最后一次访问后，超过此时限则不再刷新
     *
     * @return {@link Long} - 停止刷新时限
     */
    public Long getStopAfterAccess() {
        return stopAfterAccess;
    }

    public void setStopAfterAccess(Long stopAfterAccess) {
        this.stopAfterAccess = stopAfterAccess;
    }

    /**
     * 扩展参数
     * <p>
     * 自定义实现 {@code com.igeeksky.xcache.extension.refresh.CacheRefreshProvider} 时，
     * 如需用到额外的未定义参数，可在此配置。
     *
     * @return {@link Map} - 扩展参数
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