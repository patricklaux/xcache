package com.igeeksky.xcache.extension.contains;

/**
 * 判断数据源是否包含 key 对应的 value，工厂类
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
public interface ContainsPredicateProvider {

    <K> ContainsPredicate<K> get(ContainsConfig<K> config);

}
