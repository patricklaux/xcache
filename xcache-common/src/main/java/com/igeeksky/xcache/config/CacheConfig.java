package com.igeeksky.xcache.config;

import com.igeeksky.xcache.extension.contains.ContainsPredicate;
import com.igeeksky.xcache.extension.contains.TrueContainsPredicate;
import com.igeeksky.xcache.extension.convertor.KeyConvertor;
import com.igeeksky.xcache.extension.loader.CacheLoader;
import com.igeeksky.xcache.extension.lock.CacheLock;
import com.igeeksky.xcache.extension.lock.LocalCacheLock;
import com.igeeksky.xcache.extension.statistic.CacheStatMonitor;
import com.igeeksky.xcache.extension.sync.CacheSyncMonitor;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-09
 */
public class CacheConfig<K, V> {

    private String name;

    private String application;

    private Charset charset;

    private Class<K> keyType;

    private Class<V> valueType;

    private CacheLock cacheLock;

    private KeyConvertor keyConvertor;

    private CacheLoader<K, V> cacheLoader;

    private CacheStatMonitor statMonitor;

    private CacheSyncMonitor syncMonitor;

    private ContainsPredicate<K> containsPredicate;

    private LocalConfig<V> localConfig = new LocalConfig<>();

    private RemoteConfig<V> remoteConfig = new RemoteConfig<>();

    private Map<String, Object> metadata = new HashMap<>();

    public CacheConfig() {
    }

    public CacheConfig(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Class<K> getKeyType() {
        return keyType;
    }

    public void setKeyType(Class<K> keyType) {
        this.keyType = keyType;
    }

    public Class<V> getValueType() {
        return valueType;
    }

    public void setValueType(Class<V> valueType) {
        this.valueType = valueType;
    }

    public CacheLock getCacheLock() {
        if (cacheLock != null) {
            return cacheLock;
        }
        return new LocalCacheLock<>();
    }

    public void setCacheLock(CacheLock cacheLock) {
        this.cacheLock = cacheLock;
    }

    public KeyConvertor getKeyConvertor() {
        return keyConvertor;
    }

    public void setKeyConvertor(KeyConvertor keyConvertor) {
        this.keyConvertor = keyConvertor;
    }

    public CacheLoader<K, V> getCacheLoader() {
        return cacheLoader;
    }

    public void setCacheLoader(CacheLoader<K, V> cacheLoader) {
        this.cacheLoader = cacheLoader;
    }

    public CacheStatMonitor getStatMonitor() {
        return statMonitor;
    }

    public void setStatMonitor(CacheStatMonitor statMonitor) {
        this.statMonitor = statMonitor;
    }

    public CacheSyncMonitor getSyncMonitor() {
        return syncMonitor;
    }

    public void setSyncMonitor(CacheSyncMonitor syncMonitor) {
        this.syncMonitor = syncMonitor;
    }

    public ContainsPredicate<K> getContainsPredicate() {
        if (containsPredicate != null) {
            return containsPredicate;
        }
        return TrueContainsPredicate.getInstance();
    }

    public void setContainsPredicate(ContainsPredicate<K> containsPredicate) {
        this.containsPredicate = containsPredicate;
    }

    public LocalConfig<V> getLocalConfig() {
        return localConfig;
    }

    public void setLocalConfig(LocalConfig<V> localConfig) {
        this.localConfig = localConfig;
    }

    public RemoteConfig<V> getRemoteConfig() {
        return remoteConfig;
    }

    public void setRemoteConfig(RemoteConfig<V> remoteConfig) {
        this.remoteConfig = remoteConfig;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

}
