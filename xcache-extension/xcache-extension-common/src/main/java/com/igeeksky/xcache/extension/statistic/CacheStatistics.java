package com.igeeksky.xcache.extension.statistic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.StringJoiner;

public class CacheStatistics {
    private long hits;
    private long misses;
    private long puts;
    private long removals;
    private long clears;

    public CacheStatistics() {
    }

    public long getHits() {
        return hits;
    }

    public void setHits(long hits) {
        this.hits = hits;
    }

    public long getMisses() {
        return misses;
    }

    public void setMisses(long misses) {
        this.misses = misses;
    }

    public long getPuts() {
        return puts;
    }

    public void setPuts(long puts) {
        this.puts = puts;
    }

    public long getRemovals() {
        return removals;
    }

    public void setRemovals(long removals) {
        this.removals = removals;
    }

    public long getClears() {
        return clears;
    }

    public void setClears(long clears) {
        this.clears = clears;
    }

    public float getHitPercentage() {
        long gets = this.hits + this.misses;
        if (gets > 0) {
            BigDecimal percentage = new BigDecimal(this.hits).divide(new BigDecimal(gets), 6, RoundingMode.HALF_UP);
            return Float.parseFloat(percentage.toPlainString());
        }
        return 0F;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "{", "}")
                .add("\"hits\":" + hits)
                .add("\"misses\":" + misses)
                .add("\"puts\":" + puts)
                .add("\"removals\":" + removals)
                .add("\"clears\":" + clears)
                .add("\"hitPercentage\":" + getHitPercentage())
                .toString();
    }
}