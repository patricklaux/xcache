package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.stat.CacheStatProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheStatProviderHolder implements Holder<CacheStatProvider> {

    private final Map<String, CacheStatProvider> map = new HashMap<>();

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
