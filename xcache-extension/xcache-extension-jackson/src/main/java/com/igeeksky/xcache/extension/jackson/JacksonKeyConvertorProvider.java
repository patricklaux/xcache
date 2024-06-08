package com.igeeksky.xcache.extension.jackson;

import com.igeeksky.xcache.extension.convertor.KeyConvertor;
import com.igeeksky.xcache.extension.convertor.KeyConvertorProvider;

import java.nio.charset.Charset;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
public class JacksonKeyConvertorProvider implements KeyConvertorProvider {

    public static final JacksonKeyConvertorProvider INSTANCE = new JacksonKeyConvertorProvider();

    @Override
    public KeyConvertor get(Charset charset) {
        return JacksonKeyConvertor.getInstance();
    }

}
