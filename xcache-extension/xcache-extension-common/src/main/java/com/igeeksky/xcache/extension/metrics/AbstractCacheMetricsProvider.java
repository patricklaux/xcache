package com.igeeksky.xcache.extension.metrics;

import com.igeeksky.xtool.core.lang.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 缓存指标采集统计抽象类
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-25
 */
public abstract class AbstractCacheMetricsProvider implements CacheMetricsProvider {

    private static final Logger log = LoggerFactory.getLogger(AbstractCacheMetricsProvider.class);

    private final ScheduledFuture<?> scheduledFuture;

    private final Map<String, CacheMetricsMonitor> monitors = new ConcurrentHashMap<>();

    public AbstractCacheMetricsProvider(ScheduledExecutorService scheduler, long interval) {
        Assert.isTrue(interval > 0L, "stat interval must be greater than 0");
        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            try {
                Collection<CacheMetricsMonitor> values = monitors.values();
                List<CacheMetricsMessage> messages = new ArrayList<>(values.size());
                for (CacheMetricsMonitor monitor : values) {
                    messages.add(monitor.collect());
                }
                this.publish(messages);
            } catch (Throwable e) {
                log.error("CacheStatProvider PublishTask has error:{}", e.getMessage(), e);
            }
        }, interval, interval, TimeUnit.MILLISECONDS);
    }

    @Override
    public CacheMetricsMonitor getMonitor(MetricsConfig config) {
        return monitors.computeIfAbsent(config.getName(), ignored -> new CacheMetricsMonitorImpl(config));
    }

    @Override
    public void close() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }

}