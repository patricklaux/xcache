package com.igeeksky.xcache.redis.store;

import com.igeeksky.redis.RedisOperationException;
import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.core.store.AbstractExtraStore;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.compress.Compressor;

import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/20
 */
public abstract class AbstractRedisStore<V> extends AbstractExtraStore<V> {

    private static final String OK = RedisOperator.OK;

    public AbstractRedisStore(boolean enableNullValue, boolean enableCompressValue,
                              Compressor valueCompressor, Codec<V> valueCodec) {
        super(enableNullValue, enableCompressValue, valueCompressor, valueCodec);
    }

    protected static void checkResult(String result, String cmd) {
        if (!Objects.equals(OK, result)) {
            throw new RedisOperationException(String.format("redis [%s] error", cmd));
        }
    }

    protected void checkResult(String result, String cmd, String key, V value) {
        if (!Objects.equals(OK, result)) {
            String msg = String.format("redis [%s] error. key:[%s] value:[%s]", cmd, key, value);
            throw new RedisOperationException(msg);
        }
    }

}
