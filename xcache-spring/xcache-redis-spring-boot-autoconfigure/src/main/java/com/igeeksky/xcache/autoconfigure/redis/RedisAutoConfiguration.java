package com.igeeksky.xcache.autoconfigure.redis;

import com.igeeksky.redis.RedisOperatorFactory;
import com.igeeksky.redis.stream.StreamListenerContainer;
import com.igeeksky.xcache.autoconfigure.CacheAutoConfiguration;
import com.igeeksky.xcache.autoconfigure.SchedulerAutoConfiguration;
import com.igeeksky.xcache.autoconfigure.holder.*;
import com.igeeksky.xcache.redis.lock.RedisLockProvider;
import com.igeeksky.xcache.redis.refresh.RedisCacheRefreshProvider;
import com.igeeksky.xcache.redis.stat.RedisCacheStatProvider;
import com.igeeksky.xcache.redis.stat.RedisStatConfig;
import com.igeeksky.xcache.redis.store.RedisStoreProvider;
import com.igeeksky.xcache.redis.sync.RedisCacheSyncProvider;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({CacheAutoConfiguration.class})
@AutoConfigureAfter({SchedulerAutoConfiguration.class})
public class RedisAutoConfiguration {

    private final RedisProperties redisProperties;

    public RedisAutoConfiguration(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean
    StoreProviderHolder redisCacheStoreProviderHolder(ObjectProvider<RedisOperatorFactoryHolder> provider) {
        StoreProviderHolder holder = new StoreProviderHolder();

        List<RedisProperties.StoreOption> options = redisProperties.getStore();
        if (CollectionUtils.isEmpty(options)) {
            return holder;
        }

        Map<String, RedisOperatorFactory> map = getOperatorFactoryMap(provider);

        for (RedisProperties.StoreOption option : options) {
            String id = StringUtils.trimToNull(option.getId());
            String fid = StringUtils.trimToNull(option.getFactory());
            Assert.notNull(id, () -> "redis:store: " + option + ", id must not be null or empty.");
            Assert.notNull(fid, () -> "redis:store: " + option + ", factory must not be null or empty.");
            RedisOperatorFactory factory = map.get(fid);
            Assert.notNull(factory, () -> "redis:store: [" + fid + "] RedisOperatorFactory instance doesn't exist.");
            holder.put(id, new RedisStoreProvider(factory));
        }

        return holder;
    }


    @Bean
    ListenerContainerHolder listenerContainerHolder(ObjectProvider<RedisOperatorFactoryHolder> provider,
                                                    ScheduledExecutorService scheduler) {
        ListenerContainerHolder holder = new ListenerContainerHolder();

        List<RedisProperties.ListenerOption> options = redisProperties.getListener();
        if (CollectionUtils.isEmpty(options)) {
            return holder;
        }

        Map<String, RedisOperatorFactory> map = getOperatorFactoryMap(provider);

        for (RedisProperties.ListenerOption option : options) {
            String id = StringUtils.trimToNull(option.getId());
            String fid = StringUtils.trimToNull(option.getFactory());
            Assert.notNull(id, () -> "redis:listener: " + option + ", id must not be null or empty.");
            Assert.notNull(fid, () -> "redis:listener: " + option + ", factory must not be null or empty.");

            RedisOperatorFactory factory = map.get(fid);
            Assert.notNull(factory, () -> "redis:listener: [" + fid + "] RedisOperatorFactory instance doesn't exist.");
            long block = option.getBlock();
            long count = option.getCount();
            long delay = option.getDelay();
            holder.put(id, new StreamListenerContainer(scheduler, factory, block, count, delay));
        }

        return holder;
    }


    @Bean
    CacheSyncProviderHolder redisCacheSyncProviderHolder(ObjectProvider<ListenerContainerHolder> provider) {
        CacheSyncProviderHolder holder = new CacheSyncProviderHolder();
        List<RedisProperties.SyncOption> options = redisProperties.getSync();
        if (CollectionUtils.isEmpty(options)) {
            return holder;
        }

        Map<String, StreamListenerContainer> map = getListenerContainerMap(provider);

        for (RedisProperties.SyncOption option : options) {
            String id = StringUtils.trim(option.getId());
            String cid = StringUtils.trimToNull(option.getListener());
            Assert.notNull(id, () -> "redis:sync: " + option + ", id must not be null or empty.");
            Assert.notNull(cid, () -> "redis:sync: " + option + ", container must not be null or empty.");
            StreamListenerContainer container = map.get(cid);
            Assert.notNull(container, () -> "redis:sync: [" + cid + "] StreamListenerContainer instance doesn't exist.");
            holder.put(id, new RedisCacheSyncProvider(container));
        }

        return holder;
    }


    @Bean
    CacheLockProviderHolder cacheLockProviderHolder(ObjectProvider<RedisOperatorFactoryHolder> provider,
                                                    ScheduledExecutorService scheduler) {
        CacheLockProviderHolder holder = new CacheLockProviderHolder();
        List<RedisProperties.LockOption> options = redisProperties.getLock();
        if (CollectionUtils.isEmpty(options)) {
            return holder;
        }

        Map<String, RedisOperatorFactory> map = getOperatorFactoryMap(provider);

        for (RedisProperties.LockOption option : options) {
            String id = StringUtils.trim(option.getId());
            String fid = StringUtils.trimToNull(option.getFactory());
            Assert.notNull(id, () -> "redis:lock: " + option + ", id must not be null or empty.");
            Assert.notNull(fid, () -> "redis:lock: " + option + ", factory must not be null or empty.");
            RedisOperatorFactory factory = map.get(fid);
            Assert.notNull(factory, () -> "redis:lock: [" + fid + "] RedisOperatorFactory instance doesn't exist.");
            holder.put(id, new RedisLockProvider(scheduler, factory));
        }

        return holder;
    }


    @Bean
    CacheRefreshProviderHolder redisCacheRefreshProviderHolder(ObjectProvider<RedisOperatorFactoryHolder> provider,
                                                               ScheduledExecutorService scheduler) {
        CacheRefreshProviderHolder holder = new CacheRefreshProviderHolder();
        List<RedisProperties.RefreshOption> options = redisProperties.getRefresh();
        if (CollectionUtils.isEmpty(options)) {
            return holder;
        }

        Map<String, RedisOperatorFactory> map = getOperatorFactoryMap(provider);

        for (RedisProperties.RefreshOption option : options) {
            String id = StringUtils.trim(option.getId());
            String fid = StringUtils.trimToNull(option.getFactory());
            Assert.notNull(id, () -> "redis:refresh: " + option + ", id must not be null or empty.");
            Assert.notNull(fid, () -> "redis:refresh: " + option + ", factory must not be null or empty.");
            RedisOperatorFactory factory = map.get(fid);
            Assert.notNull(factory, () -> "redis:refresh: [" + fid + "] RedisOperatorFactory instance doesn't exist.");
            holder.put(id, new RedisCacheRefreshProvider(scheduler, factory.getRedisOperator()));
        }

        return holder;
    }


    @Bean
    CacheStatProviderHolder redisCacheStatProviderHolder(ObjectProvider<RedisOperatorFactoryHolder> provider,
                                                         ScheduledExecutorService scheduler) {
        CacheStatProviderHolder holder = new CacheStatProviderHolder();
        List<RedisProperties.StatOption> options = redisProperties.getStat();
        if (CollectionUtils.isEmpty(options)) {
            return holder;
        }

        Map<String, RedisOperatorFactory> map = getOperatorFactoryMap(provider);

        for (RedisProperties.StatOption option : options) {
            String id = StringUtils.trim(option.getId());
            String fid = StringUtils.trimToNull(option.getFactory());
            Assert.notNull(id, () -> "redis:stat: " + option + ", id must not be null or empty.");
            Assert.notNull(fid, () -> "redis:stat: " + option + ", factory must not be null or empty.");
            RedisOperatorFactory factory = map.get(fid);
            Assert.notNull(factory, () -> "redis:stat: [" + fid + "] RedisOperatorFactory instance doesn't exist.");
            RedisStatConfig config = RedisStatConfig.builder()
                    .maxLen(option.getMaxLen())
                    .period(option.getPeriod())
                    .suffix(option.getSuffix())
                    .operator(factory.getRedisOperator())
                    .scheduler(scheduler)
                    .build();
            holder.put(id, new RedisCacheStatProvider(config));
        }

        return holder;
    }

    private static Map<String, RedisOperatorFactory> getOperatorFactoryMap(ObjectProvider<RedisOperatorFactoryHolder> provider) {
        Map<String, RedisOperatorFactory> map = new HashMap<>();
        provider.orderedStream().forEach(factoryHolder -> {
            Map<String, RedisOperatorFactory> factories = factoryHolder.getAll();
            factories.forEach((id, factory) -> {
                RedisOperatorFactory old = map.put(id, factory);
                Assert.isTrue(old == null, () -> "RedisOperatorFactory: [" + id + "] duplicate id.");
            });
        });
        return map;
    }

    private static Map<String, StreamListenerContainer> getListenerContainerMap(ObjectProvider<ListenerContainerHolder> provider) {
        Map<String, StreamListenerContainer> map = new HashMap<>();
        provider.orderedStream().forEach(containerHolder -> {
            Map<String, StreamListenerContainer> containers = containerHolder.getAll();
            containers.forEach((id, container) -> {
                StreamListenerContainer old = map.put(id, container);
                Assert.isTrue(old == null, () -> "StreamListenerContainer: [" + id + "] duplicate id.");
            });
        });
        return map;
    }

}
