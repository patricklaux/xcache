package com.igeeksky.xcache.autoconfigure.holder;


import com.igeeksky.xcache.core.store.RemoteStoreProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-29
 */
public class RemoteStoreProviderHolder implements Holder<RemoteStoreProvider> {

    private final Map<String, RemoteStoreProvider> map = new HashMap<>();

    @Override
    public void put(String beanId, RemoteStoreProvider provider) {
        map.put(beanId, provider);
    }

    @Override
    public RemoteStoreProvider get(String beanId) {
        RemoteStoreProvider provider = map.get(beanId);
        Assert.notNull(provider, "beanId:[" + beanId + "] RedisCacheStoreProvider doesn't exist");
        return provider;
    }

    @Override
    public Map<String, RemoteStoreProvider> getAll() {
        return Collections.unmodifiableMap(map);
    }
}
