package com.igeeksky.xcache.extension.serializer;

import com.igeeksky.xcache.common.Provider;

import java.nio.charset.Charset;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-21
 */

public interface SerializerProvider extends Provider {

    /**
     * 接受对象的 Class type，返回序列化器
     *
     * @param name    缓存名称（当特殊情况无法获取正确的序列化器，可以自定义 SerializerProvider，然后通过缓存名称来获取）
     * @param charset 字符编码
     * @param type    序列化数据类型
     * @param <T>     序列化数据类型
     * @return {@link Serializer<T>} 序列化器
     */
    <T> Serializer<T> get(String name, Charset charset, Class<T> type, Class<?>[] valueParams);

    @Override
    default void close() {
        // do nothing
    }

}
