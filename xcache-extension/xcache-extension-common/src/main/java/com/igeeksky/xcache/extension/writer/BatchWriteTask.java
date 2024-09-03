package com.igeeksky.xcache.extension.writer;

import com.igeeksky.xcache.common.CacheWriter;

import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/3
 */
public record BatchWriteTask<K, V>(CacheWriter<K, V> writer, Map<K, V> keyValues) implements Runnable {

    @Override
    public void run() {
        writer.writeAll(keyValues);
    }

}
