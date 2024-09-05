package com.igeeksky.xcache.extension.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.igeeksky.xtool.core.lang.StringUtils;
import com.igeeksky.xtool.core.lang.codec.CodecException;
import com.igeeksky.xtool.core.lang.codec.KeyCodec;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用的序列化器
 * <p>
 * 生成的 Json 对象包含 Java 类型信息，适用于 Spring Cache
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/10
 */
public class GenericJacksonKeyCodec implements KeyCodec<Object> {

    private static final GenericJacksonKeyCodec INSTANCE = new GenericJacksonKeyCodec(new ObjectMapper(), null);

    private final ObjectMapper mapper;
    private final Map<JavaTypeInfo, JavaType> typeCache = new ConcurrentHashMap<>();

    public GenericJacksonKeyCodec(ObjectMapper mapper, String propertyName) {
        Objects.requireNonNull(mapper, "ObjectMapper must not be null!");
        this.mapper = mapper;
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        PolymorphicTypeValidator ptv = this.mapper.getPolymorphicTypeValidator();
        if (StringUtils.hasText(propertyName)) {
            mapper.activateDefaultTypingAsProperty(ptv, DefaultTyping.NON_FINAL, propertyName);
        } else {
            mapper.activateDefaultTyping(ptv, DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        }
    }

    public static GenericJacksonKeyCodec getInstance() {
        return INSTANCE;
    }

    public void registerModule(Module module) {
        this.mapper.registerModule(module);
    }

    @Override
    public String encode(Object value) {
        if (value == null) {
            throw new CodecException("Value must not be null!");
        }
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            String msg = String.format("Unable to write JSON: [%s]. %s", value, e.getMessage());
            throw new CodecException(msg, e);
        }
    }

    @Override
    public Object decode(String source) {
        return decode(source, Object.class, null);
    }

    public Object decode(String source, Class<?> type, Class<?>[] params) {
        if (null == source) {
            throw new CodecException("byte[] source must not be null");
        }
        if (type == null) {
            throw new CodecException("Class<?> type must not be null");
        }
        try {
            return mapper.readValue(source, getJavaType(type, params));
        } catch (IOException e) {
            String msg = String.format("Unable to read from JSON:[%s]. %s", source, e.getMessage());
            throw new CodecException(msg, e);
        }
    }

    private JavaType getJavaType(Class<?> type, Class<?>[] params) {
        return typeCache.computeIfAbsent(new JavaTypeInfo(type, params), key -> {
            if (params == null || params.length == 0) {
                return mapper.getTypeFactory().constructType(type);
            }
            return mapper.getTypeFactory().constructParametricType(type, params);
        });
    }

}