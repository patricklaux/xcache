package com.igeeksky.xcache.extension.compress;


import com.igeeksky.xtool.core.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-22
 */
public class GzipCompressor implements Compressor {

    private static final GzipCompressor INSTANCE = new GzipCompressor();

    public static GzipCompressor getInstance() {
        return INSTANCE;
    }

    @Override
    public byte[] compress(byte[] source) {
        if (null == source) {
            throw new CacheValueCompressException("compress: byte[] source must not be null");
        }
        try (ByteArrayOutputStream targetStream = new ByteArrayOutputStream(source.length / 2)) {
            try (GZIPOutputStream compressor = new GZIPOutputStream(targetStream)) {
                compressor.write(source, 0, source.length);
            }
            return targetStream.toByteArray();
        } catch (IOException e) {
            String msg = String.format("can't compress: [%s]. %s", new String(source), e.getMessage());
            throw new CacheValueCompressException(msg, e);
        }
    }

    @Override
    public byte[] decompress(byte[] source) {
        if (null == source) {
            throw new CacheValueCompressException("decompress: byte[] source must not be null");
        }
        try (ByteArrayOutputStream targetStream = new ByteArrayOutputStream(source.length * 2);
             ByteArrayInputStream sourceStream = new ByteArrayInputStream(source)) {
            try (GZIPInputStream decompressor = new GZIPInputStream(sourceStream)) {
                IOUtils.copy(decompressor, targetStream);
            }
            return targetStream.toByteArray();
        } catch (IOException e) {
            String msg = String.format("can't decompress: [%s]. %s", new String(source), e.getMessage());
            throw new CacheValueCompressException(msg, e);
        }
    }

}
