package com.igeeksky.xcache.autoconfigure.register;

import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xcache.common.Register;
import com.igeeksky.xcache.core.SingletonSupplier;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 用于向 ComponentManager 注册 CacheLoader
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheLoaderRegister implements Register<SingletonSupplier<CacheLoader<?, ?>>> {

    private final Map<String, SingletonSupplier<CacheLoader<?, ?>>> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 CacheLoader
     */
    public CacheLoaderRegister() {
    }

    @Override
    public void put(String name, SingletonSupplier<CacheLoader<?, ?>> loader) {
        Supplier<CacheLoader<?, ?>> old = map.put(name, loader);
        Assert.isTrue(old == null, () -> "CacheLoader: [" + name + "] duplicate id.");
    }

    @Override
    public SingletonSupplier<CacheLoader<?, ?>> get(String name) {
        return map.get(name);
    }

    @Override
    public Map<String, SingletonSupplier<CacheLoader<?, ?>>> getAll() {
        return Collections.unmodifiableMap(map);
    }

}