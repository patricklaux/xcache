package com.igeeksky.xcache.extension.stat;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存指标计数类
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public class CacheStatCounter {

    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    private final AtomicLong puts = new AtomicLong();
    private final AtomicLong removals = new AtomicLong();
    private final AtomicLong clears = new AtomicLong();

    public CacheStatCounter() {
    }

    public long getHits() {
        return hits.get();
    }

    public long getMisses() {
        return misses.get();
    }

    public long getPuts() {
        return puts.get();
    }

    public long getRemovals() {
        return removals.get();
    }

    public long getClears() {
        return clears.get();
    }

    public void incHits(long times) {
        hits.addAndGet(times);
    }

    public void incMisses(long times) {
        misses.addAndGet(times);
    }

    public void incPuts(long times) {
        puts.addAndGet(times);
    }

    public void incRemovals(long times) {
        removals.addAndGet(times);
    }

    public void incClears() {
        clears.incrementAndGet();
    }

}
