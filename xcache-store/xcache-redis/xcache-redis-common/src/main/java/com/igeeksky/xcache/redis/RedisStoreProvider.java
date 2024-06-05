package com.igeeksky.xcache.redis;

import com.igeeksky.xcache.config.CacheConfig;
import com.igeeksky.xcache.extension.serializer.StringSerializer;
import com.igeeksky.xcache.store.RemoteStore;
import com.igeeksky.xcache.store.RemoteStoreProvider;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-27
 */
public class RedisStoreProvider implements RemoteStoreProvider {

    private final RedisConnection redisConnection;

    private final RedisConnectionFactory connectionFactory;

    public RedisStoreProvider(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.redisConnection = connectionFactory.getConnection();
    }

    @Override
    public RemoteStore getRemoteCacheStore(CacheConfig<?, ?> config) {
        Charset charset = config.getCharset();
        StringSerializer serializer = StringSerializer.getInstance(charset);
        String storeName = StringUtils.toLowerCase(config.getRemoteConfig().getStoreName());
        if (storeName == null || Objects.equals(storeName, RedisStringStore.STORE_NAME)) {
            return new RedisStringStore(config, serializer, this.redisConnection);
        }
        return new RedisHashStore(config, serializer, this.redisConnection);
    }

    @Override
    public void close() throws Exception {
        this.connectionFactory.close();
    }
}
