package com.igeeksky.xcache.extension.contains;

/**
 * ContainsPredicate 工厂类
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
public interface ContainsPredicateProvider {

    /**
     * 根据配置创建 ContainsPredicate
     *
     * @param config 配置信息
     * @param <K>    键类型
     * @return ContainsPredicate，用于判断数据源是否存在 key 对应的 value
     */
    <K> ContainsPredicate<K> get(PredicateConfig<K> config);

}
