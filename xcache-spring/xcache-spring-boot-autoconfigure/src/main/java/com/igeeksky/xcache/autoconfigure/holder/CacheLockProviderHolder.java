package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.lock.CacheLockProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheLockProviderHolder implements Holder<CacheLockProvider> {

    private final Map<String, CacheLockProvider> map = new HashMap<>();

    @Override
    public void put(String beanId, CacheLockProvider provider) {
        map.put(beanId, provider);
    }

    @Override
    public CacheLockProvider get(String beanId) {
        CacheLockProvider provider = map.get(beanId);
        Assert.notNull(provider, "beanId:[" + beanId + "] CacheLockProvider doesn't exit");
        return provider;
    }

    @Override
    public Map<String, CacheLockProvider> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
