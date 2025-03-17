package com.igeeksky.xcache.caffeine;

import com.github.benmanes.caffeine.cache.Expiry;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.Register;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 过期时间策略注册器
 * <p>
 * 仅适用于 Caffeine，当通过配置已经无法满足个性化需求，可实现 {@link Expiry} 接口创建自定义的过期时间策略。
 * <p>
 * <b>注意</b>：<br>
 * 此对象实例需在 {@code com.igeeksky.xcache.autoconfigure.caffeine.CaffeineAutoConfiguration} 之前注入 Spring 容器。
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
public class CaffeineExpiryRegister implements Register<Expiry<String, CacheValue<Object>>> {

    private final Map<String, Expiry<String, CacheValue<Object>>> map = new HashMap<>();

    /**
     * 注册过期时间策略
     *
     * @param name   缓存名称
     * @param expiry 过期时间策略
     */
    @Override
    public void put(String name, Expiry<String, CacheValue<Object>> expiry) {
        Expiry<String, CacheValue<Object>> old = map.put(name, expiry);
        Assert.isTrue(old == null, () -> "Caffeine:Expiry: [" + name + "] duplicate id.");
    }

    /**
     * 获取过期时间策略
     *
     * @param name 缓存名称
     * @return 过期时间策略
     */
    @Override
    public Expiry<String, CacheValue<Object>> get(String name) {
        return map.get(name);
    }

    @Override
    public Map<String, Expiry<String, CacheValue<Object>>> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
