package com.igeeksky.xcache.autoconfigure.redis;


import com.igeeksky.xcache.autoconfigure.holder.Holder;
import com.igeeksky.xcache.redis.RedisConnectionFactory;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-29
 */
public class RedisConnectionFactoryHolder implements Holder<RedisConnectionFactory> {

    private final Map<String, RedisConnectionFactory> map = new HashMap<>();

    @Override
    public void put(String beanId, RedisConnectionFactory provider) {
        map.put(beanId, provider);
    }

    @Override
    public RedisConnectionFactory get(String beanId) {
        RedisConnectionFactory factory = map.get(beanId);
        Assert.notNull(factory, "beanId:[" + beanId + "] RedisConnectionFactory doesn't exist");
        return factory;
    }

    @Override
    public Map<String, RedisConnectionFactory> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
