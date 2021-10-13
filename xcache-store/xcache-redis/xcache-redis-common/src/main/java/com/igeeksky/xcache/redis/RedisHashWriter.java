package com.igeeksky.xcache.redis;

import com.igeeksky.xcache.common.KeyValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Redis命令接口
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-03
 */
public interface RedisHashWriter extends RedisWriter {

    Mono<byte[]> hget(byte[] key, byte[] field);

    Flux<KeyValue<byte[], byte[]>> hmget(byte[] key, byte[]... field);

    Mono<Boolean> hset(byte[] key, byte[] field, byte[] value);

    Mono<Void> hmset(byte[] key, Map<byte[], byte[]> map);

    Mono<Long> hdel(byte[] key, byte[]... fields);

    Mono<Long> del(byte[]... keys);

}
