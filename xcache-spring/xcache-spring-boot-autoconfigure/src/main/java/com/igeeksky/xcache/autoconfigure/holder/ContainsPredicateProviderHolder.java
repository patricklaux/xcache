package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.contains.ContainsPredicateProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于向 CacheManager 注册 ContainsPredicateProvider
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class ContainsPredicateProviderHolder implements Holder<ContainsPredicateProvider> {

    private final Map<String, ContainsPredicateProvider> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 ContainsPredicateProvider
     */
    public ContainsPredicateProviderHolder() {
    }

    @Override
    public void put(String beanId, ContainsPredicateProvider provider) {
        ContainsPredicateProvider old = map.put(beanId, provider);
        Assert.isTrue(old == null, () -> "ContainsPredicateProvider: [" + beanId + "] duplicate id.");
    }

    @Override
    public ContainsPredicateProvider get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, ContainsPredicateProvider> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
