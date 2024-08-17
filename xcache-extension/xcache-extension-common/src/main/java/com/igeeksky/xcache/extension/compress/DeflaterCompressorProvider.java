package com.igeeksky.xcache.extension.compress;

import com.igeeksky.xtool.core.lang.compress.Compressor;
import com.igeeksky.xtool.core.lang.compress.DeflaterCompressor;

/**
 * deflate 压缩
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/8
 */
public class DeflaterCompressorProvider implements CompressorProvider {

    private static final DeflaterCompressorProvider INSTANCE = new DeflaterCompressorProvider();

    public static DeflaterCompressorProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public Compressor get(CompressConfig config) {
        int level = config.getLevel();
        boolean nowrap = config.isNowrap();
        if (level == -1 && !nowrap) {
            return DeflaterCompressor.getInstance();
        }
        return new DeflaterCompressor(level, nowrap);
    }

}