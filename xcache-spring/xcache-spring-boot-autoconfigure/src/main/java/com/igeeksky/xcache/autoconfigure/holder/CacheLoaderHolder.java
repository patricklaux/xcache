package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于向 CacheManager 注册 CacheLoader
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheLoaderHolder implements Holder<CacheLoader<?, ?>> {

    private final Map<String, CacheLoader<?, ?>> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 CacheLoader
     */
    public CacheLoaderHolder() {
    }

    @Override
    public void put(String name, CacheLoader<?, ?> loader) {
        CacheLoader<?, ?> old = map.put(name, loader);
        Assert.isTrue(old == null, () -> "CacheLoader: [" + name + "] duplicate id.");
    }

    @Override
    public CacheLoader<?, ?> get(String name) {
        return map.get(name);
    }

    @Override
    public Map<String, CacheLoader<?, ?>> getAll() {
        return Collections.unmodifiableMap(map);
    }

}