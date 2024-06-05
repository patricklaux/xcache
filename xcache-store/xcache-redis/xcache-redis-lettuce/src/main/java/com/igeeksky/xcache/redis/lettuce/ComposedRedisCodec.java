package com.igeeksky.xcache.redis.lettuce;

import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-14
 */
public class ComposedRedisCodec implements RedisCodec<String, byte[]> {

    public static final ComposedRedisCodec UTF_8 = new ComposedRedisCodec(StandardCharsets.UTF_8);

    public static final ComposedRedisCodec ASCII = new ComposedRedisCodec(StandardCharsets.US_ASCII);

    private final StringCodec keyCodec;

    private final ByteArrayCodec valueCodec = ByteArrayCodec.INSTANCE;

    public ComposedRedisCodec(Charset charset) {
        this.keyCodec = getKeyCodec(charset);
    }

    public static ComposedRedisCodec getInstance(Charset charset) {
        if (Objects.equals(StandardCharsets.UTF_8, charset)) {
            return UTF_8;
        } else if (Objects.equals(StandardCharsets.US_ASCII, charset)) {
            return ASCII;
        }
        return new ComposedRedisCodec(charset);
    }

    private static StringCodec getKeyCodec(Charset charset) {
        if (Objects.equals(StandardCharsets.UTF_8, charset)) {
            return StringCodec.UTF8;
        } else if (Objects.equals(StandardCharsets.US_ASCII, charset)) {
            return StringCodec.ASCII;
        }
        return new StringCodec(charset);
    }

    @Override
    public String decodeKey(ByteBuffer bytes) {
        return keyCodec.decodeKey(bytes);
    }

    @Override
    public byte[] decodeValue(ByteBuffer bytes) {
        return valueCodec.decodeValue(bytes);
    }

    @Override
    public ByteBuffer encodeKey(String key) {
        return keyCodec.encodeKey(key);
    }

    @Override
    public ByteBuffer encodeValue(byte[] value) {
        return valueCodec.encodeValue(value);
    }
}
