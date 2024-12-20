package com.igeeksky.xcache.core;

import com.igeeksky.xcache.NullValue;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.CacheValues;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.compress.Compressor;

import java.util.Arrays;

/**
 * 外部缓存键值转换器
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/18
 */
public class ExtraStoreConvertor<V> {

    private final boolean enableNullValue;

    private final boolean enableCompressValue;

    private final Codec<V> codec;
    private final Compressor compressor;

    private final byte[] null_bytes;

    public ExtraStoreConvertor(boolean enableNullValue, boolean enableCompressValue,
                               Codec<V> codec, Compressor compressor) {
        this.enableCompressValue = enableCompressValue;
        this.enableNullValue = enableNullValue;
        this.codec = codec;
        this.compressor = compressor;
        this.null_bytes = enableCompressValue ? this.compressor.compress(NullValue.INSTANCE_BYTES) :
                NullValue.INSTANCE_BYTES;
    }

    public byte[] toExtraStoreValue(V value) {
        if (null == value) {
            if (enableNullValue) {
                return null_bytes;
            }
            return null;
        }

        byte[] storeValue = codec.encode(value);
        if (enableCompressValue) {
            return compressor.compress(storeValue);
        }

        return storeValue;
    }

    public CacheValue<V> fromExtraStoreValue(byte[] storeValue) {
        if (storeValue == null) {
            return null;
        }

        if (Arrays.equals(null_bytes, storeValue)) {
            if (enableNullValue) {
                return CacheValues.emptyCacheValue();
            }
            return null;
        }

        if (enableCompressValue) {
            storeValue = compressor.decompress(storeValue);
        }

        return CacheValues.newCacheValue(codec.decode(storeValue));
    }

}
