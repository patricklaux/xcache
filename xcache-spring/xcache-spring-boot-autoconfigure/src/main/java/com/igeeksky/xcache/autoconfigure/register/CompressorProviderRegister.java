package com.igeeksky.xcache.autoconfigure.register;

import com.igeeksky.xcache.common.Register;
import com.igeeksky.xcache.core.SingletonSupplier;
import com.igeeksky.xcache.extension.compress.CompressorProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 用于向 CacheManager 注册 CompressorProvider
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CompressorProviderRegister implements Register<SingletonSupplier<CompressorProvider>> {

    private final Map<String, SingletonSupplier<CompressorProvider>> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 CompressorProvider
     */
    public CompressorProviderRegister() {
    }

    @Override
    public void put(String beanId, SingletonSupplier<CompressorProvider> provider) {
        Supplier<CompressorProvider> old = map.put(beanId, provider);
        Assert.isTrue(old == null, () -> "CompressorProvider: [" + beanId + "] duplicate id.");
    }

    @Override
    public SingletonSupplier<CompressorProvider> get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, SingletonSupplier<CompressorProvider>> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
