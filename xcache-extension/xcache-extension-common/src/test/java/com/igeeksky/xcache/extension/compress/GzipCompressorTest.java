package com.igeeksky.xcache.extension.compress;

import com.igeeksky.xtool.core.lang.RandomUtils;
import org.junit.jupiter.api.Test;

/**
 * @author Patrick.Lau
 * @version 1.0
 * @since 0.0.4 2021-06-23
 */
class GzipCompressorTest {

    final int size = 100;

    @Test
    void compress() {
        byte[] source = loadFile();
        System.out.println("File length: " + source.length);
        GzipCompressor gzipCompressor = new GzipCompressor();
        long start = System.currentTimeMillis();
        byte[] compress = null;
        for (int i = 0; i < size; i++) {
            compress = gzipCompressor.compress(source);
            if ((i & 63) == 0) {
                System.out.println(compress.length);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);

        byte[] decompress = gzipCompressor.decompress(compress);
        System.out.println("decompress.length:" + decompress.length);
    }

    private byte[] loadFile() {
        byte[] out = new byte[1024 * 1024];
        RandomUtils.nextBytes(out);
        return out;
    }

}