package com.igeeksky.xcache.extension.compress;

import com.igeeksky.xcache.common.Provider;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
public interface CompressorProvider extends Provider {

    Compressor get();

    @Override
    default void close() throws Exception {
        // do nothing
    }

}
