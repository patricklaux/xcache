package com.igeeksky.xcache.common;

/**
 * 判断数据源是否有值 <p>
 * 作用：处理缓存穿透问题 <p>
 * 譬如：定时读取数据源中的所有 Key，然后生成布隆过滤器，每次回源查询数据时可以通过布隆过滤器来判断数据源是否有值
 *
 * @param <K> 键类型
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
public interface ContainsPredicate<K> {

    /**
     * 判断数据源是否存在 key 对应的 value
     *
     * @param cacheName 缓存名称，用于确定数据源
     * @param key       键
     * @return 数据源存在 key 对应的 value，返回 true，否则返回 false
     */
    boolean test(String cacheName, K key);

}