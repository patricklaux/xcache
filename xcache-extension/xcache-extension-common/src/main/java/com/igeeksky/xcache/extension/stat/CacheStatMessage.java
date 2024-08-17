package com.igeeksky.xcache.extension.stat;

import com.igeeksky.xtool.core.json.SimpleJSON;

/**
 * 缓存统计信息
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public class CacheStatMessage {

    private String name;
    private String app;

    private long hitLoads;
    private long missLoads;
    private CacheStatistics noop;
    private CacheStatistics first;
    private CacheStatistics second;
    private CacheStatistics third;

    public CacheStatMessage() {
    }

    public CacheStatMessage(String name, String app) {
        this.name = name;
        this.app = app;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public long getHitLoads() {
        return hitLoads;
    }

    public void setHitLoads(long hitLoads) {
        this.hitLoads = hitLoads;
    }

    public long getMissLoads() {
        return missLoads;
    }

    public void setMissLoads(long missLoads) {
        this.missLoads = missLoads;
    }

    public CacheStatistics getNoop() {
        return noop;
    }

    public void setNoop(CacheStatistics noop) {
        this.noop = noop;
    }

    public CacheStatistics getFirst() {
        return first;
    }

    public void setFirst(CacheStatistics first) {
        this.first = first;
    }

    public CacheStatistics getSecond() {
        return second;
    }

    public void setSecond(CacheStatistics second) {
        this.second = second;
    }

    public CacheStatistics getThird() {
        return third;
    }

    public void setThird(CacheStatistics third) {
        this.third = third;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }
}
