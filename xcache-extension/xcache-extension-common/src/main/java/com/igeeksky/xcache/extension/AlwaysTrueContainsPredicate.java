package com.igeeksky.xcache.extension;

import com.igeeksky.xcache.common.ContainsPredicate;

/**
 * 总是返回 true
 * <p>
 * 当用户未提供有效的 containsPredicate 时，使用此实现类
 *
 * @param <K> 键类型
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public class AlwaysTrueContainsPredicate<K> implements ContainsPredicate<K> {

    private static final AlwaysTrueContainsPredicate<?> INSTANCE = new AlwaysTrueContainsPredicate<>();

    @SuppressWarnings("unchecked")
    public static <K> AlwaysTrueContainsPredicate<K> getInstance() {
        return (AlwaysTrueContainsPredicate<K>) INSTANCE;
    }

    /**
     * 检查指定的键是否存在于指定的缓存中
     *
     * @param key 要检查的键，其具体类型由 K 泛型决定
     * @return 此实现类总是返回 true，表示在这个上下文中所有的键对应的值都被认为是存在的
     */
    @Override
    public boolean test(K key) {
        return true;
    }

}
