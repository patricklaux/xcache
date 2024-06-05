package com.igeeksky.xcache.extension.serializer;


import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * JDK内置序列化器
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-20
 */
public class JdkSerializer<T> implements Serializer<T> {

    public static final JdkSerializer<Object> UTF_8 = new JdkSerializer<>();

    public static final JdkSerializer<Object> ASCII = new JdkSerializer<>(StandardCharsets.US_ASCII);

    private final Charset charset;

    public JdkSerializer() {
        this(StandardCharsets.UTF_8);
    }

    public JdkSerializer(Charset charset) {
        this.charset = charset;
    }

    public static JdkSerializer<Object> getInstance(Charset charset) {
        if (Objects.equals(StandardCharsets.UTF_8, charset)) {
            return UTF_8;
        } else if (Objects.equals(StandardCharsets.US_ASCII, charset)) {
            return ASCII;
        } else {
            return new JdkSerializer<>(charset);
        }
    }

    @Override
    public byte[] serialize(T obj) {
        if (null == obj) {
            throw new SerializationFailedException("obj must not be null");
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            String errMsg = String.format("can't serialize [%s]. %s", obj, e.getMessage());
            throw new SerializationFailedException(errMsg, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(byte[] source) {
        if (null == source) {
            throw new SerializationFailedException("byte[] source must not be null");
        }
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(source);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return (T) objectInputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            String errMsg = String.format("can't deserialize [%s]. %s", new String(source, charset), e.getMessage());
            throw new SerializationFailedException(errMsg, e);
        }
    }

}
