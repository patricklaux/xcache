package com.igeeksky.xcache.extension.lock;

import java.util.concurrent.locks.Lock;

/**
 * 锁服务
 * <p>
 * 用于管理锁对象的申请和释放
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-10
 */
public interface LockService {

    /**
     * 获取锁对象
     * <p>
     * acquire 与 release 必须成对使用，否则可能会有内存溢出风险。
     *
     * <p>
     * 代码示例
     * <pre>{@code
     * String key = "test-lock";
     * // 1. 获取锁对象
     * KeyLock lock = lockService.acquire(key);
     * try {
     *     // 2. 加锁
     *     lock.lock();
     *     try {
     *         // do something;
     *     } finally {
     *         // 3. 解锁
     *         lock.unlock();
     *     }
     * } finally {
     *     // 4. 归还锁对象
     *     lockService.release(key);
     * }
     * }</pre>
     *
     * @param key 键
     * @return {@link Lock} – 锁对象
     */
    Lock acquire(String key);

    /**
     * 归还锁对象
     * <p>
     * acquire 与 release 必须成对使用，否则可能会有内存溢出风险。
     *
     * @param key 键
     */
    void release(String key);

    /**
     * 获取锁对象数量
     *
     * @return {@code int} – 锁对象数量
     */
    int size();

}