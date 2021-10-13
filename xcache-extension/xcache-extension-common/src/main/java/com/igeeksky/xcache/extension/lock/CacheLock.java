package com.igeeksky.xcache.extension.lock;

import java.util.concurrent.locks.Lock;

/**
 * 缓存锁Holder。用于根据Key获取锁，通过锁实现相同key仅允许一个线程回源获取数据
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-10
 */
public interface CacheLock<K> {

    /**
     * 获取锁
     *
     * @param key 缓存Key
     * @return 锁
     */
    Lock get(K key);

}
