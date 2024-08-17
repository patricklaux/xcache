package com.igeeksky.xcache.autoconfigure.redis;


import com.igeeksky.redis.RedisOperatorFactory;
import com.igeeksky.xcache.autoconfigure.holder.Holder;
import com.igeeksky.xtool.core.io.IOUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-29
 */
public class RedisOperatorFactoryHolder implements Holder<RedisOperatorFactory>, AutoCloseable {

    private final Map<String, RedisOperatorFactory> map = new HashMap<>();

    @Override
    public void put(String beanId, RedisOperatorFactory provider) {
        map.put(beanId, provider);
    }

    @Override
    public RedisOperatorFactory get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, RedisOperatorFactory> getAll() {
        return Collections.unmodifiableMap(map);
    }

    @Override
    public void close() {
        map.forEach((k, factory) -> IOUtils.closeQuietly(factory));
    }
}
