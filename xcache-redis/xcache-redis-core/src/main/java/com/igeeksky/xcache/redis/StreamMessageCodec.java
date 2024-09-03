package com.igeeksky.xcache.redis;

import java.util.Map;

/**
 * Redis 流消息编解码器
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/8/30
 */
public interface StreamMessageCodec<T> {

    byte[] encodeKey(String key);

    String decodeKey(byte[] key);

    Map<byte[], byte[]> encodeMsg(T message);

    T decodeMsg(Map<byte[], byte[]> body);

}
