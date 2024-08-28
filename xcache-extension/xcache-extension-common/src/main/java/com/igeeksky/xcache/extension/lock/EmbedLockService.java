package com.igeeksky.xcache.extension.lock;

import com.igeeksky.xtool.core.collection.Maps;

import java.util.Map;

/**
 * 本地缓存锁
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/22
 */
public class EmbedLockService implements LockService {

    private final Map<String, EmbedLock> locks;

    public EmbedLockService(int initialCapacity) {
        this.locks = Maps.newConcurrentHashMap(initialCapacity);
    }

    /**
     * 根据给定的键获取对应的锁实例。 <p>
     * 如果对应键的锁不存在，则创建一个新的锁实例；如果存在，则增加其使用计数。 <p>
     * 这种方法确保了同一个键总是返回相同的锁实例，实现了锁的重用，同时也通过计数器管理了锁的生命周期。
     *
     * @param key 锁的唯一标识符。与锁关联的键，用于查找或创建锁。
     * @return 对应于给定键的锁实例。如果是新创建的，返回一个新锁；否则返回已存在的锁。
     */
    @Override
    public EmbedLock acquire(String key) {
        // 使用compute方法来获取或创建锁。如果键已存在，则更新其使用计数并返回锁实例；
        // 如果键不存在，则创建一个新的锁实例并将其与键关联。
        return locks.compute(key, (k, lock) -> {
            if (lock == null) {
                // 锁不存在时，创建并返回一个新的嵌入式锁。
                return new EmbedLock(key);
            }
            // 锁已存在时，增加其使用计数并返回锁实例。
            lock.increment();
            return lock;
        });
    }

    @Override
    public void release(String key) {
        locks.computeIfPresent(key, (k, lock) -> {
            if (lock.decrementAndGet() <= 0) {
                return null;
            }
            return lock;
        });
    }

    @Override
    public int size() {
        return locks.size();
    }

}