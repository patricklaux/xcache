package com.igeeksky.xcache.extension.serializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-20
 */
public class StringSerializer implements Serializer<String> {

    public static final StringSerializer UTF_8 = new StringSerializer();

    public static final StringSerializer US_ASCII = new StringSerializer(StandardCharsets.US_ASCII);

    private final Charset charset;

    public StringSerializer() {
        this(StandardCharsets.UTF_8);
    }

    public StringSerializer(Charset charset) {
        this.charset = charset;
    }

    public static StringSerializer getInstance(Charset charset) {
        if (Objects.equals(StandardCharsets.UTF_8, charset)) {
            return UTF_8;
        }
        if (Objects.equals(StandardCharsets.US_ASCII, charset)) {
            return US_ASCII;
        }
        return new StringSerializer(charset);
    }

    @Override
    public byte[] serialize(String source) {
        if (null == source) {
            throw new SerializationFailedException("source must not be null");
        }
        return source.getBytes(charset);
    }

    @Override
    public String deserialize(byte[] source) {
        if (null == source) {
            throw new SerializationFailedException("byte[] source must not be null");
        }
        return new String(source, charset);
    }

}
