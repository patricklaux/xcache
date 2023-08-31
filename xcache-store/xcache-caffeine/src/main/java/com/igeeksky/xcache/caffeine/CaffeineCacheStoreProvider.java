package com.igeeksky.xcache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Weigher;
import com.igeeksky.xcache.beans.BeanContext;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.ExpiryCacheValue;
import com.igeeksky.xcache.config.CacheConfigException;
import com.igeeksky.xcache.config.CacheProperties;
import com.igeeksky.xcache.extension.ExtensionHelper;
import com.igeeksky.xcache.extension.compress.Compressor;
import com.igeeksky.xcache.extension.monitor.CacheMonitor;
import com.igeeksky.xcache.extension.serialization.Serializer;
import com.igeeksky.xcache.store.AbstractCacheStoreProvider;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-21
 */
public class CaffeineCacheStoreProvider extends AbstractCacheStoreProvider {

    @Override
    public <K, V> com.igeeksky.xcache.Cache<K, V> get(String name, CacheProperties cacheProperties, Class<K> keyType, Class<V> valueType) {
        CacheProperties.Caffeine caffeine = cacheProperties.getCaffeine();
        Charset charset = caffeine.getCharset();
        BeanContext beanContext = cacheProperties.getBeanContext();
        Serializer<V> valueSerializer = null;
        if (caffeine.isEnableSerializeValue()) {
            String id = caffeine.getValueSerializer();
            valueSerializer = ExtensionHelper.valueSerializer(id, beanContext, valueType, charset);
            Objects.requireNonNull(valueSerializer, "ValueSerializer: id=" + id + " is not predefined");
        }

        Compressor valueCompressor = null;
        if (caffeine.isEnableCompressValue()) {
            String id = caffeine.getValueCompressor();
            valueCompressor = ExtensionHelper.valueCompressor(id, beanContext);
            Objects.requireNonNull(valueCompressor, "ValueCompressor: id=" + id + " is not predefined");
        }

        String application = cacheProperties.getApplication();
        String statisticsId = caffeine.getStatisticsPublisher();
        boolean enableStatistics = caffeine.isEnableStatistics();
        List<CacheMonitor<K, V>> cacheMonitors = ExtensionHelper.statisticsMonitor(statisticsId, beanContext,
                enableStatistics, name, CaffeineExpiryCacheStore.STORE_TYPE, application);

        Long expireAfterWrite = caffeine.getExpireAfterWrite();
        Long expireAfterAccess = caffeine.getExpireAfterAccess();
        if (null != expireAfterWrite && null != expireAfterAccess) {
            RandomRangeCacheExpiry<K, Object> rangeCacheExpiry =
                    new RandomRangeCacheExpiry<>(Duration.ofMillis(expireAfterWrite), Duration.ofMillis(expireAfterAccess));
            Cache<K, ExpiryCacheValue<Object>> cache = buildExpiryCache(name, cacheProperties, keyType, rangeCacheExpiry);
            return new CaffeineExpiryCacheStore<>(name, caffeine, keyType, valueType, cacheMonitors,
                    valueSerializer, valueCompressor, cache);
        }

        Cache<K, CacheValue<Object>> cache = buildCache(name, cacheProperties, keyType, expireAfterWrite, expireAfterAccess);
        return new CaffeineCacheStore<>(name, caffeine, keyType, valueType, cacheMonitors,
                valueSerializer, valueCompressor, cache);
    }

    private <K> Cache<K, ExpiryCacheValue<Object>> buildExpiryCache(String name, CacheProperties cacheProperties, Class<K> keyType,
                                                                    RandomRangeCacheExpiry<K, Object> expiry) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        builder.expireAfter(expiry);
        setOther(name, cacheProperties, keyType, builder);
        return builder.build();
    }

    private <K> Cache<K, CacheValue<Object>> buildCache(String name, CacheProperties cacheProperties, Class<K> keyType,
                                                        Long expireAfterWrite, Long expireAfterAccess) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        if (null != expireAfterWrite) {
            builder.expireAfterWrite(Duration.ofMillis(expireAfterWrite));
        }
        if (null != expireAfterAccess) {
            builder.expireAfterAccess(Duration.ofMillis(expireAfterAccess));
        }
        setOther(name, cacheProperties, keyType, builder);
        return builder.build();
    }

    private <K> void setOther(String name, CacheProperties properties, Class<K> keyType, Caffeine<Object, Object> builder) {
        CacheProperties.Caffeine caffeine = properties.getCaffeine();
        Integer initialCapacity = caffeine.getInitialCapacity();
        if (initialCapacity != null) {
            builder.initialCapacity(initialCapacity);
        }

        Long maximumSize = caffeine.getMaximumSize();
        if (null != maximumSize) {
            builder.maximumSize(maximumSize);
        }

        Long maximumWeight = caffeine.getMaximumWeight();
        if (null != maximumWeight) {
            builder.maximumWeight(maximumWeight);
            String weigherId = StringUtils.trim(caffeine.getWeigher());
            if (null == weigherId) {
                throw new CacheConfigException("key:weigher must not be null");
            }
            BeanContext beanContext = properties.getBeanContext();
            WeigherProvider provider = ExtensionHelper.provider(weigherId, beanContext, WeigherProvider.class);
            Weigher<K, CacheValue<Object>> weigher = provider.get(name, keyType, Object.class);
            if (null == weigher) {
                throw new CacheConfigException("id=" + weigherId + ", the weigher is not predefined");
            }
            builder.weigher(weigher);
        }

        String weak = "weak";
        String soft = "soft";
        String keyStrength = StringUtils.toLowerCase(caffeine.getKeyStrength());
        if (keyStrength != null) {
            if (Objects.equals(weak, keyStrength)) {
                builder.weakKeys();
            } else {
                throw new CacheConfigException("keyStrength=" + keyStrength + " can only be set to 'weak'");
            }
        }

        String valueStrength = StringUtils.toLowerCase(caffeine.getValueStrength());
        if (valueStrength != null) {
            if (Objects.equals(weak, valueStrength)) {
                builder.weakValues();
            } else if (Objects.equals(soft, valueStrength)) {
                builder.softValues();
            } else {
                throw new CacheConfigException("valueStrength=" + valueStrength + " can only be set to 'weak' or 'soft'");
            }
        }
    }
}
