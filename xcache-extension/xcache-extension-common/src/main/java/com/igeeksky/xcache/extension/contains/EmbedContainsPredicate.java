package com.igeeksky.xcache.extension.contains;

/**
 * 总是返回 true
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public class EmbedContainsPredicate<K> implements ContainsPredicate<K> {

    private static final EmbedContainsPredicate<?> INSTANCE = new EmbedContainsPredicate<>();

    @SuppressWarnings("unchecked")
    public static <K> EmbedContainsPredicate<K> getInstance() {
        return (EmbedContainsPredicate<K>) INSTANCE;
    }

    @Override
    public boolean test(String cacheName, K key) {
        return true;
    }

}
