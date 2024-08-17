package com.igeeksky.xcache.extension.compress;

import com.igeeksky.xtool.core.lang.compress.Compressor;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
public interface CompressorProvider {

    Compressor get(CompressConfig config);

}
