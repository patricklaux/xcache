package com.igeeksky.xcache.autoconfigure.register;

import com.igeeksky.xcache.common.ContainsPredicate;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 用于向 CacheManager 注册 ContainsPredicate
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class ContainsPredicateRegister implements Register<Supplier<ContainsPredicate<?>>> {

    private final Map<String, Supplier<ContainsPredicate<?>>> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 ContainsPredicate
     */
    public ContainsPredicateRegister() {
    }

    @Override
    public void put(String name, Supplier<ContainsPredicate<?>> predicate) {
        Supplier<ContainsPredicate<?>> old = map.put(name, predicate);
        Assert.isTrue(old == null, () -> "ContainsPredicate: [" + name + "] duplicate id.");
    }

    @Override
    public Supplier<ContainsPredicate<?>> get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, Supplier<ContainsPredicate<?>>> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
