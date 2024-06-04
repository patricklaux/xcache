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
import com.igeeksky.xcache.extension.serialization.Serializer;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Patrick.Lau
 * @date 2017-03-05 08:41:27
 */
public class StringKeySerializer implements Serializer<Object> {

    private final Charset charset;

    private final ObjectMapper mapper;

    public StringKeySerializer() {
        this(null, null, null);
    }

    public StringKeySerializer(Charset charset) {
        this(null, charset, null);
    }

    public StringKeySerializer(ObjectMapper mapper) {
        this(mapper, null, null);
    }

    public StringKeySerializer(ObjectMapper mapper, Charset charset, String classPropertyTypeName) {
        this.mapper = (null == mapper ? new ObjectMapper() : mapper);
        this.charset = (null == charset ? StandardCharsets.UTF_8 : charset);
        if (StringUtils.hasLength(classPropertyTypeName)) {
            this.mapper.enableDefaultTypingAsProperty(DefaultTyping.NON_FINAL, classPropertyTypeName);
        } else {
            this.mapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY);
        }
    }

    @Override
    public byte[] serialize(Object source) {
        if (source instanceof String || source instanceof Number) {
            String str = source.toString();
            return str.getBytes(charset);
        }
        try {
            return mapper.writeValueAsBytes(source);
        } catch (JsonProcessingException e) {
            throw new SerializationFailedException("Could not write JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public String deserialize(byte[] bytes) {
        if (null == bytes || bytes.length == 0) return null;
        return new String(bytes, charset);
    }

}
