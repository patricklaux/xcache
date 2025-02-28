package com.igeeksky.xcache.redis.store;


import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.Store;
import com.igeeksky.xredis.common.RedisFutureHelper;
import com.igeeksky.xredis.common.RedisOperationException;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Redis 缓存存储
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/20
 */
public abstract class RedisStore<V> implements Store<V> {

    private static final String OK = "OK";

    private final long batchTimeout;

    /**
     * 创建 RedisStore
     *
     * @param batchTimeout 同步操作超时（毫秒）
     */
    public RedisStore(long batchTimeout) {
        this.batchTimeout = batchTimeout;
    }

    @Override
    public CacheValue<V> getCacheValue(String key) {
        return RedisFutureHelper.get(this.asyncGetCacheValue(key), batchTimeout);
    }

    @Override
    public Map<String, CacheValue<V>> getAllCacheValues(Set<? extends String> keys) {
        return RedisFutureHelper.get(this.asyncGetAllCacheValues(keys), batchTimeout);
    }

    @Override
    public void put(String key, V value) {
        RedisFutureHelper.get(this.asyncPut(key, value), batchTimeout);
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> keyValues) {
        RedisFutureHelper.get(this.asyncPutAll(keyValues), batchTimeout);
    }

    @Override
    public void remove(String key) {
        RedisFutureHelper.get(this.asyncRemove(key), batchTimeout);
    }

    @Override
    public void removeAll(Set<? extends String> keys) {
        RedisFutureHelper.get(this.asyncRemoveAll(keys), batchTimeout);
    }

    protected static Void checkResult(String result) {
        if (!Objects.equals(OK, result)) {
            throw new RedisOperationException(String.format("redis [%s] error", "asyncPutAll"));
        }
        return null;
    }

    protected static <V> Void checkResult(String result, String key, V value) {
        if (!Objects.equals(OK, result)) {
            String msg = String.format("redis [%s] error. key:[%s] value:[%s]", "asyncPut", key, value);
            throw new RedisOperationException(msg);
        }
        return null;
    }

}
