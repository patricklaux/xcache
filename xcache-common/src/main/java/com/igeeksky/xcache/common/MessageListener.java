package com.igeeksky.xcache.common;

import java.util.Map;

/**
 * 消息监听者
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public interface MessageListener {

    /**
     * 消息监听
     *
     * @param message 消息(消息对象的每个属性分别序列化后转换成 map)
     */
    void onMessage(Map<byte[], byte[]> message);

}
