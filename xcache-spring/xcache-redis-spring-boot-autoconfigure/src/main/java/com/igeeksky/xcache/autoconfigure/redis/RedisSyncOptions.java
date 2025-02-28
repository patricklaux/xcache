package com.igeeksky.xcache.autoconfigure.redis;

import com.igeeksky.xcache.redis.sync.RedisCacheSyncProvider;
import com.igeeksky.xtool.core.json.SimpleJSON;

/**
 * {@link RedisCacheSyncProvider} 配置项
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class RedisSyncOptions {

    /**
     * 数据同步编解码器 ID
     */
    private String codec = "jackson";

    /**
     * 数据同步编解码器 ID
     * <p>
     * 默认值：jackson
     *
     * @return 编解码器 ID
     */
    public String getCodec() {
        return codec;
    }

    /**
     * 数据同步编解码器 ID
     * <p>
     * 默认值：jackson
     *
     * @param codec 编解码器 ID
     */
    public void setCodec(String codec) {
        this.codec = codec;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}
