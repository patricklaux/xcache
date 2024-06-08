package com.igeeksky.xcache.extension.statistic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * 缓存指标采集
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-25
 */
public abstract class AbstractCacheStatManager implements CacheStatManager {

    private static final Logger log = LoggerFactory.getLogger(AbstractCacheStatManager.class);

    private final long period;
    private final ScheduledExecutorService executorService;
    private final ConcurrentMap<String, CacheStatMonitor> holderMap = new ConcurrentHashMap<>();

    public AbstractCacheStatManager(long period) {
        this.period = period;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.executorService.scheduleAtFixedRate(() -> {
            try {
                Collection<CacheStatMonitor> monitors = holderMap.values();
                List<CacheStatMessage> messages = new ArrayList<>(monitors.size());
                for (CacheStatMonitor monitor : monitors) {
                    messages.add(monitor.collect());
                }
                publish(messages);
            } catch (Throwable e) {
                log.error("CacheStatManager PublishTask has error", e);
            }
        }, period, period, TimeUnit.MILLISECONDS);
    }

    @Override
    public void register(String name, CacheStatMonitor monitor) {
        holderMap.put(name, monitor);
    }

    public long getPeriod() {
        return period;
    }

    @Override
    public void close() {
        this.holderMap.clear();
        if (executorService.isShutdown()) {
            return;
        }
        executorService.shutdown();
    }

}
