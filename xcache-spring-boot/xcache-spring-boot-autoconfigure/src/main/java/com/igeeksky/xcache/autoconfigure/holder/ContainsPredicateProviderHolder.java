package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.contains.ContainsPredicateProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class ContainsPredicateProviderHolder implements Holder<ContainsPredicateProvider> {

    private final Map<String, ContainsPredicateProvider> map = new HashMap<>();

    @Override
    public void put(String beanId, ContainsPredicateProvider provider) {
        map.put(beanId, provider);
    }

    @Override
    public ContainsPredicateProvider get(String beanId) {
        ContainsPredicateProvider provider = map.get(beanId);
        Assert.notNull(provider, "beanId:[" + beanId + "] ContainsPredicateProvider doesn't exit");
        return provider;
    }

    @Override
    public Map<String, ContainsPredicateProvider> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
