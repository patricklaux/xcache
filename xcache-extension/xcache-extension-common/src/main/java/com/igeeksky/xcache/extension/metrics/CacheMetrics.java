package com.igeeksky.xcache.extension.metrics;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 缓存统计信息
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public class CacheMetrics {

    private long hits;

    private long misses;

    private long puts;

    private long removals;

    private long clears;

    private Float hitPercentage;

    public CacheMetrics() {
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

    public void setHitPercentage(Float hitPercentage) {
        this.hitPercentage = hitPercentage;
    }

    public Float getHitPercentage() {
        if (hitPercentage == null) {
            long gets = this.hits + this.misses;
            if (gets > 0) {
                BigDecimal percentage = new BigDecimal(this.hits).divide(new BigDecimal(gets), 6, RoundingMode.HALF_UP);
                hitPercentage = Float.parseFloat(percentage.toPlainString());
            } else {
                hitPercentage = 0F;
            }
        }
        return hitPercentage;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }
}