package com.igeeksky.xcache.extension.statistic;

import java.util.StringJoiner;

/**
 * 缓存统计信息
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public class CacheStatMessage {

    private String name;
    private String application;

    private long loads;
    private CacheStatistics noop;
    private CacheStatistics local;
    private CacheStatistics remote;

    public CacheStatMessage() {
    }

    public CacheStatMessage(String name, String application) {
        this.name = name;
        this.application = application;
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

    public long getLoads() {
        return loads;
    }

    public void setLoads(long loads) {
        this.loads = loads;
    }

    public CacheStatistics getNoop() {
        return noop;
    }

    public void setNoop(CacheStatistics noop) {
        this.noop = noop;
    }

    public CacheStatistics getLocal() {
        return local;
    }

    public void setLocal(CacheStatistics local) {
        this.local = local;
    }

    public CacheStatistics getRemote() {
        return remote;
    }

    public void setRemote(CacheStatistics remote) {
        this.remote = remote;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "{", "}")
                .add("\"name\":\"" + name + "\"")
                .add("\"application\":\"" + application + "\"")
                .add("\"loads\":" + loads)
                .add("\"noop\":" + noop)
                .add("\"local\":" + local)
                .add("\"remote\":" + remote)
                .toString();
    }
}
