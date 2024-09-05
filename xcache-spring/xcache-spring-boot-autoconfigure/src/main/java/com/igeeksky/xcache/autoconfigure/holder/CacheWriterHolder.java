package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.common.CacheWriter;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheWriterHolder implements Holder<CacheWriter<?, ?>> {

    private final Map<String, CacheWriter<?, ?>> map = new HashMap<>();

    @Override
    public void put(String name, CacheWriter<?, ?> writer) {
        CacheWriter<?, ?> old = map.put(name, writer);
        Assert.isTrue(old == null, () -> "CacheWriter: [" + name + "] duplicate id.");
    }

    @Override
    public CacheWriter<?, ?> get(String name) {
        return map.get(name);
    }

    @Override
    public Map<String, CacheWriter<?, ?>> getAll() {
        return Collections.unmodifiableMap(map);
    }

}