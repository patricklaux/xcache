package com.igeeksky.xcache.redis.store;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.redis.RedisOperatorFactory;
import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.core.store.StoreConfig;
import com.igeeksky.xcache.core.store.StoreProvider;
import com.igeeksky.xcache.props.RedisType;

import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-27
 */
public class RedisStoreProvider implements StoreProvider {

    private final RedisOperator operator;

    public RedisStoreProvider(RedisOperatorFactory factory) {
        this.operator = factory.getRedisOperator();
    }

    @Override
    public <V> Store<V> getStore(StoreConfig<V> storeConfig) {
        RedisStoreConfig<V> config = new RedisStoreConfig<>(storeConfig);
        RedisType redisType = config.getRedisType();
        if (redisType == null || Objects.equals(RedisType.STRING, redisType)) {
            return new RedisStringStore<>(this.operator, config);
        }
        return new RedisHashStore<>(this.operator, config);
    }

}
