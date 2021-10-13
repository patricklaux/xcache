package com.igeeksky.xcache.config;

import com.igeeksky.xcache.beans.BeanContext;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 多级缓存的个性化配置
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-25
 */
public class MultiCacheProperties {

    private BeanContext beanContext;

    private String application;
    private String cacheId;
    private Charset charset;
    private String name;
    private Boolean enableCache;
    private Boolean enableUpdateListener;
    private Boolean enableUpdateBroadcast;
    private Boolean enableStatistics;
    private String cacheLoader;
    private String cacheWriter;
    private String cacheLock;
    private String cacheUpdate;
    private String containsPredicate;
    private String eventSerializer;
    private String statisticsPublisher;
    private String statisticsSerializer;

    private final List<CacheProperties> caches = new LinkedList<>();
    private final Map<String, Object> metadata = new LinkedHashMap<>(0);

    public String getApplication() {
        return application;
    }

    void setApplication(String application) {
        this.application = application;
        for (CacheProperties cacheProperties : caches) {
            cacheProperties.setApplication(application);
        }
    }

    public String getCacheId() {
        return cacheId;
    }

    void setCacheId(String cacheId) {
        this.cacheId = cacheId;
        for (CacheProperties cacheProperties : caches) {
            cacheProperties.setApplication(application);
        }
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
        for (CacheProperties cacheProperties : caches) {
            cacheProperties.setCharset(charset);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnableCache() {
        return enableCache;
    }

    public void setEnableCache(Boolean enableCache) {
        this.enableCache = enableCache;
    }

    public Boolean getEnableUpdateListener() {
        return enableUpdateListener;
    }

    public void setEnableUpdateListener(Boolean enableUpdateListener) {
        this.enableUpdateListener = enableUpdateListener;
    }

    public Boolean getEnableUpdateBroadcast() {
        return enableUpdateBroadcast;
    }

    public void setEnableUpdateBroadcast(Boolean enableUpdateBroadcast) {
        this.enableUpdateBroadcast = enableUpdateBroadcast;
    }

    public Boolean getEnableStatistics() {
        return enableStatistics;
    }

    public void setEnableStatistics(Boolean enableStatistics) {
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
        if (null != caches) {
            this.caches.addAll(caches);
        }
    }

    public void addToCaches(CacheProperties cacheProperties) {
        if (null != cacheProperties) {
            this.caches.add(cacheProperties);
        }
    }

    public BeanContext getBeanContext() {
        return beanContext;
    }

    void setBeanContext(BeanContext beanContext) {
        this.beanContext = beanContext;
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

}
