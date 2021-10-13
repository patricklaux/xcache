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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.igeeksky.xcache.event.CacheLoadAllEvent;
import com.igeeksky.xcache.extension.serialization.SerializationFailedException;
import com.igeeksky.xcache.extension.serialization.ValueSerializer;
import com.igeeksky.xcache.util.BytesUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * @author Patrick.Lau
 * @since 0.0.1 2017-03-06 20:38:33
 */
public class Jackson2JsonSerializer<T> implements ValueSerializer<T> {

    private final ObjectMapper mapper;

    private JavaType javaType;

    private final TypeReference<?> typeReference;

    public Jackson2JsonSerializer(Class<T> classType) {
        this(new ObjectMapper(), null);
    }

    public Jackson2JsonSerializer(TypeReference<?> typeReference) {
        this(new ObjectMapper(), typeReference);
    }

    public Jackson2JsonSerializer(ObjectMapper mapper, TypeReference<?> typeReference) {
        Objects.requireNonNull(mapper, "ObjectMapper must not be null");
        //Objects.requireNonNull(classType, "classType must not be null");
        this.mapper = mapper;
        this.typeReference = typeReference;
    }

    public static void main(String[] args) throws IOException {
        TypeReference<CacheLoadAllEvent<UserKey, UserValue>> typeReference = new TypeReference<CacheLoadAllEvent<UserKey, UserValue>>() {
        };

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addKeyDeserializer(UserKey.class, new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext context)  {
                Jackson2JsonSerializer<UserKey> serializer = new Jackson2JsonSerializer<>(new TypeReference<UserKey>() {
                });
                return serializer.deserialize(key.getBytes(StandardCharsets.UTF_8));
            }
        });
        //simpleModule.registerSubtypes(UserKey.class, UserValue.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(simpleModule);

        Jackson2JsonSerializer<CacheLoadAllEvent<UserKey, UserValue>> serializer = new Jackson2JsonSerializer<>(typeReference);


        UserKey key = new UserKey("aa", 1);
        UserValue value = new UserValue("aa", 1);
        Map<UserKey, UserValue> keyValues = new HashMap<>();
        keyValues.put(key, value);
        CacheLoadAllEvent<UserKey, UserValue> loadAllEvent = new CacheLoadAllEvent<>();
        loadAllEvent.setKeyValues(keyValues);

        byte[] bytes = serializer.serialize(loadAllEvent);
        System.out.println(new String(bytes));

        CacheLoadAllEvent<UserKey, UserValue> loadAllEvent1 = objectMapper.readValue(bytes, typeReference);
        System.out.println(loadAllEvent1.getMillis());
        Map<? extends UserKey, ? extends UserValue> keyValues1 = loadAllEvent1.getKeyValues();
        keyValues1.forEach((userKey, userValue) -> {
            System.out.println(userKey);
            System.out.println(userValue);
        });

    }

    @Override
    public byte[] serialize(Object obj) {
        if (obj instanceof String) {
            return ((String) obj).getBytes(StandardCharsets.UTF_8);
        }
        try {
            return mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new SerializationFailedException("Could not write JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public T deserialize(byte[] source) {
        if (BytesUtils.isEmpty(source)) {
            return null;
        }
        try {
            return mapper.readValue(source, typeReference);
        } catch (Exception e) {
            throw new SerializationFailedException("Could not write JSON: " + e.getMessage(), e);
        }
    }

    protected JavaType getJavaType(Class<?> clazz) {
        return TypeFactory.defaultInstance().constructType(clazz);
    }

    @Override
    public <R> R deserialize(byte[] source, Class<R> type) {
        if (BytesUtils.isEmpty(source)) {
            return null;
        }
        try {
            return mapper.readValue(source, getJavaType(type));
        } catch (Exception e) {
            throw new SerializationFailedException("Could not write JSON: " + e.getMessage(), e);
        }
    }

    private static class UserKey {
        private String name;
        private int age;

        public UserKey() {
        }

        public UserKey(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserKey userKey = (UserKey) o;
            return age == userKey.age && Objects.equals(name, userKey.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }

        @Override
        public String toString() {
            return "{\"name\":\"" +
                    getName() +
                    "\", \"age\":" +
                    getAge() +
                    "}";
        }
    }

    private static class UserValue {
        private String name;
        private int age;

        public UserValue() {
        }

        public UserValue(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "{\"name\":\"" +
                    getName() +
                    "\", \"age\":" +
                    getAge() +
                    "}";
        }
    }

}
