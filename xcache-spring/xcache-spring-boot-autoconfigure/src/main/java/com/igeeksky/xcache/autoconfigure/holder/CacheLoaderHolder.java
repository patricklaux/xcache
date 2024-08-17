package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.CacheLoader;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheLoaderHolder implements Holder<CacheLoader<?, ?>> {

    private final Map<String, CacheLoader<?, ?>> map = new HashMap<>();

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