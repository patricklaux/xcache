package com.igeeksky.xcache.extension;

import com.igeeksky.xcache.beans.BeanContext;
import com.igeeksky.xcache.beans.BeanHolder;
import com.igeeksky.xcache.common.CacheInitializationException;
import com.igeeksky.xcache.extension.compress.Compressor;
import com.igeeksky.xcache.extension.compress.CompressorProvider;
import com.igeeksky.xcache.extension.lock.CacheLock;
import com.igeeksky.xcache.extension.lock.CacheLockProvider;
import com.igeeksky.xcache.extension.monitor.CacheMonitor;
import com.igeeksky.xcache.extension.serialization.CacheEventSerializer;
import com.igeeksky.xcache.extension.serialization.CacheEventSerializerProvider;
import com.igeeksky.xcache.extension.serialization.Serializer;
import com.igeeksky.xcache.extension.serialization.SerializerProvider;
import com.igeeksky.xcache.extension.statistic.CacheStatisticsMonitor;
import com.igeeksky.xcache.extension.statistic.CacheStatisticsProvider;
import com.igeeksky.xcache.extension.statistic.CacheStatisticsPublisher;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-10-04
 */
public class ExtensionHelper {

    public static <K, V> List<CacheMonitor<K, V>> statisticsMonitor(String id, BeanContext beanContext,
                                                                    boolean enableStatistics, String name,
                                                                    String storeType, String application) {
        List<CacheMonitor<K, V>> cacheMonitors = new LinkedList<>();
        if (enableStatistics) {
            CacheStatisticsProvider provider = provider(id, beanContext, CacheStatisticsProvider.class);
            if (null == provider) {
                return null;
            }
            CacheStatisticsPublisher publisher = provider.get();
            if (null == publisher) {
                return null;
            }
            CacheStatisticsMonitor<K, V> monitor = publisher.getStatisticsMonitor(name, storeType, application);
            if (null == monitor) {
                throw new CacheInitializationException("StatisticsPublisher:id=" + id
                        + ", statisticsMonitor must not be null");
            }
            cacheMonitors.add(monitor);
        }
        return cacheMonitors;
    }

    public static <K> Serializer<K> keySerializer(String id, BeanContext beanContext, Class<K> keyType, Charset charset) {
        SerializerProvider provider = provider(id, beanContext, SerializerProvider.class);
        return (null == provider) ? null : provider.get(keyType, charset);
    }

    public static <V> Serializer<V> valueSerializer(String id, BeanContext beanContext, Class<V> valueType, Charset charset) {
        SerializerProvider provider = provider(id, beanContext, SerializerProvider.class);
        return (null == provider) ? null : provider.get(valueType, charset);
    }

    public static Compressor valueCompressor(String id, BeanContext beanContext) {
        CompressorProvider provider = provider(id, beanContext, CompressorProvider.class);
        return (null == provider) ? null : provider.get();
    }

    public static <T> T provider(String id, BeanContext beanContext, Class<T> providerType) {
        if (null == id || null == beanContext) {
            return null;
        }
        BeanHolder beanHolder = beanContext.getBeanHolder(id);
        if (null == beanHolder) {
            return null;
        }
        return beanHolder.getBean(providerType);
    }

    public static <K> CacheLock<K> cacheLock(String id, BeanContext beanContext, String name, Class<K> keyType,
                                             Map<String, Object> metadata) {
        CacheLockProvider provider = provider(id, beanContext, CacheLockProvider.class);
        return (null == provider) ? null : provider.get(name, keyType, metadata);
    }

    public static <V, K> CacheEventSerializer<K, V> eventSerializer(String id, BeanContext beanContext,
                                                                    Class<K> keyType, Class<V> valueType) {
        CacheEventSerializerProvider provider = provider(id, beanContext, CacheEventSerializerProvider.class);
        return (null == provider) ? null : provider.get(keyType, valueType);
    }
}
