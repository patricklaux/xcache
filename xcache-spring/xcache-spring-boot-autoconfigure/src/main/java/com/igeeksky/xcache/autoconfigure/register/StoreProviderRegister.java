package com.igeeksky.xcache.autoconfigure.register;

import com.igeeksky.xcache.core.store.StoreProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 用于向 CacheManager 注册 StoreProvider
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-29
 */
public class StoreProviderRegister implements Register<Supplier<StoreProvider>> {

    private final Map<String, Supplier<StoreProvider>> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 StoreProvider
     */
    public StoreProviderRegister() {
    }

    @Override
    public void put(String beanId, Supplier<StoreProvider> provider) {
        Supplier<StoreProvider> old = map.put(beanId, provider);
        Assert.isTrue(old == null, "StoreProvider:[" + beanId + "]: duplicate id.");
    }

    @Override
    public Supplier<StoreProvider> get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, Supplier<StoreProvider>> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
