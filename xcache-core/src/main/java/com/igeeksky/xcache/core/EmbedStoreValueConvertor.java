package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.CacheValues;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.compress.Compressor;

/**
 * 内嵌缓存值转换器
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/18
 */
public class EmbedStoreValueConvertor<V> {

    private final boolean enableNullValue;
    private final boolean enableCompressValue;
    private final boolean enableSerializeValue;

    private final Codec<V> codec;
    private final Compressor compressor;

    public EmbedStoreValueConvertor(boolean enableNullValue, boolean enableCompressValue,
                                    boolean enableSerializeValue, Codec<V> codec, Compressor compressor) {
        this.enableNullValue = enableNullValue;
        this.enableCompressValue = enableCompressValue;
        this.enableSerializeValue = enableSerializeValue;
        this.compressor = compressor;
        this.codec = codec;
    }

    public CacheValue<Object> toStoreValue(V value) {
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
    public CacheValue<V> fromStoreValue(CacheValue<Object> storeValue) {
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
