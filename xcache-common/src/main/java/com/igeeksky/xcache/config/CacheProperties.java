package com.igeeksky.xcache.config;

import com.igeeksky.xcache.beans.BeanContext;
import com.igeeksky.xcache.common.CacheLevel;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * <b>缓存配置类</b><br/>
 *
 * @author Patrick.Lau
 * @since 0.0.2 2020-12-11
 */
public class CacheProperties {

    private String application;
    private String cacheId;

    private String defaultStore;
    private CacheLevel cacheLevel;
    private Generic generic;
    private Local local;
    private Remote remote;
    private Caffeine caffeine;
    private Redis redis;
    private List<String> stores;
    private BeanContext beanContext;

    public String getApplication() {
        return application;
    }

    void setApplication(String application) {
        this.application = application;
    }

    public String getCacheId() {
        return cacheId;
    }

    void setCacheId(String cacheId) {
        this.cacheId = cacheId;
    }

    void setCharset(Charset charset) {
        if (null != generic) {
            generic.setCharset(charset);
        }
        if (null != local) {
            local.setCharset(charset);
        }
        if (null != remote) {
            remote.setCharset(charset);
        }
        if (null != caffeine) {
            caffeine.setCharset(charset);
        }
        if (null != redis) {
            redis.setCharset(charset);
        }
    }

    public String getDefaultStore() {
        return defaultStore;
    }

    public void setDefaultStore(String defaultStore) {
        this.defaultStore = defaultStore;
    }

    public CacheLevel getCacheLevel() {
        return cacheLevel;
    }

    public void setCacheLevel(CacheLevel cacheLevel) {
        this.cacheLevel = cacheLevel;
    }

    public Generic getGeneric() {
        return generic;
    }

