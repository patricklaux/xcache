package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.stat.CacheStatProvider;
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
public class CacheStatProviderHolder implements Holder<CacheStatProvider> {

    private final Map<String, CacheStatProvider> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 CacheStatProvider
     */
    public CacheStatProviderHolder() {
    }

    @Override
    public void put(String beanId, CacheStatProvider provider) {
        CacheStatProvider old = map.put(beanId, provider);
        Assert.isTrue(old == null, () -> "CacheStatProvider: [" + beanId + "] duplicate id.");
    }

    @Override
    public CacheStatProvider get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, CacheStatProvider> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
