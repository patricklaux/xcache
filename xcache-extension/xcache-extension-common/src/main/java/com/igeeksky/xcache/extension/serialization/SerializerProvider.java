package com.igeeksky.xcache.extension.serialization;

import com.igeeksky.xcache.common.Provider;
import com.igeeksky.xcache.common.SPI;

import java.nio.charset.Charset;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-21
 */
@SPI
public interface SerializerProvider extends Provider {

    /**
     * 接受对象的Class type，返回序列化器
     *
     * @param clazz 接受类型
     * @param <T>   对象类型
     * @return Serializer<T> 序列化类
     */
    <T> Serializer<T> get(Class<T> clazz, Charset charset);

}
