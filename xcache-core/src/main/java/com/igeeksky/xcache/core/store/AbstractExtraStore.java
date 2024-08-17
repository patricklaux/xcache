package com.igeeksky.xcache.core.store;

import com.igeeksky.xcache.NullValue;
import com.igeeksky.xcache.Store;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.CacheValues;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.compress.Compressor;

import java.util.Arrays;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/13
 */
public abstract class AbstractExtraStore<V> implements Store<V> {

    private final boolean enableNullValue;

    private final boolean enableCompressValue;

    private final Compressor valueCompressor;

    private final Codec<V> valueCodec;

    private final byte[] null_bytes;

    public AbstractExtraStore(boolean enableNullValue, boolean enableCompressValue,
                              Compressor valueCompressor, Codec<V> valueCodec) {
        this.enableCompressValue = enableCompressValue;
        this.enableNullValue = enableNullValue;
        this.valueCompressor = valueCompressor;
        this.valueCodec = valueCodec;
        this.null_bytes = enableCompressValue ? valueCompressor.compress(NullValue.INSTANCE_BYTES) : NullValue.INSTANCE_BYTES;
    }

    protected byte[] toExtraStoreValue(V value) {
        if (null == value) {
            if (enableNullValue) {
                return null_bytes;
            }
            return null;
        }

        byte[] storeValue = valueCodec.encode(value);
        if (enableCompressValue) {
            return valueCompressor.compress(storeValue);
        }

        return storeValue;
    }

    protected CacheValue<V> fromExtraStoreValue(byte[] storeValue) {
        if (storeValue == null) {
            return null;
        }

        if (enableNullValue) {
            if (Arrays.equals(null_bytes, storeValue)) {
                return CacheValues.emptyCacheValue();
            }
        }

        if (enableCompressValue) {
            storeValue = valueCompressor.decompress(storeValue);
        }

        return CacheValues.newCacheValue(valueCodec.decode(storeValue));
    }

}