package com.igeeksky.xcache.redis.lettuce;

import com.igeeksky.xcache.redis.RedisHashWriter;
import com.igeeksky.xcache.redis.RedisPropertiesKey;
import com.igeeksky.xcache.redis.RedisStringWriter;
import com.igeeksky.xcache.redis.RedisWriterProvider;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.StringUtils;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-27
 */
public class LettuceRedisWriterProvider implements RedisWriterProvider {

    private final Map<String, Object> metadata;
    private final AbstractRedisClient abstractRedisClient;

    public LettuceRedisWriterProvider(HashMap<String, Object> metadata) {
        this.metadata = metadata;
        this.abstractRedisClient = createRedisClient(this.metadata);
    }

    private AbstractRedisClient createRedisClient(Map<String, Object> metadata) {
        // TODO 连接池、哨兵、集群配置
        RedisURI redisURI;
        String uri = Maps.getString(metadata, RedisPropertiesKey.URI);
        if (StringUtils.hasLength(uri)) {
            redisURI = RedisURI.create(uri);
            return RedisClient.create(redisURI);
        }

        String host = Maps.getString(metadata, RedisPropertiesKey.HOST);
        int port = Maps.getInteger(metadata, RedisPropertiesKey.PORT, 6379);
        redisURI = RedisURI.create(host, port);

        Long timeout = Maps.getLong(metadata, RedisPropertiesKey.TIMEOUT);
        if (null != timeout) {
            redisURI.setTimeout(Duration.ofSeconds(timeout));
        }

        return RedisClient.create(redisURI);
    }

    @Override
    public RedisStringWriter getRedisStringWriter() {
        return new LettuceRedisStringWriter(abstractRedisClient);
    }

    @Override
    public RedisHashWriter getRedisHashWriter() {
        return new LettuceRedisHashWriter(abstractRedisClient);
    }
}
