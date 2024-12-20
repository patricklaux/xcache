package com.igeeksky.xcache.redis.store;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.function.tuple.KeyValue;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class RedisHashStoreHelper<V> {

    private final StringCodec stringCodec;
    private final Function<byte[], CacheValue<V>> valueFunction;

    public RedisHashStoreHelper(StringCodec stringCodec, Function<byte[], CacheValue<V>> valueFunction) {
        this.stringCodec = stringCodec;
        this.valueFunction = valueFunction;
    }

    public Map<String, CacheValue<V>> toResult(List<KeyValue<byte[], byte[]>> keyValues) {
        if (CollectionUtils.isEmpty(keyValues)) {
            return Collections.emptyMap();
        }

        Map<String, CacheValue<V>> result = HashMap.newHashMap(keyValues.size());
        for (KeyValue<byte[], byte[]> keyValue : keyValues) {
            if (keyValue.hasValue()) {
                CacheValue<V> cacheValue = this.valueFunction.apply(keyValue.getValue());
                if (cacheValue != null) {
                    result.put(this.fromStoreField(keyValue.getKey()), cacheValue);
                }
            }
        }
        return result;
    }

    public byte[] toStoreField(String field) {
        return this.stringCodec.encode(field);
    }

    public String fromStoreField(byte[] field) {
        return this.stringCodec.decode(field);
    }

}
