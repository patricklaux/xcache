package com.igeeksky.xcache.extension.statistic;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-26
 */
public class CacheStatisticsMessage implements Serializable {

    private String name;
    private String application;
    private String store;
    private long collectTime;
    private long nullHits;
    private long notNullHits;
    private long nullLoads;
    private long notNullLoads;
    private long misses;
    private long puts;
    private long removals;
    private long clears;

    public CacheStatisticsMessage() {
    }

    public CacheStatisticsMessage(String name, String storeType, String application, CacheStatisticsCounter counter) {
        this.name = name;
        this.application = application;
        this.store = storeType;
        this.collectTime = System.currentTimeMillis();
        this.setNotNullHits(counter.getNotNullHits());
        this.setNullHits(counter.getNullHits());
        this.setNotNullLoads(counter.getNotNullLoads());
        this.setNullLoads(counter.getNullLoads());
        this.setMisses(counter.getMisses());
        this.setPuts(counter.getPuts());
        this.setRemovals(counter.getRemovals());
        this.setClears(counter.getClears());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public long getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(long collectTime) {
        this.collectTime = collectTime;
    }

    public long getNullHits() {
        return nullHits;
    }

    public void setNullHits(long nullHits) {
        this.nullHits = nullHits;
    }

    public long getNotNullHits() {
        return notNullHits;
    }

    public void setNotNullHits(long notNullHits) {
        this.notNullHits = notNullHits;
    }

    public long getNullLoads() {
        return nullLoads;
    }

    public void setNullLoads(long nullLoads) {
        this.nullLoads = nullLoads;
    }

    public long getNotNullLoads() {
        return notNullLoads;
    }

    public void setNotNullLoads(long notNullLoads) {
        this.notNullLoads = notNullLoads;
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
        long hits = nullHits + notNullHits;
        long gets = hits + misses;
        if (gets <= 0) {
            return 0.0F;
        }

        BigDecimal hitPercentage = new BigDecimal(hits).divide(new BigDecimal(gets), 6, RoundingMode.HALF_UP);
        return Float.parseFloat(hitPercentage.toPlainString());
    }

    @Override
    public String toString() {
        return "{\"name\":\"" +
                getName() +
                "\", \"application\":\"" +
                getApplication() +
                "\", \"store\":\"" +
                getStore() +
                "\", \"collectTime\":" +
                getCollectTime() +
                ", \"nullHits\":" +
                getNullHits() +
                ", \"notNullHits\":" +
                getNotNullHits() +
                ", \"nullLoads\":" +
                getNullLoads() +
                ", \"notNullLoads\":" +
                getNotNullLoads() +
                ", \"misses\":" +
                getMisses() +
                ", \"hitPercentage\":" +
                getHitPercentage() +
                ", \"puts\":" +
                getPuts() +
                ", \"removals\":" +
                getRemovals() +
                ", \"clears\":" +
                getClears() +
                "}";
    }

}
