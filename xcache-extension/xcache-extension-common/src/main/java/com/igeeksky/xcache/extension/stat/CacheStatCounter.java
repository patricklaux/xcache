package com.igeeksky.xcache.extension.stat;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public class CacheStatCounter {

    private final LongAdder hits = new LongAdder();
    private final LongAdder misses = new LongAdder();
    private final LongAdder puts = new LongAdder();
    private final LongAdder removals = new LongAdder();
    private final LongAdder clears = new LongAdder();

    public CacheStatCounter() {
    }

    public long getHits() {
        return hits.sum();
    }

    public long getMisses() {
        return misses.sum();
    }

    public long getPuts() {
        return puts.sum();
    }

    public long getRemovals() {
        return removals.sum();
    }

    public long getClears() {
        return clears.sum();
    }

    public void incHits(long times) {
        hits.add(times);
    }

    public void incMisses(long times) {
        misses.add(times);
    }

    public void incPuts(long times) {
        puts.add(times);
    }

    public void incRemovals(long times) {
        removals.add(times);
    }

    public void incClears() {
        clears.increment();
    }
}
