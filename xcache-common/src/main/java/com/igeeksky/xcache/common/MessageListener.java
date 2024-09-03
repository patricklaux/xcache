package com.igeeksky.xcache.common;

/**
 * 消息监听者
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public interface MessageListener<T> {

    /**
     * 消息监听
     *
     * @param message 消息(消息对象的每个属性分别序列化后转换成 map)
     */
    void onMessage(T message);

}
