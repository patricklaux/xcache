package com.igeeksky.xcache.core.store;

import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.CacheValues;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.compress.Compressor;


/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/13
 */
public abstract class AbstractEmbedStore<V> implements Store<V> {

    private final boolean enableNullValue;
    private final boolean enableCompressValue;
    private final boolean enableSerializeValue;

    private final Codec<V> codec;
    private final Compressor compressor;

    public AbstractEmbedStore(boolean enableNullValue, boolean enableCompressValue, boolean enableSerializeValue,
                              Compressor compressor, Codec<V> codec) {
        this.enableNullValue = enableNullValue;
        this.enableCompressValue = enableCompressValue;
        this.enableSerializeValue = enableSerializeValue;
        this.compressor = compressor;
        this.codec = codec;
    }

    protected CacheValue<Object> toStoreValue(V value) {
        if (null == value) {
            if (enableNullValue) {
                return CacheValues.emptyCacheValue();
            }
            return null;
        }
        if (enableSerializeValue) {
            byte[] innerValue = codec.encode(value);
            if (enableCompressValue) {
                innerValue = compressor.compress(innerValue);
            }
            return CacheValues.newCacheValue(innerValue);
        }
        return CacheValues.newCacheValue(value);
    }

    @SuppressWarnings("unchecked")
    protected CacheValue<V> fromStoreValue(CacheValue<Object> storeValue) {
        if (storeValue == null) {
            return null;
        }
        if (!storeValue.hasValue()) {
            if (enableNullValue) {
                return (CacheValue<V>) storeValue;
            }
            return null;
        }
        if (enableSerializeValue) {
            byte[] innerValue = (byte[]) storeValue.getValue();
            if (enableCompressValue) {
                innerValue = compressor.decompress(innerValue);
            }
            return CacheValues.newCacheValue(codec.decode(innerValue));
        }
        return (CacheValue<V>) storeValue;
    }

}