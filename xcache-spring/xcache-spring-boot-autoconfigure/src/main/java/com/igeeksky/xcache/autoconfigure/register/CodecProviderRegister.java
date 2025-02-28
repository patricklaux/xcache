package com.igeeksky.xcache.autoconfigure.register;

import com.igeeksky.xcache.extension.codec.CodecProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 用于向 CacheManager 注册 CodecProvider
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-08
 */
public class CodecProviderRegister implements Register<Supplier<CodecProvider>> {

    private final Map<String, Supplier<CodecProvider>> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 CodecProvider 对象
     */
    public CodecProviderRegister() {
    }

    @Override
    public void put(String beanId, Supplier<CodecProvider> provider) {
        Supplier<CodecProvider> old = map.put(beanId, provider);
        Assert.isTrue(old == null, () -> "CodecProvider: [" + beanId + "] duplicate id.");
    }

    @Override
    public Supplier<CodecProvider> get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, Supplier<CodecProvider>> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
