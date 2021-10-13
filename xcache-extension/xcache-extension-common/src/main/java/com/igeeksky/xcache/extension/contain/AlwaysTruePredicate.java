package com.igeeksky.xcache.extension.contain;

/**
 * 无操作判断类，test方法始终返回true
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
public class AlwaysTruePredicate<K> implements ContainsPredicate<K> {

    @Override
    public boolean test(String cacheName, K key) {
        return true;
    }

}