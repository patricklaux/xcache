package com.igeeksky.xcache.extension.statistic;

import com.igeeksky.xcache.common.StoreType;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * 缓存统计指标
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public class CacheStatMonitor {

    private final String name;
    private final String application;

    private final AtomicReference<LongAdder> loadsRef = new AtomicReference<>(new LongAdder());
    private final AtomicReference<CacheStatCounter> noopRef = new AtomicReference<>(new CacheStatCounter());
    private final AtomicReference<CacheStatCounter> localRef = new AtomicReference<>(new CacheStatCounter());
    private final AtomicReference<CacheStatCounter> remoteRef = new AtomicReference<>(new CacheStatCounter());

    public CacheStatMonitor(String name, String application) {
        this.name = name;
        this.application = application;
    }

    public void incHits(StoreType storeType, long times) {
        if (times == 0) return;
        if (StoreType.LOCAL == storeType) {
            localRef.get().incHits(times);
        } else if (StoreType.REMOTE == storeType) {
            remoteRef.get().incHits(times);
        } else {
            noopRef.get().incHits(times);
        }
    }

    public void incMisses(StoreType storeType, long times) {
        if (times == 0) return;
        if (StoreType.LOCAL == storeType) {
            localRef.get().incMisses(times);
        } else if (StoreType.REMOTE == storeType) {
            remoteRef.get().incMisses(times);
        } else {
            noopRef.get().incMisses(times);
        }
    }

    public void incPuts(StoreType storeType, long times) {
        if (times == 0) return;
        if (StoreType.LOCAL == storeType) {
            localRef.get().incPuts(times);
        } else if (StoreType.REMOTE == storeType) {
            remoteRef.get().incPuts(times);
        } else {
            noopRef.get().incPuts(times);
        }
    }

    public void incLoads() {
        loadsRef.get().increment();
    }

    public void incRemovals(StoreType storeType, long times) {
        if (times == 0) return;
        if (StoreType.LOCAL == storeType) {
            localRef.get().incRemovals(times);
        } else if (StoreType.REMOTE == storeType) {
            remoteRef.get().incRemovals(times);
        } else {
            noopRef.get().incRemovals(times);
        }
    }

    public void incClears(StoreType storeType) {
        if (StoreType.LOCAL == storeType) {
            localRef.get().incClears();
        } else if (StoreType.REMOTE == storeType) {
            remoteRef.get().incClears();
        } else {
            noopRef.get().incClears();
        }
    }

    /**
     * 采集缓存统计信息
     *
     * @return {@link CacheStatMessage} 缓存统计信息
     */
    public CacheStatMessage collect() {
        LongAdder loads = loadsRef.getAndSet(new LongAdder());
        CacheStatCounter noopCounter = noopRef.getAndSet(new CacheStatCounter());
        CacheStatCounter localCounter = localRef.getAndSet(new CacheStatCounter());
        CacheStatCounter remoteCounter = remoteRef.getAndSet(new CacheStatCounter());

        // 生成统计消息
        CacheStatMessage message = new CacheStatMessage(name, application);
        message.setLoads(loads.sum());
        message.setNoop(convert(noopCounter));
        message.setLocal(convert(localCounter));
        message.setRemote(convert(remoteCounter));
        return message;
    }

    private CacheStatistics convert(CacheStatCounter counter) {
        CacheStatistics statistics = new CacheStatistics();
        statistics.setHits(counter.getHits());
        statistics.setMisses(counter.getMisses());
        statistics.setPuts(counter.getPuts());
        statistics.setRemovals(counter.getRemovals());
        statistics.setClears(counter.getClears());
        return statistics;
    }

}
