package com.igeeksky.xcache.extension.contains;

/**
 * 无操作类工厂，test 方法始终返回 true
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
public class EmbedContainsPredicateProvider implements ContainsPredicateProvider {

    private static final EmbedContainsPredicateProvider INSTANCE = new EmbedContainsPredicateProvider();

    public static EmbedContainsPredicateProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public <K> ContainsPredicate<K> get(ContainsConfig<K> config) {
        return EmbedContainsPredicate.getInstance();
    }

}