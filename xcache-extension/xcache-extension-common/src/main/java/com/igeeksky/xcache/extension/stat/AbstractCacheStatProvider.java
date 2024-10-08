package com.igeeksky.xcache.extension.stat;

import com.igeeksky.xtool.core.concurrent.VirtualThreadFactory;
import com.igeeksky.xtool.core.lang.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 缓存指标采集统计抽象类
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-25
 */
public abstract class AbstractCacheStatProvider implements CacheStatProvider {

    private static final Logger log = LoggerFactory.getLogger(AbstractCacheStatProvider.class);

    private final ExecutorService executor = executor();

    private final Map<String, CacheStatMonitor> monitors = new ConcurrentHashMap<>();

    public AbstractCacheStatProvider(ScheduledExecutorService scheduler, long period) {
        Assert.isTrue(period > 0L, "stat period must be greater than 0");
        scheduler.scheduleAtFixedRate(() -> {
            try {
                executor.submit(() -> {
                    try {
                        Collection<CacheStatMonitor> values = monitors.values();
                        List<CacheStatMessage> messages = new ArrayList<>(values.size());
                        for (CacheStatMonitor monitor : values) {
                            messages.add(monitor.collect());
                        }
                        this.publish(messages);
                    } catch (Throwable e) {
                        log.error("CacheStatProvider PublishTask has error:{}", e.getMessage(), e);
                    }
                });
            } catch (Throwable e) {
                log.error("CacheStatProvider PublishTask has error:{}", e.getMessage(), e);
            }
        }, period, period, TimeUnit.MILLISECONDS);
    }

    @Override
    public CacheStatMonitor getMonitor(StatConfig config) {
        return monitors.computeIfAbsent(config.getName(), ignored -> new CacheStatMonitorImpl(config));
    }

    private static ExecutorService executor() {
        return Executors.newThreadPerTaskExecutor(new VirtualThreadFactory("cache-stat-thread-"));
    }

}