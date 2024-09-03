package com.igeeksky.xcache.common;



import com.igeeksky.xtool.core.concurrent.Futures;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 缓存刷新
 * <p>
 * 保存所有访问记录，定时使用 consumer 回源读取数据并存入缓存
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/26
 */
public interface CacheRefresh {

    /**
     * 保存访问记录
     *
     * @param key 键
     */
    void access(String key);

    /**
     * 批量保存访问记录
     *
     * @param keys 键集
     */
    void accessAll(Set<String> keys);

    /**
     * 移除访问记录
     *
     * @param key 键
     */
    void remove(String key);

    /**
     * 批量移除访问记录
     *
     * @param keys 键集
     */
    void removeAll(Set<String> keys);


    /**
     * 设置刷新任务消费回调
     *
     * @param consumer 根据传入的 key，回源查询并存入缓存
     */
    void setConsumer(Consumer<String> consumer);

    /**
     * 检查所有刷新任务是否已完成
     *
     * @param futuresRef 刷新任务队列
     * @param timeout    超时（单位：毫秒）
     * @return {@code true} - 已完成全部任务；{@code false} - 仍有任务未完成
     */
    default boolean checkRefreshTasks(AtomicReference<Future<?>[]> futuresRef, long timeout) {
        Future<?>[] futures = futuresRef.get();
        if (futures != null) {
            int last = Futures.awaitAll(timeout, TimeUnit.MILLISECONDS, 0, futures);
            int length = futures.length;
            if (last < length) {
                if (last > 0) {
                    futuresRef.set(Arrays.copyOfRange(futures, last, length));
                }
                return false;
            }
            futuresRef.set(null);
        }
        return true;
    }

}