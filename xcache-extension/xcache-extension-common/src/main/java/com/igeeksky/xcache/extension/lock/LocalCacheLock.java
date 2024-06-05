package com.igeeksky.xcache.extension.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2020-12-10
 */
public class LocalCacheLock<K> implements CacheLock {

    private static final int DEFAULT_LOCK_SIZE = 1 << 7;
    private static final int MAXIMUM_LOCK_SIZE = 1 << 12;

    /**
     * LOCK数组对象的HashSlot计算
     */
    private final int mask;

    /**
     * 内部锁的对象数组
     */
    private final ReentrantLock[] LOCK_ARRAY;

    public LocalCacheLock(int lockSize) {
        int size = lockSizeFor(lockSize <= 0 ? DEFAULT_LOCK_SIZE : lockSize);
        this.mask = size - 1;
        this.LOCK_ARRAY = new ReentrantLock[size];
        IntStream.range(0, size).forEach(i -> LOCK_ARRAY[i] = new ReentrantLock());
    }

    public LocalCacheLock() {
        this(DEFAULT_LOCK_SIZE);
    }

    private int lockSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_LOCK_SIZE) ? MAXIMUM_LOCK_SIZE : n + 1;
    }

    @Override
    public Lock get(String key) {
        return LOCK_ARRAY[key.hashCode() & mask];
    }

}
