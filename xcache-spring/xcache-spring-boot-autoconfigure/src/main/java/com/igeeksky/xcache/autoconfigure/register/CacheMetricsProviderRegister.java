package com.igeeksky.xcache.autoconfigure.register;

import com.igeeksky.xcache.core.SingletonSupplier;
import com.igeeksky.xcache.extension.metrics.CacheMetricsProvider;
import com.igeeksky.xtool.core.io.IOUtils;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于向 CacheManager 注册 CacheStatProvider
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheMetricsProviderRegister implements Register<SingletonSupplier<CacheMetricsProvider>> {

    private final Map<String, SingletonSupplier<CacheMetricsProvider>> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 CacheStatProvider
     */
    public CacheMetricsProviderRegister() {
    }

    @Override
    public void put(String beanId, SingletonSupplier<CacheMetricsProvider> supplier) {
        SingletonSupplier<CacheMetricsProvider> old = map.put(beanId, supplier);
        Assert.isTrue(old == null, () -> "CacheStatProvider: [" + beanId + "] duplicate id.");
    }

    @Override
    public SingletonSupplier<CacheMetricsProvider> get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, SingletonSupplier<CacheMetricsProvider>> getAll() {
        return Collections.unmodifiableMap(map);
    }

    public void shutdown() {
        map.forEach((id, supplier) -> IOUtils.closeQuietly(supplier.getIfPresent()));
    }

}