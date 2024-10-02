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

    private SyncType first;

    private SyncType second;

    private Long maxLen;

    private String provider;

    private Boolean enableGroupPrefix;

    private final Map<String, Object> params = new HashMap<>();

    /**
     * 一级缓存数据同步类型
     * <p>
     * 默认值：ALL
     * <p>
     * 如仅有一级缓存，且为本地缓存，只能设置为 NONE 或 CLEAR：<br>
     * 需要同步缓存清空事件，请设置为 CLEAR；无需同步缓存清空事件，请设置为 NONE. <p>
     * 如仅有一级缓存，又设置为 ALL，多个实例会重复以下过程，导致每次查询缓存数据均需回源：<p>
     * A(load and put) -- A(send msg) -- B(receive msg) -- B(remove) -- B(load and put) -- B(send msg)
     * -- A(receive msg) -- A(remove)……
     *
     * @return {@link SyncType} – 一级缓存数据同步类型
     */
    public SyncType getFirst() {
        return first;
    }

    /**
     * @param first 数据同步类型
     */
    public void setFirst(SyncType first) {
        this.first = first;
    }

    /**
     * 二级缓存数据同步类型
     * <p>
     * 默认值：NONE
     *
     * @return {@link SyncType} – 二级缓存数据同步类型
     */
    public SyncType getSecond() {
        return second;
    }

    /**
     * @param second 数据同步类型
     */
    public void setSecond(SyncType second) {
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
     * 默认值：lettuce
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
