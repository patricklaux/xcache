package com.igeeksky.xcache.autoconfigure.redis;

import com.igeeksky.xcache.extension.stat.CacheStatMessage;
import com.igeeksky.xcache.props.CacheConstants;
import com.igeeksky.xcache.redis.stat.RedisCacheStatProvider;
import com.igeeksky.xtool.core.json.SimpleJSON;

/**
 * {@link RedisCacheStatProvider} 配置项
 */
public class RedisStatOptions {

    /**
     * 缓存指标统计的时间间隔
     * <p>
     * 默认值：60000，单位：毫秒
     */
    private long period = 60000;

    /**
     * Redis stream 最大长度
     * <p>
     * 默认值：10000
     */
    private long maxLen = 10000;

    /**
     * 统计消息的 stream channel，是否附加 group 作为后缀
     * <p>
     * 默认值：false
     * <p>
     * 如果为 true，完整的 channel 为：{@code String channel = "stat:" + group} <br>
     * 如果为 false，完整的 channel 为：{@code String channel = "stat:" } <p>
     * {@link CacheStatMessage} 已有 group 属性，因此可省略 group。
     * 如希望多个应用的缓存指标共用一套消费者进行统计汇总，且新增应用后无需再手动添加 channel，则建议保持默认，不附加 group。
     */
    private Boolean enableGroupPrefix;

    /**
     * 统计消息的编码格式
     * <p>
     * 默认值：UTF-8
     */
    private String charset = "UTF-8";

    /**
     * 统计消息的编解码器
     * <p>
     * 默认值：jackson
     *
     * @see CacheConstants#JACKSON_CODEC
     */
    private String codec = CacheConstants.JACKSON_CODEC;

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public Boolean getEnableGroupPrefix() {
        return enableGroupPrefix;
    }

    public void setEnableGroupPrefix(Boolean enableGroupPrefix) {
        this.enableGroupPrefix = enableGroupPrefix;
    }

    public long getMaxLen() {
        return maxLen;
    }

    public void setMaxLen(long maxLen) {
        this.maxLen = maxLen;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}