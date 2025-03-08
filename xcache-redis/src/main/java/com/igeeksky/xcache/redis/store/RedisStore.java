package com.igeeksky.xcache.redis.store;


import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.Store;
import com.igeeksky.xredis.common.RedisHelper;
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

    private final long timeout;

    /**
     * 创建 RedisStore
     *
     * @param timeout 同步操作超时（毫秒）
     */
    public RedisStore(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public CacheValue<V> getCacheValue(String key) {
        return RedisHelper.get(this.getCacheValueAsync(key), timeout);
    }

    @Override
    public Map<String, CacheValue<V>> getAllCacheValues(Set<? extends String> keys) {
        return RedisHelper.get(this.getAllCacheValuesAsync(keys), timeout);
    }

    @Override
    public void put(String key, V value) {
        RedisHelper.get(this.putAsync(key, value), timeout);
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> keyValues) {
        RedisHelper.get(this.putAllAsync(keyValues), timeout);
    }

    @Override
    public void remove(String key) {
        RedisHelper.get(this.removeAsync(key), timeout);
    }

    @Override
    public void removeAll(Set<? extends String> keys) {
        RedisHelper.get(this.removeAllAsync(keys), timeout);
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
