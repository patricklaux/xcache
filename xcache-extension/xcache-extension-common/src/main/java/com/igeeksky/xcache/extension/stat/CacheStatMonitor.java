package com.igeeksky.xcache.extension.stat;

import com.igeeksky.xcache.props.StoreLevel;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 缓存指标采集类
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public class CacheStatMonitor {

    private final String name;
    private final String application;

    private final AtomicLong hitLoads = new AtomicLong();
    private final AtomicLong missLoads = new AtomicLong();
    private AtomicReference<CacheStatCounter> noop = null;
    private AtomicReference<CacheStatCounter> first = null;
    private AtomicReference<CacheStatCounter> second = null;
    private AtomicReference<CacheStatCounter> third = null;

    public CacheStatMonitor(StatConfig config) {
        this.name = config.getName();
        this.application = config.getApp();
    }

    public void incHits(StoreLevel level, long times) {
        if (times > 0) {
            getCounter(level).incHits(times);
        }
    }

    public void incMisses(StoreLevel level, long times) {
        if (times > 0) {
            getCounter(level).incMisses(times);
        }
    }

    public void incPuts(StoreLevel level, long times) {
        if (times > 0) {
            getCounter(level).incPuts(times);
        }
    }

    public void incHitLoads(long times) {
        if (times > 0) {
            hitLoads.getAndAdd(times);
        }
    }

    public void incMissLoads(long times) {
        if (times > 0) {
            missLoads.getAndAdd(times);
        }
    }

    public void incRemovals(StoreLevel level, long times) {
        if (times > 0) {
            getCounter(level).incRemovals(times);
        }
    }

    public void incClears(StoreLevel level) {
        getCounter(level).incClears();
    }

    /**
     * 采集缓存统计信息
     *
     * @return {@link CacheStatMessage} 缓存统计信息
     */
    public CacheStatMessage collect() {
        CacheStatMessage message = new CacheStatMessage(name, application);
        message.setHitLoads(this.hitLoads.getAndSet(0));
        message.setMissLoads(this.missLoads.getAndSet(0));

        if (noop != null) {
            CacheStatCounter counter = noop.getAndSet(new CacheStatCounter());
            message.setNoop(convert(counter));
        }

        if (first != null) {
            CacheStatCounter counter = first.getAndSet(new CacheStatCounter());
            message.setFirst(convert(counter));
        }

        if (second != null) {
            CacheStatCounter counter = second.getAndSet(new CacheStatCounter());
            message.setSecond(convert(counter));
        }

        if (third != null) {
            CacheStatCounter counter = third.getAndSet(new CacheStatCounter());
            message.setThird(convert(counter));
        }

        return message;
    }

    private CacheStatistics convert(CacheStatCounter counter) {
        CacheStatistics stat = new CacheStatistics();
        stat.setHits(counter.getHits());
        stat.setMisses(counter.getMisses());
        stat.setPuts(counter.getPuts());
        stat.setRemovals(counter.getRemovals());
        stat.setClears(counter.getClears());
        return stat;
    }

    public void setCounter(StoreLevel level) {
        if (StoreLevel.FIRST == level) {
            first = new AtomicReference<>(new CacheStatCounter());
        } else if (StoreLevel.SECOND == level) {
            second = new AtomicReference<>(new CacheStatCounter());
        } else if (StoreLevel.THIRD == level) {
            third = new AtomicReference<>(new CacheStatCounter());
        } else {
            noop = new AtomicReference<>(new CacheStatCounter());
        }
    }

    private CacheStatCounter getCounter(StoreLevel level) {
        if (StoreLevel.FIRST == level) {
            return first.get();
        } else if (StoreLevel.SECOND == level) {
            return second.get();
        } else if (StoreLevel.THIRD == level) {
            return third.get();
        } else {
            return noop.get();
        }
    }

}