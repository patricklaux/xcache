package com.igeeksky.xcache.redis;

import com.igeeksky.xcache.common.ExpiryKeyValue;
import com.igeeksky.xcache.common.KeyValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Redis命令接口
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-03
 */
public interface RedisStringWriter extends RedisWriter {

    Mono<byte[]> get(byte[] key);

    Flux<KeyValue<byte[], byte[]>> mget(byte[]... keys);

    Mono<Void> set(byte[] key, byte[] value);

    Mono<Void> psetex(byte[] key, long milliseconds, byte[] value);

    Mono<Void> mset(Map<byte[], byte[]> keyValues);

    Mono<Void> mpsetex(List<ExpiryKeyValue<byte[], byte[]>> keyValues);

    Mono<Long> del(byte[]... keys);

}
