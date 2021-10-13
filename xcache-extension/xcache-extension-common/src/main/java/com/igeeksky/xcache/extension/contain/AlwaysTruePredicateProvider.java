package com.igeeksky.xcache.extension.contain;

import com.igeeksky.xcache.common.Singleton;

/**
 * 无操作类工厂，test方法始终返回true
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
@Singleton
public class AlwaysTruePredicateProvider implements ContainsPredicateProvider {

    @Override
    public <K> ContainsPredicate<K> get(Class<K> keyType) {
        return new AlwaysTruePredicate<>();
    }

}