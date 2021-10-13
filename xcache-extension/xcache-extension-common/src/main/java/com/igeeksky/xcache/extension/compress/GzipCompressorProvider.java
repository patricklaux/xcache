package com.igeeksky.xcache.extension.compress;

import com.igeeksky.xcache.common.Singleton;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-19
 */
@Singleton
public class GzipCompressorProvider implements CompressorProvider {

    @Override
    public Compressor get() {
        return GzipCompressor.getInstance();
    }

}