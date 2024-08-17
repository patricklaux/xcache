package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.refresh.CacheRefreshProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheRefreshProviderHolder implements Holder<CacheRefreshProvider> {

    private final Map<String, CacheRefreshProvider> map = new HashMap<>();

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