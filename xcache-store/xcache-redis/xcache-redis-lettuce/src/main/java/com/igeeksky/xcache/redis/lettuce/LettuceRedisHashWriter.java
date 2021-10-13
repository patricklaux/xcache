package com.igeeksky.xcache.redis.lettuce;

import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.redis.RedisHashWriter;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisHashReactiveCommands;
import io.lettuce.core.api.reactive.RedisKeyReactiveCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.reactive.RedisAdvancedClusterReactiveCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-12
 */
public class LettuceRedisHashWriter implements RedisHashWriter {

    private static final Logger logger = LoggerFactory.getLogger(LettuceRedisHashWriter.class);

    private final AbstractRedisClient abstractRedisClient;
    private final StatefulConnection<byte[], byte[]> statefulConnection;
    private final RedisHashReactiveCommands<byte[], byte[]> hashReactiveCommands;
    private final RedisKeyReactiveCommands<byte[], byte[]> keyReactiveCommands;

    public LettuceRedisHashWriter(AbstractRedisClient abstractRedisClient) {
        this.abstractRedisClient = abstractRedisClient;
        if (abstractRedisClient instanceof RedisClusterClient) {
            RedisClusterClient redisClusterClient = (RedisClusterClient) abstractRedisClient;
            StatefulRedisClusterConnection<byte[], byte[]> connection = redisClusterClient.connect(new ByteArrayCodec());
            this.statefulConnection = connection;
            RedisAdvancedClusterReactiveCommands<byte[], byte[]> reactiveCommands = connection.reactive();
            this.hashReactiveCommands = reactiveCommands;
            this.keyReactiveCommands = reactiveCommands;
        } else if (abstractRedisClient instanceof RedisClient) {
            RedisClient redisClient = (RedisClient) abstractRedisClient;
            StatefulRedisConnection<byte[], byte[]> connection = redisClient.connect(new ByteArrayCodec());
            this.statefulConnection = connection;
            RedisReactiveCommands<byte[], byte[]> reactiveCommands = connection.reactive();
            this.hashReactiveCommands = reactiveCommands;
            this.keyReactiveCommands = reactiveCommands;
        } else {
            throw new UnsupportedOperationException("Unsupported Redis Client.");
        }
    }

    @Override
    public Mono<byte[]> hget(byte[] key, byte[] field) {
        return hashReactiveCommands.hget(key, field);
    }

    @Override
    public Flux<KeyValue<byte[], byte[]>> hmget(byte[] key, byte[]... fields) {
        return hashReactiveCommands.hmget(key, fields)
                .map(kv -> new KeyValue<>(kv.getKey(), kv.getValue()));
    }

    @Override
    public Mono<Boolean> hset(byte[] key, byte[] field, byte[] value) {
        return hashReactiveCommands.hset(key, field, value);
    }

    @Override
    public Mono<Void> hmset(byte[] key, Map<byte[], byte[]> map) {
        // TODO 可能失败？
        return hashReactiveCommands.hmset(key, map).then();
    }

    @Override
    public Mono<Long> hdel(byte[] key, byte[]... fields) {
        return hashReactiveCommands.hdel(key, fields);
    }

    @Override
    public Mono<Long> del(byte[]... keys) {
        return keyReactiveCommands.del(keys);
    }

    @Override
    public Mono<Void> reactiveClose() {
        if (null != statefulConnection) {
            return Mono.fromFuture(statefulConnection.closeAsync().thenCompose(v -> abstractRedisClient.shutdownAsync()));
        }
        return Mono.empty();
    }

    private void isSetSuccess(byte[] key, byte[] value, String result) {
        if (!Objects.equals(OK, result)) {
            RuntimeException e = new RuntimeException("redis set error. key=" + new String(key) + ", value=" + new String(value));
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

}
