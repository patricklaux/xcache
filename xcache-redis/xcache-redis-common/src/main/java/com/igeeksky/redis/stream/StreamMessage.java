package com.igeeksky.redis.stream;

import java.util.Map;

/**
 * Redis 流消息
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/20
 */
public record StreamMessage(byte[] key, String id, Map<byte[], byte[]> body) {

}