package com.igeeksky.xcache.extension.statistic;

import java.util.concurrent.atomic.LongAdder;

/**
 * 缓存指标统计
 *
 * @author Patrick.Lau
 * @since 0.0.2 2020-12-12
 */
public class CacheStatisticsCounter {

    private final LongAdder nullHits = new LongAdder();
    private final LongAdder notNullHits = new LongAdder();
    private final LongAdder nullLoads = new LongAdder();
    private final LongAdder notNullLoads = new LongAdder();
    private final LongAdder misses = new LongAdder();
    private final LongAdder puts = new LongAdder();
    private final LongAdder removals = new LongAdder();
    private final LongAdder clears = new LongAdder();

    public CacheStatisticsCounter() {
    }

    public long getNullHits() {
        return nullHits.sum();
    }

    public long getNotNullHits() {
        return notNullHits.sum();
    }

    public long getNullLoads() {
        return nullLoads.sum();
    }

    public long getNotNullLoads() {
        return notNullLoads.sum();
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

    public void incNullHits() {
        nullHits.increment();
    }

    public void incNotNullHits() {
        notNullHits.increment();
    }

    public void incNullLoads() {
        nullLoads.increment();
    }

    public void incNotNullLoads() {
        notNullLoads.increment();
    }

    public void incMisses() {
        misses.increment();
    }

    public void incPuts(long times) {
        puts.add(times);
    }

    public void incRemovals(long times) {
        removals.add(times);
    }

    public void incClears() {
        clears.add(1L);
    }

}
