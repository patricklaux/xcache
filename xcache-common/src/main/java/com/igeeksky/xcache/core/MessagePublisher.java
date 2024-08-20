package com.igeeksky.xcache.core;

import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public interface MessagePublisher {

    void publish(byte[] channel, Map<byte[], byte[]> message);

}
