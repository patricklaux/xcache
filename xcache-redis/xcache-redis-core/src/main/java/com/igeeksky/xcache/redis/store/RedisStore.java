package com.igeeksky.xcache.redis.store;

import com.igeeksky.redis.RedisOperationException;
import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.common.Store;

import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/20
 */
public interface RedisStore<V> extends Store<V> {

    String OK = RedisOperator.OK;

    default void checkResult(String result, String cmd) {
        if (!Objects.equals(OK, result)) {
            throw new RedisOperationException(String.format("redis [%s] error", cmd));
        }
    }

    default void checkResult(String result, String cmd, String key, V value) {
        if (!Objects.equals(OK, result)) {
            String msg = String.format("redis [%s] error. key:[%s] value:[%s]", cmd, key, value);
            throw new RedisOperationException(msg);
        }
    }

}
