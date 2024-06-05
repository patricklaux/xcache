package com.igeeksky.xcache.extension.contains;

import com.igeeksky.xcache.config.props.CacheProps;

/**
 * 无操作类工厂，test方法始终返回true
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
public class TrueContainsPredicateProvider implements ContainsPredicateProvider {

    public static final TrueContainsPredicateProvider INSTANCE = new TrueContainsPredicateProvider();

    @Override
    public <K> ContainsPredicate<K> get(Class<K> keyType, CacheProps cacheProps) {
        return TrueContainsPredicate.getInstance();
    }

    @Override
    public void close() {
        // do nothing
    }
}