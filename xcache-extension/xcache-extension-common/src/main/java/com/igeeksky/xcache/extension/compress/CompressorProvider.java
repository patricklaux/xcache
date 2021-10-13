package com.igeeksky.xcache.extension.compress;

import com.igeeksky.xcache.common.Provider;
import com.igeeksky.xcache.common.SPI;

/**
 * @author Patrick.Lau
 * @date 2021-07-26
 */
@SPI("com.igeeksky.xcache.core.extension.compress.GzipCompressor$GzipCompressorProvider")
public interface CompressorProvider extends Provider {

    Compressor get();

}
