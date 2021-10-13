package com.igeeksky.xcache.redis.lettuce;

import com.igeeksky.xcache.common.ExpiryKeyValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.redis.RedisStringWriter;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisKeyReactiveCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.reactive.RedisStringReactiveCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.reactive.RedisAdvancedClusterReactiveCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-12
 */
public class LettuceRedisStringWriter implements RedisStringWriter {

    private static final Logger logger = LoggerFactory.getLogger(LettuceRedisStringWriter.class);

    private final AbstractRedisClient abstractRedisClient;
    private final StatefulConnection<byte[], byte[]> statefulConnection;
    private final RedisStringReactiveCommands<byte[], byte[]> stringReactiveCommands;
    private final RedisKeyReactiveCommands<byte[], byte[]> keyReactiveCommands;

    public LettuceRedisStringWriter(AbstractRedisClient abstractRedisClient) {
        this.abstractRedisClient = abstractRedisClient;
        if (abstractRedisClient instanceof RedisClusterClient) {
            RedisClusterClient redisClusterClient = (RedisClusterClient) abstractRedisClient;
            StatefulRedisClusterConnection<byte[], byte[]> connection = redisClusterClient.connect(new ByteArrayCodec());
            this.statefulConnection = connection;
            RedisAdvancedClusterReactiveCommands<byte[], byte[]> reactiveCommands = connection.reactive();
            this.stringReactiveCommands = reactiveCommands;
            this.keyReactiveCommands = reactiveCommands;
        } else if (abstractRedisClient instanceof RedisClient) {
            RedisClient redisClient = (RedisClient) abstractRedisClient;
            StatefulRedisConnection<byte[], byte[]> connection = redisClient.connect(new ByteArrayCodec());
            this.statefulConnection = connection;
            RedisReactiveCommands<byte[], byte[]> reactiveCommands = connection.reactive();
            this.stringReactiveCommands = reactiveCommands;
            this.keyReactiveCommands = reactiveCommands;
        } else {
            throw new UnsupportedOperationException("Unsupported Redis Client.");
        }
    }

    @Override
    public Mono<byte[]> get(byte[] key) {
        return stringReactiveCommands.get(key);
    }

    @Override
    public Flux<KeyValue<byte[], byte[]>> mget(byte[]... keys) {
        return stringReactiveCommands.mget(keys)
                .map(kv -> new KeyValue<>(kv.getKey(), kv.getValue()));
    }

    @Override
    public Mono<Void> set(byte[] key, byte[] value) {
        return stringReactiveCommands.set(key, value)
                .flatMap(result -> {
                    isSetSuccess(key, value, result);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> psetex(byte[] key, long milliseconds, byte[] value) {
        return stringReactiveCommands.psetex(key, milliseconds, value)
                .flatMap(result -> {
                    isSetSuccess(key, value, result);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> mset(Map<byte[], byte[]> keyValues) {
        return stringReactiveCommands.mset(keyValues).then();
    }

    @Override
    public final Mono<Void> mpsetex(List<ExpiryKeyValue<byte[], byte[]>> keyValues) {
        Flux<String> flux = Flux.empty();
        for (ExpiryKeyValue<byte[], byte[]> keyValue : keyValues) {
            Mono<String> mono = stringReactiveCommands.psetex(keyValue.getKey(), keyValue.getTtl().toMillis(), keyValue.getValue());
            flux = flux.concatWith(mono);
        }
        return flux.then();
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
            RuntimeException e = new RuntimeException("redis set error. " + new String(key) + ", value=" + new String(value));
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

}
