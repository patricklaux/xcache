package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.sync.CacheSyncManager;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheSyncProviderHolder implements Holder<CacheSyncManager> {

    private final Map<String, CacheSyncManager> map = new HashMap<>();

    @Override
    public void put(String beanId, CacheSyncManager provider) {
        map.put(beanId, provider);
    }

    @Override
    public CacheSyncManager get(String beanId) {
        CacheSyncManager manager = map.get(beanId);
        Assert.notNull(manager, "beanId:[" + beanId + "] RedisCacheSyncManager doesn't exit");
        return manager;
    }

    @Override
    public Map<String, CacheSyncManager> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
