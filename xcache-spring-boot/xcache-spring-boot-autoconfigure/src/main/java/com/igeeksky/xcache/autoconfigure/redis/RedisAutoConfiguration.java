package com.igeeksky.xcache.autoconfigure.redis;

import com.igeeksky.xcache.autoconfigure.CacheManagerConfiguration;
import com.igeeksky.xcache.autoconfigure.holder.CacheSyncProviderHolder;
import com.igeeksky.xcache.autoconfigure.holder.RemoteStoreProviderHolder;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(CacheManagerConfiguration.class)
public class RedisAutoConfiguration {

    private final RedisProperties redisProperties;

    public RedisAutoConfiguration(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean
    RemoteStoreProviderHolder redisCacheStoreProviderHolder(RedisConnectionFactoryHolder factoryHolder) {
        RemoteStoreProviderHolder holder = new RemoteStoreProviderHolder();

        List<RedisProperties.Store> stores = redisProperties.getStores();
        if (CollectionUtils.isNotEmpty(stores)) {
            for (RedisProperties.Store store : stores) {
                String id = StringUtils.trim(store.getId());
                String connection = StringUtils.trim(store.getConnection());
                Assert.notNull(id, () -> "redis:stores: [" + store + "] id must not be null or empty.");
                Assert.notNull(connection, () -> "redis:stores: [" + store + "] connection must not be null or empty.");
                RedisConnectionFactory factory = factoryHolder.get(connection);
                RedisStoreProvider storeProvider = new RedisStoreProvider(factory);
                holder.put(id, storeProvider);
            }
        }

        return holder;
    }

    @Bean
    CacheSyncProviderHolder redisCacheSyncProviderHolder(RedisConnectionFactoryHolder factoryHolder) {
        CacheSyncProviderHolder holder = new CacheSyncProviderHolder();

        List<RedisProperties.Store> stores = redisProperties.getSyncs();
        if (CollectionUtils.isNotEmpty(stores)) {
            for (RedisProperties.Store store : stores) {
                String id = StringUtils.trim(store.getId());
                String connection = StringUtils.trim(store.getConnection());
                Assert.notNull(id, () -> "redis:syncs: [" + store + "] id must not be null or empty.");
                Assert.notNull(connection, () -> "redis:syncs: [" + store + "] connection must not be null or empty.");
                RedisConnectionFactory factory = factoryHolder.get(connection);
                RedisCacheSyncManager syncManager = new RedisCacheSyncManager(factory);
                holder.put(id, syncManager);
            }
        }

        return holder;
    }

}