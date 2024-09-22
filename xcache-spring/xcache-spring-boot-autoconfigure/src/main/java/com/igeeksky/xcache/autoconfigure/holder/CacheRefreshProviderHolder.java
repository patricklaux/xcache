package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.refresh.CacheRefreshProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于向 CacheManager 注册 CacheRefreshProvider
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheRefreshProviderHolder implements Holder<CacheRefreshProvider> {

    private final Map<String, CacheRefreshProvider> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 CacheRefreshProvider
     */
    public CacheRefreshProviderHolder() {
    }

    @Override
    public void put(String beanId, CacheRefreshProvider provider) {
        CacheRefreshProvider old = map.put(beanId, provider);
        Assert.isTrue(old == null, () -> "CacheRefreshProvider: [" + beanId + "] duplicate id.");
    }

    @Override
    public CacheRefreshProvider get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, CacheRefreshProvider> getAll() {
        return Collections.unmodifiableMap(map);
    }

}