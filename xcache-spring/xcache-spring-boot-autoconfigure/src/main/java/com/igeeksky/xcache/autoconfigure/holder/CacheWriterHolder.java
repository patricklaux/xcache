package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.common.CacheWriter;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于向 CacheManager 注册 CacheWriter
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheWriterHolder implements Holder<CacheWriter<?, ?>> {

    private final Map<String, CacheWriter<?, ?>> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 CacheWriter
     */
    public CacheWriterHolder() {
    }

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