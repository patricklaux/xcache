package com.igeeksky.xcache.serialization.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igeeksky.xcache.extension.convertor.KeyConvertor;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-15
 */
public class JacksonKeyConvertor implements KeyConvertor {

    private static final JacksonKeyConvertor INSTANCE = new JacksonKeyConvertor();

    private final ObjectMapper objectMapper;

    public JacksonKeyConvertor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JacksonKeyConvertor() {
        this.objectMapper = new ObjectMapper();
    }

    public static JacksonKeyConvertor getInstance() {
        return INSTANCE;
    }

    @Override
    public String doApply(Object original) {
        try {
            return objectMapper.writeValueAsString(original);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
