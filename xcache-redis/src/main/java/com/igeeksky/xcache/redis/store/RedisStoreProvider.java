package com.igeeksky.xcache.redis.store;


import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.core.store.StoreConfig;
import com.igeeksky.xcache.core.store.StoreProvider;
import com.igeeksky.xcache.props.RedisType;
import com.igeeksky.xredis.common.RedisOperatorProxy;

import java.util.Objects;

/**
 * RedisStore 工厂类
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-27
 */
public class RedisStoreProvider implements StoreProvider {

    private final long batchTimeout;
    private final RedisOperatorProxy operator;

    public RedisStoreProvider(RedisOperatorProxy operator, long batchTimeout) {
        this.operator = operator;
        this.batchTimeout = batchTimeout;
    }

    @Override
    public <V> Store<V> getStore(StoreConfig<V> storeConfig) {
        RedisStoreConfig<V> config = new RedisStoreConfig<>(storeConfig, batchTimeout);
        RedisType redisType = config.getRedisType();
        if (redisType == null || Objects.equals(RedisType.STRING, redisType)) {
            return new RedisStringStore<>(this.operator, config);
        }
        if (this.operator.isCluster()) {
            return new RedisClusterHashStore<>(this.operator, config);
        }
        return new RedisHashStore<>(this.operator, config);
    }

}
