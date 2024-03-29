/*
 * Copyright 2017 Tony.lau All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igeeksky.xcache.serialization.jackson;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.igeeksky.xcache.extension.serialization.SerializationFailedException;
import com.igeeksky.xcache.extension.serialization.ValueSerializer;
import com.igeeksky.xcache.util.BytesUtils;
import com.igeeksky.xcache.util.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 0.0.1 2017-03-06
 */
public class GenericJackson2JsonSerializer implements ValueSerializer<Object> {

    private final ObjectMapper mapper;

    private final Charset charset;

    public GenericJackson2JsonSerializer() {
        this(null, null, null);
    }

    public GenericJackson2JsonSerializer(Charset charset) {
        this(null, charset, null);
    }

    public GenericJackson2JsonSerializer(ObjectMapper mapper) {
        this(mapper, null, null);
    }

    public GenericJackson2JsonSerializer(ObjectMapper mapper, Charset charset, String classPropertyTypeName) {
        this.mapper = (null != mapper ? mapper : new ObjectMapper());
        this.charset = (null != charset ? charset : StandardCharsets.UTF_8);
        if (StringUtils.isNotEmpty(classPropertyTypeName)) {
            this.mapper.enableDefaultTypingAsProperty(DefaultTyping.NON_FINAL, classPropertyTypeName);
        } else {
            this.mapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY);
        }
    }

    @Override
    public byte[] serialize(Object source) {
        if (source instanceof String || source instanceof Number) {
            return source.toString().getBytes(charset);
        }
        try {
            return mapper.writeValueAsBytes(source);
        } catch (JsonProcessingException e) {
            throw new SerializationFailedException("Could not write JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public Object deserialize(byte[] source) {
        return deserialize(source, Object.class);
    }

    @Override
    public <T> T deserialize(byte[] source, Class<T> type) {
        Objects.requireNonNull(mapper, "Deserialization type must not be null! ");

        if (BytesUtils.isEmpty(source)) {
            return null;
        }
        try {
            return mapper.readValue(source, type);
        } catch (Exception e) {
            throw new SerializationFailedException("Could not write JSON: " + e.getMessage(), e);
        }
    }

}
