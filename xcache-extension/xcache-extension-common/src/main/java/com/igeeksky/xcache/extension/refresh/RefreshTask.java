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
 * @param consumer     刷新回调（数据回源取值，并更新缓存数据）
 * @param predicate    判断缓存是否包含该键
 */
public record RefreshTask(CacheRefresh cacheRefresh, String key,
                          Consumer<String> consumer, Predicate<String> predicate) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(RefreshTask.class);

    @Override
    public void run() {
        try {
            // 判断缓存中是否包含该键
            if (predicate.test(key)) {
                // 刷新缓存
                consumer.accept(key);
            } else {
                // 刷新队列删除该键
                cacheRefresh.onRemove(key);
            }
        } catch (Throwable e) {
            log.error("RefreshTask has error: {}", e.getMessage());
        }
    }

}