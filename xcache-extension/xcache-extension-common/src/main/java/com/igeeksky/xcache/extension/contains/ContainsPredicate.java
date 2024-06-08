package com.igeeksky.xcache.extension.contains;

/**
 * <p>键是否存在于数据源的判断器</p>
 * <p>用于出理缓存穿透问题</p>
 * 譬如可以将数据源中的所有 Key 定时读取后然后生成一个布隆过滤器，每次回源查询数据时先通过布隆过滤器判断是否有该数据。
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
public interface ContainsPredicate<K> {

    /**
     * 判断 cacheName 数据源是否包含 key 对应的 value
     *
     * @param cacheName 缓存名称
     * @param key       键
     * @return 如果数据源中存在该 Key，返回 true；否则返回 false。
     */
    boolean test(String cacheName, K key);

}