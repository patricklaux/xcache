package com.igeeksky.xcache.extension.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 缓存刷新任务
 *
 * @param cacheRefresh 缓存刷新器
 * @param key          键
 * @param consumer     用以执行回源查询并将回源结果写入缓存
 * @param predicate    用以判断缓存是否存在键对应的值：如果此 {@code predicate} 返回 true，执行数据刷新；否则，不再继续刷新。 <br>
 *                     1. 缓存存储系统可能会根据特定算法清除某些访问频率较低的键，增加此判断可以避免缓存算法失效，导致命中率降低； <br>
 *                     2. {@link CacheRefresh#onRemove} 执行异常，导致未能移除无需再刷新的键，增加此判断可及时清理垃圾键集。
 */
public record RefreshTask(CacheRefresh cacheRefresh, String key,
                          Consumer<String> consumer, Predicate<String> predicate) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(RefreshTask.class);

    @Override
    public void run() {
        try {
            if (predicate.test(key)) {
                // 刷新数据
                consumer.accept(key);
            } else {
                // 停止刷新
                cacheRefresh.onRemove(key);
            }
        } catch (Throwable e) {
            log.error("RefreshTask has error: {}", e.getMessage());
        }
    }

}