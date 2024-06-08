package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.core.store.LocalStoreProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-29
 */
public class LocalStoreProviderHolder implements Holder<LocalStoreProvider> {

    private final Map<String, LocalStoreProvider> map = new HashMap<>();

    @Override
    public void put(String beanId, LocalStoreProvider provider) {
        map.put(beanId, provider);
    }

    @Override
    public LocalStoreProvider get(String beanId) {
        LocalStoreProvider provider = map.get(beanId);
        Assert.notNull(provider, "beanId:[" + beanId + "] CaffeineStoreProvider doesn't exist");
        return provider;
    }

    @Override
    public Map<String, LocalStoreProvider> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
