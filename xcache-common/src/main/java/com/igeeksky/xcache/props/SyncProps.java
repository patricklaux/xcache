package com.igeeksky.xcache.props;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据更新同步配置
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/11
 */
public class SyncProps {

    private Boolean first;

    private Boolean second;

    private Long maxLen;

    private String provider;

    private Boolean enableGroupPrefix;

    private final Map<String, Object> params = new HashMap<>();

    /**
     * 一级缓存是否启用数据同步
     * <p>
     * 默认值：true
     * <p>
     * 当且仅当满足以下两个条件，此选项才能设置为 true：<br>
     * 1. 一级缓存为私有缓存；<br>
     * 2. 二级缓存或三级缓存至少有一级为共享缓存。
     * <p>
     * 如仅有一级缓存，又设置为 true，多个实例会重复以下过程，导致每次查询缓存数据均需回源：<br>
     * A(load and put) -- A(send msg) -- B(receive msg) -- B(remove) -- B(load and put) -- B(send msg)
     * -- A(receive msg) -- A(remove)……
     * <p>
     * 当然，如果无需数据同步，建议直接将 provider 配置项设为 none。
     *
     * @return {@link Boolean} – 一级缓存是否启用数据同步
     */
    public Boolean getFirst() {
        return first;
    }

    /**
     * @param first 数据同步类型
     */
    public void setFirst(Boolean first) {
        this.first = first;
    }

    /**
     * 二级缓存是否启用数据同步
     * <p>
     * 默认值：false
     * <p>
     * 当且仅当满足以下两个条件，此选项才能设置为 true：<br>
     * 1. 二级缓存为私有缓存；<br>
     * 2. 三级缓存为共享缓存。
     *
     * @return {@link Boolean} – 二级缓存是否启用数据同步
     */
    public Boolean getSecond() {
        return second;
    }

    /**
     * @param second 数据同步类型
     */
    public void setSecond(Boolean second) {
        this.second = second;
    }

    /**
     * 是否添加 group 作为前缀
     * <p>
     * 如仅使用 cacheName 作为前缀会导致键冲突，则需再附加 group 作为前缀。
     * <p>
     * 默认值：true <br>
     * {@link CacheConstants#DEFAULT_ENABLE_GROUP_PREFIX}
     * <p>
     * 如果为 true，则完整的键为：{@code "sync:" + group + ":" + cacheName}。<br>
     * 如果为 false，则完整的键为：{@code "sync:" + cacheName}。
     *
     * @return {@link Boolean} - 是否添加 group 作为前缀
     */
    public Boolean getEnableGroupPrefix() {
        return enableGroupPrefix;
    }

    /**
     * 设置 是否添加 group 作为前缀
     *
     * @param enableGroupPrefix 是否添加 group 作为前缀
     */
    public void setEnableGroupPrefix(Boolean enableGroupPrefix) {
        this.enableGroupPrefix = enableGroupPrefix;
    }

    /**
     * 缓存同步队列最大长度
     * <p>
     * 默认值：10000
     * <p>
     * 如果使用 redis stream 处理数据同步消息，发送消息时会自动裁剪为设置长度（近似值）.
     *
     * @return {@link Long} – 缓存同步队列最大长度
     */
    public Long getMaxLen() {
        return maxLen;
    }

    /**
     * 设置消息队列最大长度
     *
     * @param maxLen 消息队列最大长度
     */
    public void setMaxLen(Long maxLen) {
        this.maxLen = maxLen;
    }

    /**
     * CacheSyncProviderId
     * <p>
     * 默认值：none
     * <p>
     * {@link CacheConstants#DEFAULT_SYNC_PROVIDER}
     *
     * @return {@link String} – CacheSyncProviderId
     */
    public String getProvider() {
        return provider;
    }

    /**
     * 设置 CacheSyncProviderId
     *
     * @param provider CacheSyncProviderId
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * 扩展参数
     * <p>
     * 自定义扩展实现时，如需用到额外的未定义参数，可在此配置。
     * <p>
     * 如使用 xcache 内置实现，则无需此配置。<br>
     * 如不使用，请删除，否则会导致 SpringBoot 读取配置错误而启动失败。
     *
     * @return {@link Map} – 扩展属性
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * 设置扩展属性
     *
     * @param params 扩展属性
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
