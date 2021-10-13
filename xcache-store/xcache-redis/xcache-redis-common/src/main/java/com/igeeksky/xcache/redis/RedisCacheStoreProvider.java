package com.igeeksky.xcache.redis;

import com.igeeksky.xcache.Cache;
import com.igeeksky.xcache.beans.BeanContext;
import com.igeeksky.xcache.config.CacheConfigException;
import com.igeeksky.xcache.config.CacheProperties;
import com.igeeksky.xcache.extension.ExtensionHelper;
import com.igeeksky.xcache.extension.compress.Compressor;
import com.igeeksky.xcache.extension.monitor.CacheMonitor;
import com.igeeksky.xcache.extension.serialization.Serializer;
import com.igeeksky.xcache.store.AbstractCacheStoreProvider;
import com.igeeksky.xcache.util.StringUtils;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-21
 */
public class RedisCacheStoreProvider extends AbstractCacheStoreProvider {

    private final RedisWriterProvider redisWriterProvider;

    public RedisCacheStoreProvider(RedisWriterProvider redisWriterProvider) {
        this.redisWriterProvider = redisWriterProvider;
    }

    @Override
    public <K, V> Cache<K, V> get(String name, CacheProperties cacheProperties, Class<K> keyType, Class<V> valueType) {
        CacheProperties.Redis redis = cacheProperties.getRedis();

        Charset charset = redis.getCharset();
        BeanContext beanContext = cacheProperties.getBeanContext();

        String keySerializerId = redis.getKeySerializer();
        Serializer<K> keySerializer = ExtensionHelper.keySerializer(keySerializerId, beanContext, keyType, charset);

        String valueSerializerId = redis.getValueSerializer();
        Serializer<V> valueSerializer = ExtensionHelper.valueSerializer(valueSerializerId, beanContext, valueType, charset);

        String valueCompressorId = redis.getValueCompressor();
        Compressor valueCompressor = ExtensionHelper.valueCompressor(valueCompressorId, beanContext);

        String storeType = StringUtils.toLowerCase(redis.getStoreType());
        if (null == storeType || Objects.equals(RedisStringCacheStore.STORE_TYPE, storeType)) {
            storeType = RedisStringCacheStore.STORE_TYPE;
        } else {
            if (Objects.equals(RedisHashCacheStore.STORE_TYPE, storeType)) {
                storeType = RedisHashCacheStore.STORE_TYPE;
            } else {
                throw new CacheConfigException("Name:" + name + ", the storeType:" + storeType + " is incorrect");
            }
        }

        String statisticsId = redis.getStatisticsPublisher();
        boolean enableStatistics = redis.isEnableStatistics();
        String application = cacheProperties.getApplication();

        List<CacheMonitor<K, V>> cacheMonitors = ExtensionHelper.statisticsMonitor(statisticsId, beanContext,
                enableStatistics, name, storeType, application);

        if (Objects.equals(RedisStringCacheStore.STORE_TYPE, storeType)) {
            RedisStringWriter redisStringWriter = redisWriterProvider.getRedisStringWriter();
            return new RedisStringCacheStore<>(name, redis, keyType, valueType, cacheMonitors, redisStringWriter,
                    keySerializer, valueSerializer, valueCompressor);
        }

        RedisHashWriter redisHashWriter = redisWriterProvider.getRedisHashWriter();
        return new RedisHashCacheStore<>(name, redis, keyType, valueType, cacheMonitors, redisHashWriter,
                keySerializer, valueSerializer, valueCompressor);
    }

}
