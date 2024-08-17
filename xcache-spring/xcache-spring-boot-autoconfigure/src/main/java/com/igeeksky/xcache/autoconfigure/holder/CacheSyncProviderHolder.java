package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.sync.CacheSyncProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheSyncProviderHolder implements Holder<CacheSyncProvider> {

    private final Map<String, CacheSyncProvider> map = new HashMap<>();

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