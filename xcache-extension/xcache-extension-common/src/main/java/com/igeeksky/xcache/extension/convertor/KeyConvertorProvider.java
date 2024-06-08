package com.igeeksky.xcache.extension.convertor;

import com.igeeksky.xcache.common.Provider;

import java.nio.charset.Charset;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
public interface KeyConvertorProvider extends Provider {

    KeyConvertor get(Charset charset);

    @Override
    default void close() {
        // do nothing
    }

}
