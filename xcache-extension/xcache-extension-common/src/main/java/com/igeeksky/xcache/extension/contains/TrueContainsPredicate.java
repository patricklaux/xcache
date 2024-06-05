package com.igeeksky.xcache.extension.contains;

/**
 * 总是返回 true
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public class TrueContainsPredicate<K> implements ContainsPredicate<K> {

    private static final TrueContainsPredicate<?> INSTANCE = new TrueContainsPredicate<>();

    @SuppressWarnings("unchecked")
    public static <K> TrueContainsPredicate<K> getInstance() {
        return (TrueContainsPredicate<K>) INSTANCE;
    }

    @Override
    public boolean test(String cacheName, K key) {
        return true;
    }

}
