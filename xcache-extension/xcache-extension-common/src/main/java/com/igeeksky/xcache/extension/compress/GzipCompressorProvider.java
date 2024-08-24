package com.igeeksky.xcache.extension.compress;

import com.igeeksky.xtool.core.lang.compress.Compressor;
import com.igeeksky.xtool.core.lang.compress.GzipCompressor;

/**
 * gzip 压缩
 * <p>
 * <b>注意</b>：level 和 nowrap 配置无效，无法调整压缩级别
 * <p>
 * 如需调整压缩级别，请用 {@link DeflaterCompressorProvider}
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-19
 */
public class GzipCompressorProvider implements CompressorProvider {

    private static final GzipCompressorProvider INSTANCE = new GzipCompressorProvider();

    public static GzipCompressorProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public Compressor get(CompressConfig config) {
        return GzipCompressor.getInstance();
    }

}