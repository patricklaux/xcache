package com.igeeksky.xcache.extension.contains;

/**
 * 判断数据源是否有值 <p>
 * 作用：处理缓存穿透问题 <p>
 * 譬如：定时读取数据源中的所有 Key，然后生成一个布隆过滤器，每次回源查询数据时可以通过布隆过滤器来判断数据源是否有值
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