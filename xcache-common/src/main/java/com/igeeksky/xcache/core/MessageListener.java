package com.igeeksky.xcache.core;

import java.util.Map;

/**
 * 广播消息的消费者
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public interface MessageListener {

    void onMessage(Map<byte[], byte[]> message);

}
