package com.igeeksky.xcache.extension.writer;

import com.igeeksky.xcache.common.CacheWriter;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.collection.Sets;
import com.igeeksky.xtool.core.concurrent.VirtualThreadFactory;
import com.igeeksky.xtool.core.function.tuple.Pair;
import com.igeeksky.xtool.core.function.tuple.Pairs;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/2
 */
public class CacheWriteScheduler<K, V> {

    private final boolean writeBehind;

    private final CacheWriter<K, V> writer;

    private final CacheWriterConfig<K, V> config;

    private final ExecutorService executor = executor();

    private final ScheduledExecutorService scheduler;

    private volatile ConcurrentMap<K, Pair<CacheWriteType, V>> idleTasks = Maps.newConcurrentHashMap();

    private volatile ConcurrentMap<K, Pair<CacheWriteType, V>> activeTasks = Maps.newConcurrentHashMap();

    private volatile ConcurrentMap<K, Pair<CacheWriteType, V>> retryTasks = Maps.newConcurrentHashMap();

    public CacheWriteScheduler(CacheWriterConfig<K, V> config, ScheduledExecutorService scheduler) {
        this.config = config;
        this.writer = config.getWriter();
        if (CacheWriteStrategy.WRITE_BEHIND == config.getStrategy()) {
            this.writeBehind = true;
            this.scheduler = scheduler;
            this.start();
        } else {
            this.writeBehind = false;
            this.scheduler = null;
        }
    }

    public void write(K key, V value) {
        if (writeBehind) {
            idleTasks.put(key, Pairs.of(CacheWriteType.WRITE, value));
        } else {
            writer.write(key, value);
        }
    }

    public void writeAll(Map<K, V> keyValues) {
        if (writeBehind) {
            keyValues.forEach((key, value) -> idleTasks.put(key, Pairs.of(CacheWriteType.WRITE, value)));
        } else {
            writer.writeAll(keyValues);
        }
    }

    public void delete(K key) {
        if (writeBehind) {
            idleTasks.put(key, Pairs.of(CacheWriteType.DELETE, null));
        } else {
            writer.delete(key);
        }
    }

    public void deleteAll(Set<K> keys) {
        if (writeBehind) {
            keys.forEach(key -> idleTasks.put(key, Pairs.of(CacheWriteType.DELETE, null)));
        } else {
            writer.deleteAll(keys);
        }
    }

    private void start() {
        scheduler.scheduleAtFixedRate(this::execute, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private void execute() {
        int batchSize = config.getBatchSize();
        Map<K, V> batchWrite = Maps.newHashMap(batchSize);
        Set<K> batchDelete = Sets.newHashSet(batchSize);
        for (Map.Entry<K, Pair<CacheWriteType, V>> entry : idleTasks.entrySet()) {
            K key = entry.getKey();
            Pair<CacheWriteType, V> pair = entry.getValue();
            if (pair.key() == CacheWriteType.DELETE) {
                batchDelete.add(key);
                if (batchDelete.size() >= batchSize) {
                    executor.submit(new BatchDeleteTask<>(writer, batchDelete));
                    batchDelete = Sets.newHashSet(batchSize);
                }
            } else {
                batchWrite.put(key, pair.value());
                if (batchWrite.size() >= batchSize) {
                    executor.submit(new BatchWriteTask<>(writer, batchWrite));
                    batchWrite = Maps.newHashMap(batchSize);
                }
            }
        }
    }

    private static ExecutorService executor() {
        return Executors.newThreadPerTaskExecutor(new VirtualThreadFactory("cache-write-thread-"));
    }

}