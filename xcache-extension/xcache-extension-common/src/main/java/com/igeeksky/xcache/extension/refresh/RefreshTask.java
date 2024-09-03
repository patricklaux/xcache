package com.igeeksky.xcache.extension.refresh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * 缓存刷新任务
 *
 * @param key      键
 * @param consumer 刷新回调，数据回源取值成功后更新缓存信息
 */
public record RefreshTask(String key, Consumer<String> consumer) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(RefreshTask.class);

    @Override
    public void run() {
        try {
            consumer.accept(key);
        } catch (Throwable e) {
            log.error("RefreshTask has error: {}", e.getMessage());
        }
    }
}