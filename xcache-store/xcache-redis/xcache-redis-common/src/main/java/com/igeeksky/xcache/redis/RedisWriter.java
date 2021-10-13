package com.igeeksky.xcache.redis;

import reactor.core.publisher.Mono;

/**
 * Redis命令接口
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-03
 */
public interface RedisWriter extends AutoCloseable {

    String OK = "OK";

    Mono<Void> reactiveClose();

    @Override
    default void close() {
        reactiveClose().subscribe();
    }
}
