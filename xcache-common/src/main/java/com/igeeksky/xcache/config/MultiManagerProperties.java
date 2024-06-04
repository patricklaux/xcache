package com.igeeksky.xcache.config;

import com.igeeksky.xcache.beans.BeanContext;
import com.igeeksky.xcache.common.CacheLevel;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 多级缓存管理器配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-03
 */
public class MultiManagerProperties {
    private final Object lock = new Object();

    private String application;
    private volatile String cacheId;
    private volatile Charset charset;

    private boolean enableCache = true;
    private boolean enableCacheProxy = true;

    private boolean enableUpdateListener = false;
    private boolean enableUpdateBroadcast = false;
    private boolean enableStatistics = false;
    private String cacheLoader;
    private String cacheWriter;
    private String cacheLock = "localCacheLockProvider";
    private String cacheUpdate;
    private String containsPredicate = "alwaysTruePredicateProvider";
    private String eventSerializer;
    private String statisticsPublisher;
    private String statisticsSerializer;

    private List<CacheProperties> caches;
    private BeanContext beanContext;
    private final Map<String, Object> metadata = new LinkedHashMap<>(0);

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getCacheId() {
        if (null == cacheId) {
            synchronized (lock) {
                if (null == cacheId) {
                    cacheId = UUID.randomUUID().toString();
                }
            }
        }
        return cacheId;
    }

    /**
     * 如果不设置，由缓存自动生成：{@link UUID#randomUUID()#toString}
     *
     * @param cacheId 缓存ID
     */
    public void setCacheId(String cacheId) {
        this.cacheId = cacheId;
    }

    public Charset getCharset() {
        if (null == charset) {
            synchronized (lock) {
                if (null == charset) {
                    charset = StandardCharsets.UTF_8;
                }
            }
        }
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public boolean isEnableCache() {
        return enableCache;
    }

    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    public boolean isEnableCacheProxy() {
        return enableCacheProxy;
    }

    public void setEnableCacheProxy(boolean enableCacheProxy) {
        this.enableCacheProxy = enableCacheProxy;
    }

    public boolean isEnableUpdateListener() {
        return enableUpdateListener;
    }

    public void setEnableUpdateListener(boolean enableUpdateListener) {
        this.enableUpdateListener = enableUpdateListener;
    }

    public boolean isEnableUpdateBroadcast() {
        return enableUpdateBroadcast;
    }

    public void setEnableUpdateBroadcast(boolean enableUpdateBroadcast) {
        this.enableUpdateBroadcast = enableUpdateBroadcast;
    }

    public boolean isEnableStatistics() {
        return enableStatistics;
    }

    public void setEnableStatistics(boolean enableStatistics) {
        this.enableStatistics = enableStatistics;
    }

    public String getCacheLoader() {
        return cacheLoader;
    }

    public void setCacheLoader(String cacheLoader) {
        this.cacheLoader = cacheLoader;
    }

    public String getCacheWriter() {
        return cacheWriter;
    }

    public void setCacheWriter(String cacheWriter) {
        this.cacheWriter = cacheWriter;
    }

    public String getCacheLock() {
        return cacheLock;
    }

    public void setCacheLock(String cacheLock) {
        this.cacheLock = cacheLock;
    }

    public String getCacheUpdate() {
        return cacheUpdate;
    }

    public void setCacheUpdate(String cacheUpdate) {
        this.cacheUpdate = cacheUpdate;
    }

    public String getContainsPredicate() {
        return containsPredicate;
    }

    public void setContainsPredicate(String containsPredicate) {
        this.containsPredicate = containsPredicate;
    }

    public String getEventSerializer() {
        return eventSerializer;
    }

    public void setEventSerializer(String eventSerializer) {
        this.eventSerializer = eventSerializer;
    }

    public String getStatisticsPublisher() {
        return statisticsPublisher;
    }

    public void setStatisticsPublisher(String statisticsPublisher) {
        this.statisticsPublisher = statisticsPublisher;
    }

    public String getStatisticsSerializer() {
        return statisticsSerializer;
    }

    public void setStatisticsSerializer(String statisticsSerializer) {
        this.statisticsSerializer = statisticsSerializer;
    }

    public List<CacheProperties> getCaches() {
        return caches;
    }

    public void setCaches(List<CacheProperties> caches) {
        this.caches = caches;
    }

    public BeanContext getBeanContext() {
        return beanContext;
    }

    public void setBeanContext(BeanContext beanContext) {
        this.beanContext = beanContext;
        caches.forEach(cacheProperties -> cacheProperties.setBeanContext(beanContext));
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        if (null != metadata) {
            this.metadata.putAll(metadata);
        }
    }

    public Object getFromMetadata(String key) {
        return metadata.get(key);
    }

    public void addToMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public TreeSet<CacheLevel> getCacheLevelSet() {
        TreeSet<CacheLevel> levelSet = new TreeSet<>(Comparator.comparing(Enum::name));
        for (CacheProperties properties : caches) {
            levelSet.add(properties.getCacheLevel());
        }
        return levelSet;
    }

    public Map<CacheLevel, CacheProperties> getCachesMap() {
        Map<CacheLevel, CacheProperties> map = new HashMap<>(caches.size());
        for (CacheProperties properties : caches) {
            map.put(properties.getCacheLevel(), properties);
        }
        return map;
    }
}
