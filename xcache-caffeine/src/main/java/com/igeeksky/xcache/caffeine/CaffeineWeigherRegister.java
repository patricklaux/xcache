package com.igeeksky.xcache.caffeine;

import com.github.benmanes.caffeine.cache.Weigher;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.Register;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 权重计算策略注册器
 * <p>
 * 仅适用于 Caffeine，当通过配置已经无法满足个性化需求，可实现 {@link Weigher} 接口创建自定义的过期时间策略。
 * <p>
 * <b>注意</b>：<br>
 * 此对象实例需在 {@code com.igeeksky.xcache.autoconfigure.caffeine.CaffeineAutoConfiguration} 之前注入 Spring 容器。
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
public class CaffeineWeigherRegister implements Register<Weigher<String, CacheValue<Object>>> {

    private final Map<String, Weigher<String, CacheValue<Object>>> map = new HashMap<>();

    /**
     * 注册权重计算策略
     *
     * @param name    缓存名称
     * @param weigher 权重计算策略
     */
    @Override
    public void put(String name, Weigher<String, CacheValue<Object>> weigher) {
        Weigher<String, CacheValue<Object>> old = map.put(name, weigher);
        Assert.isTrue(old == null, () -> "Caffeine:Weigher: [" + name + "] duplicate id.");
    }

    /**
     * 获取权重计算策略
     *
     * @param name 缓存名称
     * @return 权重计算策略
     */
    @Override
    public Weigher<String, CacheValue<Object>> get(String name) {
        return map.get(name);
    }

    /**
     * 获取所有的权重计算策略
     *
     * @return {@code UnmodifiableMap} – 所有的权重计算策略
     */
    @Override
    public Map<String, Weigher<String, CacheValue<Object>>> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
