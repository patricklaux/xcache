package com.igeeksky.xcache.autoconfigure.register;

import com.igeeksky.xcache.core.SingletonSupplier;
import com.igeeksky.xcache.extension.refresh.CacheRefreshProvider;
import com.igeeksky.xtool.core.io.IOUtils;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 用于向 CacheManager 注册 CacheRefreshProvider
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheRefreshProviderRegister implements Register<SingletonSupplier<CacheRefreshProvider>> {

    private final Map<String, SingletonSupplier<CacheRefreshProvider>> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 CacheRefreshProvider
     */
    public CacheRefreshProviderRegister() {
    }

    @Override
    public void put(String beanId, SingletonSupplier<CacheRefreshProvider> provider) {
        Supplier<CacheRefreshProvider> old = map.put(beanId, provider);
        Assert.isTrue(old == null, () -> "CacheRefreshProvider: [" + beanId + "] duplicate id.");
    }

    @Override
    public SingletonSupplier<CacheRefreshProvider> get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, SingletonSupplier<CacheRefreshProvider>> getAll() {
        return Collections.unmodifiableMap(map);
    }

    public void shutdown() {
        map.forEach((name, supplier) -> IOUtils.closeQuietly(supplier.getIfPresent()));
    }

}