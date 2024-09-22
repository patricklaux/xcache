package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.lock.CacheLockProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于向 CacheManager 注册 CacheLockProvider
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheLockProviderHolder implements Holder<CacheLockProvider> {

    private final Map<String, CacheLockProvider> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 CacheLockProvider
     */
    public CacheLockProviderHolder() {
    }

    @Override
    public void put(String beanId, CacheLockProvider provider) {
        CacheLockProvider old = map.put(beanId, provider);
        Assert.isTrue(old == null, () -> "CacheLockProvider: [" + beanId + "] duplicate id.");
    }

    @Override
    public CacheLockProvider get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, CacheLockProvider> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
