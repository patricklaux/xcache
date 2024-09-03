package com.igeeksky.xcache.extension.writer;

import com.igeeksky.xcache.common.CacheWriter;

import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/3
 */
public record BatchDeleteTask<K, V>(CacheWriter<K, V> writer, Set<K> keys) implements Runnable {

    @Override
    public void run() {
        writer.deleteAll(keys);
    }

}
