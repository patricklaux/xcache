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

    private Long period;

    private String provider;

    private Long stopAfterAccess;

    private Boolean enableGroupPrefix;

    private final Map<String, Object> params = new HashMap<>();

    /**
     * 是否附加 group 作为键前缀
     * <p>
     * 当使用外部的缓存刷新时，如仅使用 cacheName 作为前缀会导致键冲突，则需再附加 group 作为前缀。
     * <p>
     * 默认值：<br>
     * 内嵌缓存：此选项无效 <br>
     * 外部缓存：true
     * <p>
     * {@link CacheConstants#DEFAULT_EXTRA_ENABLE_GROUP_PREFIX}
     * <p>
     * 如果 enableGroupPrefix 为 true：<br>
     * 用于保存所有访问记录 {@code String refreshKey = "refresh:" + group + ":" + cacheName} <br>
     * 用于避免同名缓存的多个实例同时执行刷新任务：{@code String refreshLockKey = "refresh:lock:" + group + ":" + cacheName} <br>
     * 用于保证同名缓存的多个实例一个周期内仅执行一次刷新任务：{@code String refreshPeriodKey = "refresh:period:" + group + ":" + cacheName}
     * <p>
     * 如果 enableGroupPrefix 为 false：<br>
     * 用于保存所有访问记录 {@code String refreshKey = "refresh:" + cacheName} <br>
     * 用于避免同名缓存的多个实例同时执行刷新任务：{@code String refreshLockKey = "refresh:lock:" + cacheName} <br>
     * 用于保证同名缓存的多个实例一个周期内仅执行一次刷新任务：{@code String refreshPeriodKey = "refresh:period:" + cacheName}
     * <p>
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
     * CacheRefreshProviderId
     * <p>
     * 默认值：none
     * <p>
     * {@link CacheConstants#DEFAULT_REFRESH_PROVIDER}
     * <p>
     * 注意：<br>
     * CacheRefresh 依赖于 CacheLoader，如未配置 CacheLoader，请保持默认值 none，否则会抛出配置错误异常。
     *
     * @return {@link String} - CacheRefreshProviderId
     */
    public String getProvider() {
        return provider;
    }

    /**
     * 设置 CacheRefreshProviderId
     *
     * @param provider CacheRefreshProviderId
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * 刷新间隔周期
     * <p>
     * 默认值：1800000 单位：毫秒
     * <p>
     * {@link CacheConstants#DEFAULT_REFRESH_PERIOD}
     * <p>
     * 用于定期执行刷新任务。
     *
     * @return {@link Long} - 刷新间隔周期
     */
    public Long getPeriod() {
        return period;
    }

    /**
     * 设置刷新间隔周期
     *
     * @param period 刷新间隔周期
     */
    public void setPeriod(Long period) {
        this.period = period;
    }

    /**
     * 停止刷新时限
     * <p>
     * 默认值：7200000 单位：毫秒
     * <p>
     * {@link CacheConstants#DEFAULT_REFRESH_STOP_AFTER_ACCESS}
     * <p>
     * 最后一次访问后，超过此时限则不再刷新
     *
     * @return {@link Long} - 停止刷新时限
     */
    public Long getStopAfterAccess() {
        return stopAfterAccess;
    }

    /**
     * 设置停止刷新时限
     *
     * @param stopAfterAccess 停止刷新时限
     */
    public void setStopAfterAccess(Long stopAfterAccess) {
        this.stopAfterAccess = stopAfterAccess;
    }

    /**
     * 扩展参数
     * <p>
     * 自定义扩展实现时，如需用到额外的未定义参数，可在此配置。
     * <p>
     * 如使用 xcache 内置实现，则无需此配置。<br>
     * 如不使用，请删除，否则会导致 SpringBoot 读取配置错误而启动失败。
     *
     * @return {@link Map} - 扩展参数
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