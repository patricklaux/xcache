package com.igeeksky.xcache.redis.lettuce;

import com.igeeksky.xcache.common.ExpiryKeyValue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-17
 */
class LettuceRedisStringWriterTest {

    private final RedisURI redisURI = RedisURI.builder().withHost("127.0.0.1").withPort(6379).build();
    private final Charset UTF_8 = StandardCharsets.UTF_8;
    RedisClient redisClient = RedisClient.create(redisURI);
    StatefulRedisConnection<byte[], byte[]> connection = redisClient.connect(new ByteArrayCodec());
    RedisCommands<byte[], byte[]> redisCommands = connection.sync();
    RedisReactiveCommands<byte[], byte[]> reactiveCommands = connection.reactive();
    private final LettuceRedisStringWriter lettuceRedisStringWriter = new LettuceRedisStringWriter(redisClient);

    @Test
    void get() {
        byte[] key = "a".getBytes(UTF_8);
        byte[] value = "a".getBytes(UTF_8);
        lettuceRedisStringWriter.set(key, value).subscribe();
        lettuceRedisStringWriter.get(key).subscribe(v -> {
            String valueString = new String(v, UTF_8);
            System.out.println(valueString);
            Assertions.assertEquals("a", valueString);
        });
    }

    @Test
    void mget() {
        int size = 100000;
        byte[][] keySet = new byte[size][];
        for (int i = 0; i < size; i++) {
            byte[] bytes = ("b" + i).getBytes(UTF_8);
            keySet[i] = bytes;
        }
        long start = System.currentTimeMillis();
        lettuceRedisStringWriter.mget(keySet).collectList().doOnNext(list -> System.out.println(list.size())).then().block();
        long end = System.currentTimeMillis();
        System.out.println("1:   " + (end - start));
    }

    @Test
    void set() {
    }

    @Test
    void mset() {
    }

    @Test
    void mpsetex() {
        int size = 100000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            byte[] bytes = ("b" + i).getBytes(UTF_8);
            redisCommands.psetex(bytes, 5000000, bytes);
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    @Test
    void mpsetex2() throws InterruptedException {
        int size = 100000;
        List<ExpiryKeyValue<byte[], byte[]>> keyValues = new ArrayList<>(size);
        byte[][] keySet = new byte[size][];
        for (int i = 0; i < size; i++) {
            byte[] bytes = ("b" + i).getBytes(UTF_8);
            ExpiryKeyValue<byte[], byte[]> keyValue = new ExpiryKeyValue<>(bytes, bytes, Duration.ofMillis(500000000));
            keyValues.add(keyValue);
            keySet[i] = bytes;
        }
        long start = System.currentTimeMillis();
        lettuceRedisStringWriter.mpsetex(keyValues).subscribe();
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        Thread.sleep(15000);
        long end2 = System.currentTimeMillis();
        System.out.println(end2 - start);

        lettuceRedisStringWriter.mget(keySet);
    }

    @Test
    @SuppressWarnings("unchecked")
    void mpsetex3() throws InterruptedException {
        int size = 100000;
        ExpiryKeyValue<byte[], byte[]>[] keyValues = new ExpiryKeyValue[size];
        for (int i = 0; i < size; i++) {
            byte[] bytes = ("b" + i).getBytes(UTF_8);
            keyValues[i] = new ExpiryKeyValue<>(bytes, bytes, Duration.ofMillis(500000000));
        }

        long start = System.currentTimeMillis();
        Flux<String> flux = Flux.empty();
        for (ExpiryKeyValue<byte[], byte[]> kv : keyValues) {
            Mono<String> mono = reactiveCommands.psetex(kv.getKey(), kv.getTtl().toMillis(), kv.getValue());
            flux = flux.concatWith(mono);
        }
        flux.subscribe();
        long end = System.currentTimeMillis();
        System.out.println("2:   " + (end - start));
        Thread.sleep(15000);
        long end2 = System.currentTimeMillis();
        System.out.println(end2 - start);
    }

    @Test
    void del() {
        int size = 100000;
        byte[][] keySet = new byte[size][];
        for (int i = 0; i < size; i++) {
            keySet[i] = ("b" + i).getBytes(UTF_8);
        }

        long start = System.currentTimeMillis();
        redisCommands.del(keySet);
        long del = System.currentTimeMillis();
        System.out.println("1:   " + (del - start));
    }

    @Test
    void hget() {
    }

    @Test
    void hmget() {
    }

    @Test
    void hset() {
    }

    @Test
    void hmset() {
    }

    @Test
    void hdel() {
    }

    @Test
    void close() {
    }

    @Test
    void reactiveClose() {
    }
}