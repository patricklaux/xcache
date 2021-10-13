package com.igeeksky.xcache.extension.serialization;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-20
 */
public class StringSerializer implements Serializer<String> {

    public static final StringSerializer UTF_8 = new StringSerializer();

    public static final StringSerializer US_ASCII = new StringSerializer(StandardCharsets.US_ASCII);

    private static final ConcurrentMap<Charset, StringSerializer> STRING_SERIALIZER_MAP = new ConcurrentHashMap<>();

    private final Charset charset;

    public StringSerializer() {
        this(StandardCharsets.UTF_8);
    }

    public StringSerializer(Charset charset) {
        this.charset = charset;
    }

    @Override
    public byte[] serialize(String source) {
        return source.getBytes(charset);
    }

    @Override
    public String deserialize(byte[] bytes) {
        return new String(bytes, charset);
    }

    public static class StringSerializerProvider implements SerializerProvider {

        @SuppressWarnings("unchecked")
        @Override
        public <T> Serializer<T> get(Class<T> clazz, Charset charset) {
            if (Objects.equals(String.class, clazz)) {
                if (Objects.equals(StandardCharsets.UTF_8, charset)) {
                    return (Serializer<T>) UTF_8;
                }
                if (Objects.equals(StandardCharsets.US_ASCII, charset)) {
                    return (Serializer<T>) US_ASCII;
                }
                return (Serializer<T>) STRING_SERIALIZER_MAP.computeIfAbsent(charset, key -> new StringSerializer(charset));
            }
            return null;
        }

    }
}
