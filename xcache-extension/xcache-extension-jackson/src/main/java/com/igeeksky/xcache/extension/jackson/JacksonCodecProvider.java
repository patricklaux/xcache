package com.igeeksky.xcache.extension.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igeeksky.xcache.extension.codec.CodecConfig;
import com.igeeksky.xcache.extension.codec.CodecProvider;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.codec.KeyCodec;

import java.nio.charset.Charset;
import java.util.*;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-22
 */
public class JacksonCodecProvider implements CodecProvider {

    private final ObjectMapper mapper = createMapper();
    private static final JacksonCodecProvider INSTANCE = new JacksonCodecProvider();

    public static JacksonCodecProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public <K> KeyCodec<K> doGetKeyCodec(CodecConfig<K> config) {
        JavaType javaType = getJavaType(mapper, config.getType(), config.getParams());
        return new JacksonKeyCodec<>(mapper, javaType);
    }

    @Override
    public <V> Codec<V> doGetCodec(CodecConfig<V> config) {
        JavaType javaType = getJavaType(mapper, config.getType(), config.getParams());
        return new JacksonCodec<>(mapper, javaType);
    }

    @Override
    public <T> Codec<Set<T>> getSetCodec(Charset charset, Class<T> type) {
        JavaType javaType = getJavaType(mapper, LinkedHashSet.class, new Class[]{type});
        return new JacksonCodec<>(mapper, javaType);
    }

    @Override
    public <T> Codec<Set<T>> getListCodec(Charset charset, Class<T> type) {
        JavaType javaType = getJavaType(mapper, LinkedList.class, new Class[]{type});
        return new JacksonCodec<>(mapper, javaType);
    }

    @Override
    public <K, V> Codec<Map<K, V>> getMapCodec(Charset charset, Class<K> keyType, Class<V> valType) {
        JavaType javaType = getJavaType(mapper, LinkedHashMap.class, new Class[]{keyType, valType});
        return new JacksonCodec<>(mapper, javaType);
    }

    private static ObjectMapper createMapper() {
        return new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private static <K> JavaType getJavaType(ObjectMapper mapper, Class<K> type, Class<?>[] params) {
        if (params == null || params.length == 0) {
            return mapper.getTypeFactory().constructType(type);
        } else {
            return mapper.getTypeFactory().constructParametricType(type, params);
        }
    }

}