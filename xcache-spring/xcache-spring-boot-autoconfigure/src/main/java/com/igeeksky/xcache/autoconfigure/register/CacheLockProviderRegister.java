package com.igeeksky.xcache.autoconfigure.register;

import com.igeeksky.xcache.core.SingletonSupplier;
import com.igeeksky.xcache.extension.lock.CacheLockProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 用于向 CacheManager 注册 CacheLockProvider
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheLockProviderRegister implements Register<SingletonSupplier<CacheLockProvider>> {

    private final Map<String, SingletonSupplier<CacheLockProvider>> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 CacheLockProvider
     */
    public CacheLockProviderRegister() {
    }

    @Override
    public void put(String beanId, SingletonSupplier<CacheLockProvider> provider) {
        Supplier<CacheLockProvider> old = map.put(beanId, provider);
        Assert.isTrue(old == null, () -> "CacheLockProvider: [" + beanId + "] duplicate id.");
    }

    @Override
    public SingletonSupplier<CacheLockProvider> get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, SingletonSupplier<CacheLockProvider>> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
