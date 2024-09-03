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

    /**
     * 数据同步策略 <p>
     * 一级缓存默认值：ALL <p>
     * 如果仅有一级缓存，且为本地缓存：如需同步 clear 事件，请改成 CLEAR；如无需同步 clear 事件，请改成 NONE。<p>
     * 否则，多个实例会重复以下过程，缓存失效：A(load and put) -- A(send msg)
     * -- B(receive msg) -- B(remove) -- B(load and put) -- B(send msg) -- A(receive msg) -- A(remove)……
     *
     * @see SyncType
     */
    private SyncType first;

    /**
     * 数据同步策略 <p>
     * 二级缓存默认值：NONE <p>
     *
     * @see SyncType
     */
    private SyncType second;

    /**
     * 数据同步通道名称的中缀
     * <p>
     * 如果为空，使用 application 替代。
     * 完整通道名称："sync:" + infix + ":" + cache-name
     * <p>
     * 如果为 “NONE”，完整通道名称："sync:" + cache-name
     * <pre>{@code
     * if(Objects.equals(infix, "NONE")){
     *     channel = "sync:" + cache-name;
     * }else{
     *     if(infix == null){
     *         infix = application;
     *     }
     *     channel = "sync:" + infix + ":" + cache-name;
     * }
     * }</pre>
     */
    private String infix;

    /**
     * 消息队列最大长度（近似值）
     * <p>
     * 默认值：10000
     * <p>
     * 发送消息时自动裁剪长度，避免占用过多内存 <p>
     * 缓存同步队列的最大长度，请根据写入速率、读取速率、网路情况等相关信息进行设置
     * </p>
     */
    private Long maxLen;

    /**
     * 数据同步 provider
     * <p>
     * 默认值：lettuceCacheSyncProvider <p>
     * 需与 xcache.redis.sync-providers. .id 保持一致
     * <p>
     * {@link CacheConstants#DEFAULT_SYNC_PROVIDER}
     */
    private String provider;

    /**
     * 自定义扩展属性
     */
    private final Map<String, Object> params = new HashMap<>();

    public SyncType getFirst() {
        return first;
    }

    public void setFirst(SyncType first) {
        this.first = first;
    }

    public SyncType getSecond() {
        return second;
    }

    public void setSecond(SyncType second) {
        this.second = second;
    }

    public String getInfix() {
        return infix;
    }

    public void setInfix(String infix) {
        this.infix = infix;
    }

    public Long getMaxLen() {
        return maxLen;
    }

    public void setMaxLen(Long maxLen) {
        this.maxLen = maxLen;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
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
