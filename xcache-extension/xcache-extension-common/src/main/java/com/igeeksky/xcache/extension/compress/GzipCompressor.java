package com.igeeksky.xcache.extension.compress;

import com.igeeksky.xcache.util.BytesUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Patrick.Lau
 * @date 2021-06-22
 */
public class GzipCompressor implements Compressor {

    private static final GzipCompressor INSTANCE = new GzipCompressor();

    public static GzipCompressor getInstance() {
        return INSTANCE;
    }

    @Override
    public byte[] compress(byte[] source) throws IOException {
        Objects.requireNonNull(source, "source must not be null");
        try (ByteArrayOutputStream targetStream = new ByteArrayOutputStream(source.length / 2)) {
            try (GZIPOutputStream compressor = new GZIPOutputStream(targetStream)) {
                compressor.write(source, 0, source.length);
            }
            return targetStream.toByteArray();
        }
    }

    @Override
    public byte[] decompress(byte[] source) throws IOException {
        Objects.requireNonNull(source, "source must not be null");
        try (ByteArrayOutputStream targetStream = new ByteArrayOutputStream(source.length * 2);
             ByteArrayInputStream sourceStream = new ByteArrayInputStream(source)) {
            try (GZIPInputStream decompressor = new GZIPInputStream(sourceStream)) {
                BytesUtils.copy(decompressor, targetStream);
            }
            return targetStream.toByteArray();
        }
    }

}
