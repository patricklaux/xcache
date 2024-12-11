package com.igeeksky.xcache.common;

/**
 * 缓存存储接口
 * <p>
 * 键限定为字符串类型
 *
 * @param <V> 缓存值类型
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-14
 */
public interface Store<V> extends Base<String, V> {

    /**
     * 判断缓存中是否存在指定键的缓存值
     *
     * @param key 缓存键
     * @return 存在返回true，否则返回false
     */
    boolean contains(String key);

}
