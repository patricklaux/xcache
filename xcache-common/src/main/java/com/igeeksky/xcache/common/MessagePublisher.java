package com.igeeksky.xcache.common;

import java.util.Map;

/**
 * 消息发布者
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public interface MessagePublisher {

    /**
     * @param channel 消息通道名称，根据缓存名称自动生成
     * @param message message 消息(消息对象的每个属性分别序列化后转换成 map)
     */
    void publish(byte[] channel, Map<byte[], byte[]> message);

}
