package com.igeeksky.xcache.extension.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Patrick.Lau
 * @date 2020-12-10
 */
public class LocalCacheLock<K> implements CacheLock<K> {

    private static final int MAXIMUM_LOCK = 1 << 30;

    /**
     * LOCK数组对象的HashSlot计算
     */
    private final int mask;

    /**
     * 内部锁的对象数组
     */
    private final ReentrantLock[] LOCK_ARRAY;

    public LocalCacheLock(int lockSize) {
        int lockSize1 = lockSizeFor(lockSize == 0 ? 256 : lockSize);
        this.mask = lockSize1 - 1;
        this.LOCK_ARRAY = new ReentrantLock[lockSize1];
        for (int i = 0; i < this.LOCK_ARRAY.length; i++) {
            LOCK_ARRAY[i] = new ReentrantLock();
        }
    }

    private int lockSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_LOCK) ? MAXIMUM_LOCK : n + 1;
    }

    @Override
    public Lock get(K key) {
        int hashCode = key.hashCode();
        return LOCK_ARRAY[hashCode & mask];
    }

}
