package com.igeeksky.xcache.core.store;

import com.igeeksky.xcache.Base;

/**
 * 缓存存储接口
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-14
 */
public interface Store<K, V> extends Base<K, V> {

    String getStoreName();

}
