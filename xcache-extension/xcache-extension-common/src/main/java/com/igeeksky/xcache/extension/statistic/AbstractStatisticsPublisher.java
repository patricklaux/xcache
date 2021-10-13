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
public abstract class AbstractStatisticsPublisher implements CacheStatisticsPublisher {

    private static final Logger logger = LoggerFactory.getLogger(AbstractStatisticsPublisher.class);

    private final ConcurrentMap<String, CacheStatisticsMonitor<?, ?>> holderMap = new ConcurrentHashMap<>();

    public AbstractStatisticsPublisher() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> {
            try {
                Collection<CacheStatisticsMonitor<?, ?>> holders = holderMap.values();
                List<CacheStatisticsMessage> messages = new ArrayList<>(holders.size());
                for (CacheStatisticsMonitor<?, ?> cacheStatisticsMonitor : holders) {
                    CacheStatisticsMessage collect = cacheStatisticsMonitor.collect();
                    messages.add(collect);
                }
                publish(messages);
            } catch (Throwable e) {
                logger.error("PublishTask has error: " + e.getMessage(), e);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    public <K, V> CacheStatisticsMonitor<K, V> getStatisticsMonitor(String name, String storeType, String application) {
        String key = name + storeType + application;
        return (CacheStatisticsMonitor<K, V>)
                holderMap.computeIfAbsent(key, k -> new CacheStatisticsMonitor<>(name, storeType, application));
    }

}