    public void setGeneric(Generic generic) {
        this.generic = generic;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public Remote getRemote() {
        return remote;
    }

    public void setRemote(Remote remote) {
        this.remote = remote;
    }

    public Caffeine getCaffeine() {
        return caffeine;
    }

    public void setCaffeine(Caffeine caffeine) {
        this.caffeine = caffeine;
    }

    public Redis getRedis() {
        return redis;
    }

    public void setRedis(Redis redis) {
        this.redis = redis;
    }

    public List<String> getStores() {
        return stores;
    }

    public void setStores(List<String> stores) {
        this.stores = stores;
    }

    public BeanContext getBeanContext() {
        return beanContext;
    }

    public void setBeanContext(BeanContext beanContext) {
        this.beanContext = beanContext;
    }


    /**
     * 缓存基本配置
     *
     * @author Patrick.Lau
     * @since 0.0.4 2021-09-28
     */
    public static class Generic {

        private Charset charset;

        private CacheLevel cacheLevel;
        private Boolean enableNullValue;
        private Boolean enableStatistics;
        private String statisticsPublisher;
        private String statisticsSerializer;

        private final Map<String, Object> metadata = new LinkedHashMap<>(0);

        public Charset getCharset() {
            return charset;
        }

        void setCharset(Charset charset) {
            this.charset = charset;
        }

        public CacheLevel getCacheLevel() {
            return cacheLevel;
        }

        public void setCacheLevel(CacheLevel cacheLevel) {
            this.cacheLevel = cacheLevel;
        }

        public Boolean getEnableNullValue() {
            return enableNullValue;
        }

        public boolean isEnableNullValue() {
            return null != enableNullValue ? enableNullValue : true;
        }

        public void setEnableNullValue(Boolean enableNullValue) {
            this.enableNullValue = enableNullValue;
        }

        public Boolean getEnableStatistics() {
            return enableStatistics;
        }

        public boolean isEnableStatistics() {
            return null != enableStatistics ? enableStatistics : false;
        }

        public void setEnableStatistics(Boolean enableStatistics) {
            this.enableStatistics = enableStatistics;
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

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public Object getFromMetadata(String key) {
            return metadata.get(key);
        }

        public void setMetadata(Map<String, Object> metadata) {
            if (null != metadata) {
                this.metadata.putAll(metadata);
            }
        }

        public void addToMetadata(String key, Object value) {
            if (null != key) {
                metadata.put(key, value);
            }
        }

    }

    /**
     * @author Patrick.Lau
     * @since 0.0.4 2021-09-28
     */
    public static class Local extends Generic {

        private Boolean enableSerializeValue;
        private Boolean enableCompressValue;
        private String valueSerializer;
        private String valueCompressor;

        public Boolean getEnableSerializeValue() {
            return enableSerializeValue;
        }

        public boolean isEnableSerializeValue() {
            return null != enableSerializeValue ? enableSerializeValue : false;
        }

        public void setEnableSerializeValue(Boolean enableSerializeValue) {
            this.enableSerializeValue = enableSerializeValue;
        }

        public Boolean getEnableCompressValue() {
            return enableCompressValue;
        }

        public boolean isEnableCompressValue() {
            return null != enableCompressValue ? enableCompressValue : false;
        }

        public void setEnableCompressValue(Boolean enableCompressValue) {
            this.enableCompressValue = enableCompressValue;
        }

        public String getValueSerializer() {
            return valueSerializer;
        }

        public void setValueSerializer(String valueSerializer) {
            this.valueSerializer = valueSerializer;
        }

        public String getValueCompressor() {
            return valueCompressor;
        }

        public void setValueCompressor(String valueCompressor) {
            this.valueCompressor = valueCompressor;
        }

    }

    /**
     * @author Patrick.Lau
     * @since 0.0.4 2021-09-28
     */
    public static class Remote extends Generic {

        private boolean enableCompressValue;
        private String keySerializer;
        private String valueSerializer;
        private String valueCompressor;

        public boolean isEnableCompressValue() {
            return enableCompressValue;
        }

        public void setEnableCompressValue(boolean enableCompressValue) {
            this.enableCompressValue = enableCompressValue;
        }

        public String getKeySerializer() {
            return keySerializer;
        }

        public void setKeySerializer(String keySerializer) {
            this.keySerializer = keySerializer;
        }

        public String getValueSerializer() {
            return valueSerializer;
        }

        public void setValueSerializer(String valueSerializer) {
            this.valueSerializer = valueSerializer;
        }

        public String getValueCompressor() {
            return valueCompressor;
        }

        public void setValueCompressor(String valueCompressor) {
            this.valueCompressor = valueCompressor;
        }
    }

    /**
     * @author Patrick.Lau
     * @since 0.0.4 2021-09-28
     */
    public static class Caffeine extends Local {

        private Integer initialCapacity;
        private Long maximumSize;
        private Long maximumWeight;
        private String weigher;
        private String keyStrength;
        private String valueStrength;
        private Long expireAfterWrite;
        private Long expireAfterAccess;

        public Integer getInitialCapacity() {
            return initialCapacity;
        }

        public void setInitialCapacity(Integer initialCapacity) {
            this.initialCapacity = initialCapacity;
        }

        public Long getMaximumSize() {
            return maximumSize;
        }

        public void setMaximumSize(Long maximumSize) {
            this.maximumSize = maximumSize;
        }

        public Long getMaximumWeight() {
            return maximumWeight;
        }

        public void setMaximumWeight(Long maximumWeight) {
            this.maximumWeight = maximumWeight;
        }

        public String getWeigher() {
            return weigher;
        }

        public void setWeigher(String weigher) {
            this.weigher = weigher;
        }

        public String getKeyStrength() {
            return keyStrength;
        }

        public void setKeyStrength(String keyStrength) {
            this.keyStrength = keyStrength;
        }

        public String getValueStrength() {
            return valueStrength;
        }

        public void setValueStrength(String valueStrength) {
            this.valueStrength = valueStrength;
        }

        public Long getExpireAfterWrite() {
            return expireAfterWrite;
        }

        public void setExpireAfterWrite(Long expireAfterWrite) {
            this.expireAfterWrite = expireAfterWrite;
        }

        public Long getExpireAfterAccess() {
            return expireAfterAccess;
        }

        public void setExpireAfterAccess(Long expireAfterAccess) {
            this.expireAfterAccess = expireAfterAccess;
        }
    }

    /**
     * @author Patrick.Lau
     * @since 0.0.4 2021-09-28
     */
    public static class Redis extends Remote {

        private String storeType;
        private Long expireAfterWrite;
        private String namespace;
        private boolean enableKeyPrefix;

        public String getStoreType() {
            return storeType;
        }

        public void setStoreType(String storeType) {
            this.storeType = storeType;
        }

        public Long getExpireAfterWrite() {
            return expireAfterWrite;
        }

        public void setExpireAfterWrite(Long expireAfterWrite) {
            this.expireAfterWrite = expireAfterWrite;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public boolean isEnableKeyPrefix() {
            return enableKeyPrefix;
        }

        public void setEnableKeyPrefix(boolean enableKeyPrefix) {
            this.enableKeyPrefix = enableKeyPrefix;
        }
    }
}
