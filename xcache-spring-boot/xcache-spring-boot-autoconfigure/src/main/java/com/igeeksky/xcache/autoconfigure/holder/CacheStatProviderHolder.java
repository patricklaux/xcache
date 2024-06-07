package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.statistic.CacheStatManager;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CacheStatProviderHolder implements Holder<CacheStatManager> {

    private final Map<String, CacheStatManager> map = new HashMap<>();

    @Override
    public void put(String beanId, CacheStatManager provider) {
        map.put(beanId, provider);
    }

    @Override
    public CacheStatManager get(String beanId) {
        CacheStatManager manager = map.get(beanId);
        Assert.notNull(manager, "beanId:[" + beanId + "] CacheStatManager doesn't exit");
        return manager;
    }

    @Override
    public Map<String, CacheStatManager> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
