package com.igeeksky.xcache.extension.metrics;

import com.igeeksky.xcache.props.StoreLevel;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 缓存指标采集类
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-11
 */
public class CacheMetricsMonitorImpl implements CacheMetricsMonitor {

    private final String name;
    private final String group;

    private final AtomicLong hitLoads = new AtomicLong();
    private final AtomicLong missLoads = new AtomicLong();
    private AtomicReference<CacheMetricsCounter> noop = null;
    private AtomicReference<CacheMetricsCounter> first = null;
    private AtomicReference<CacheMetricsCounter> second = null;
    private AtomicReference<CacheMetricsCounter> third = null;

    public CacheMetricsMonitorImpl(MetricsConfig config) {
        this.name = config.getName();
        this.group = config.getGroup();
    }

    @Override
    public void incHits(StoreLevel level, long times) {
        if (times > 0) {
            getCounter(level).incHits(times);
        }
    }

    @Override
    public void incMisses(StoreLevel level, long times) {
        if (times > 0) {
            getCounter(level).incMisses(times);
        }
    }

    @Override
    public void incPuts(StoreLevel level, long times) {
        if (times > 0) {
            getCounter(level).incPuts(times);
        }
    }

    @Override
    public void incHitLoads(long times) {
        if (times > 0) {
            hitLoads.getAndAdd(times);
        }
    }

    @Override
    public void incMissLoads(long times) {
        if (times > 0) {
            missLoads.getAndAdd(times);
        }
    }

    @Override
    public void incRemovals(StoreLevel level, long times) {
        if (times > 0) {
            getCounter(level).incRemovals(times);
        }
    }

    @Override
    public void incClears(StoreLevel level) {
        getCounter(level).incClears();
    }

    /**
     * 采集缓存统计信息
     *
     * @return {@link CacheMetricsMessage} 缓存统计信息
     */
    @Override
    public CacheMetricsMessage collect() {
        CacheMetricsMessage message = new CacheMetricsMessage(name, group);
        message.setHitLoads(this.hitLoads.getAndSet(0));
        message.setMissLoads(this.missLoads.getAndSet(0));

        if (noop != null) {
            CacheMetricsCounter counter = noop.getAndSet(new CacheMetricsCounter());
            message.setNoop(convert(counter));
        }

        if (first != null) {
            CacheMetricsCounter counter = first.getAndSet(new CacheMetricsCounter());
            message.setFirst(convert(counter));
        }

        if (second != null) {
            CacheMetricsCounter counter = second.getAndSet(new CacheMetricsCounter());
            message.setSecond(convert(counter));
        }

        if (third != null) {
            CacheMetricsCounter counter = third.getAndSet(new CacheMetricsCounter());
            message.setThird(convert(counter));
        }

        return message;
    }

    private CacheMetrics convert(CacheMetricsCounter counter) {
        CacheMetrics stat = new CacheMetrics();
        stat.setHits(counter.getHits());
        stat.setMisses(counter.getMisses());
        stat.setPuts(counter.getPuts());
        stat.setRemovals(counter.getRemovals());
        stat.setClears(counter.getClears());
        return stat;
    }

    @Override
    public void setCounter(StoreLevel level) {
        if (StoreLevel.FIRST == level) {
            first = new AtomicReference<>(new CacheMetricsCounter());
        } else if (StoreLevel.SECOND == level) {
            second = new AtomicReference<>(new CacheMetricsCounter());
        } else if (StoreLevel.THIRD == level) {
            third = new AtomicReference<>(new CacheMetricsCounter());
        } else {
            noop = new AtomicReference<>(new CacheMetricsCounter());
        }
    }

    private CacheMetricsCounter getCounter(StoreLevel level) {
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