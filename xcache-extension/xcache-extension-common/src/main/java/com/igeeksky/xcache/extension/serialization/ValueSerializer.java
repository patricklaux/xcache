package com.igeeksky.xcache.extension.serialization;

/**
 * @author Patrick.Lau
 * @date 2017-03-07 06:49:37
 */
public interface ValueSerializer<V> extends Serializer<V> {

    <T> T deserialize(byte[] source, Class<T> type);

}
