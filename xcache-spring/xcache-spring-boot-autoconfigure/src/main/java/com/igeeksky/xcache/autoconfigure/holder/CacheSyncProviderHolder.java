package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.sync.CacheSyncProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于向 CacheManager 注册 CacheSyncProvider
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheSyncProviderHolder implements Holder<CacheSyncProvider> {

    private final Map<String, CacheSyncProvider> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 CacheSyncProvider
     */
    public CacheSyncProviderHolder() {
    }

    @Override
    public void put(String beanId, CacheSyncProvider provider) {
        CacheSyncProvider old = map.put(beanId, provider);
        Assert.isTrue(old == null, () -> "CacheSyncProvider: [" + beanId + "] duplicate id.");
    }

    @Override
    public CacheSyncProvider get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, CacheSyncProvider> getAll() {
        return Collections.unmodifiableMap(map);
    }

}