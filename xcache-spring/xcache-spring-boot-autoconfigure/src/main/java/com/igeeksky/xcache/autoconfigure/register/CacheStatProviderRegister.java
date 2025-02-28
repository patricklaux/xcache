package com.igeeksky.xcache.autoconfigure.register;

import com.igeeksky.xcache.extension.stat.CacheStatProvider;
import com.igeeksky.xtool.core.io.IOUtils;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 用于向 CacheManager 注册 CacheStatProvider
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheStatProviderRegister implements Register<Supplier<CacheStatProvider>>, AutoCloseable {

    private final Map<String, Supplier<CacheStatProvider>> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 CacheStatProvider
     */
    public CacheStatProviderRegister() {
    }

    @Override
    public void put(String beanId, Supplier<CacheStatProvider> provider) {
        Supplier<CacheStatProvider> old = map.put(beanId, provider);
        Assert.isTrue(old == null, () -> "CacheStatProvider: [" + beanId + "] duplicate id.");
    }

    @Override
    public Supplier<CacheStatProvider> get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, Supplier<CacheStatProvider>> getAll() {
        return Collections.unmodifiableMap(map);
    }

    @Override
    public void close() {
        map.forEach((name, provider) -> IOUtils.closeQuietly(provider.get()));
    }

}