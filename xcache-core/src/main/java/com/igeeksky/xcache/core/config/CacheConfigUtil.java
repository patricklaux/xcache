package com.igeeksky.xcache.core.config;

import com.igeeksky.xcache.common.CacheType;
import com.igeeksky.xcache.common.ReferenceType;
import com.igeeksky.xcache.props.*;
import com.igeeksky.xtool.core.annotation.Perfect;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * <p>配置工具类</p>
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-20
 */
@Perfect
public class CacheConfigUtil {

    @Perfect
    public static TemplateProps copyTemplateProps(TemplateProps from, TemplateProps to) {
        copyProps(from, to);
        return to;
    }

    /**
     * <p> 用户模板配置 覆盖 模板默认配置，生成用户自定义模板配置 </p>
     * <p> 用户单个缓存配置 覆盖 用户自定义模板配置，生成最终配置 </p>
     * <p> 用户配置项如果为空，则使用模板配置项(或默认配置项) </p>
     * <p> String类型，如果不希望使用该配置项，可以配置为 “none” <p>
     */
    @Perfect
    public static CacheProps copyCacheProps(CacheProps from, CacheProps to) {
        to.setName(from.getName());
        copyProps(from, to);
        return to;
    }

    private static void copyProps(AbstractProps from, AbstractProps to) {
        String charset = StringUtils.toUpperCase(from.getCharset());
        if (charset != null) {
            to.setCharset(charset);
        }

        CacheType cacheType = from.getCacheType();
        if (cacheType != null) {
            to.setCacheType(cacheType);
        }

        copyLocalProps(from.getLocal(), to.getLocal());
        copyRemoteProps(from.getRemote(), to.getRemote());
        copyExtensionProps(from.getExtension(), to.getExtension());

        to.getMetadata().putAll(from.getMetadata());
    }

    @Perfect
    private static void copyLocalProps(LocalProps from, LocalProps to) {
        String cacheStore = StringUtils.trim(from.getCacheStore());
        if (StringUtils.hasLength(cacheStore)) {
            to.setCacheStore(cacheStore);
        }

        String storeName = StringUtils.trim(from.getStoreName());
        if (StringUtils.hasLength(storeName)) {
            to.setStoreName(storeName);
        }

        Integer initialCapacity = from.getInitialCapacity();
        if (initialCapacity != null) {
            to.setInitialCapacity(initialCapacity);
        }

        Long maximumSize = from.getMaximumSize();
        if (maximumSize != null) {
            to.setMaximumSize(maximumSize);
        }

        Long maximumWeight = from.getMaximumWeight();
        if (maximumWeight != null) {
            to.setMaximumWeight(maximumWeight);
        }

        Long expireAfterWrite = from.getExpireAfterWrite();
        if (expireAfterWrite != null) {
            to.setExpireAfterWrite(expireAfterWrite);
        }

        Long expireAfterAccess = from.getExpireAfterAccess();
        if (expireAfterAccess != null) {
            to.setExpireAfterAccess(expireAfterAccess);
        }

        ReferenceType keyStrength = from.getKeyStrength();
        if (keyStrength != null) {
            to.setKeyStrength(keyStrength);
        }

        ReferenceType valueStrength = from.getValueStrength();
        if (valueStrength != null) {
            to.setValueStrength(valueStrength);
        }

        String valueSerializer = StringUtils.trim(from.getValueSerializer());
        if (StringUtils.hasLength(valueSerializer)) {
            to.setValueSerializer(valueSerializer);
        }

        String valueCompressor = StringUtils.trim(from.getValueCompressor());
        if (StringUtils.hasLength(valueCompressor)) {
            to.setValueCompressor(valueCompressor);
        }

        Boolean enableRandomTtl = from.getEnableRandomTtl();
        if (enableRandomTtl != null) {
            to.setEnableRandomTtl(enableRandomTtl);
        }

        Boolean enableNullValue = from.getEnableNullValue();
        if (enableNullValue != null) {
            to.setEnableNullValue(enableNullValue);
        }
    }

    @Perfect
    private static void copyRemoteProps(RemoteProps from, RemoteProps to) {
        String cacheStore = StringUtils.trim(from.getCacheStore());
        if (StringUtils.hasLength(cacheStore)) {
            to.setCacheStore(cacheStore);
        }

        String storeName = StringUtils.trim(from.getStoreName());
        if (StringUtils.hasLength(storeName)) {
            to.setStoreName(storeName);
        }

        Long expireAfterWrite = from.getExpireAfterWrite();
        if (expireAfterWrite != null) {
            to.setExpireAfterWrite(expireAfterWrite);
        }

        String valueSerializer = StringUtils.trim(from.getValueSerializer());
        if (StringUtils.hasLength(valueSerializer)) {
            to.setValueSerializer(valueSerializer);
        }

        String valueCompressor = StringUtils.trim(from.getValueCompressor());
        if (StringUtils.hasLength(valueCompressor)) {
            to.setValueCompressor(valueCompressor);
        }

        Boolean enableRandomTtl = from.getEnableRandomTtl();
        if (enableRandomTtl != null) {
            to.setEnableRandomTtl(enableRandomTtl);
        }

        Boolean enableKeyPrefix = from.getEnableKeyPrefix();
        if (enableKeyPrefix != null) {
            to.setEnableKeyPrefix(enableKeyPrefix);
        }

        Boolean enableNullValue = from.getEnableNullValue();
        if (enableNullValue != null) {
            to.setEnableNullValue(enableNullValue);
        }
    }

    @Perfect
    private static void copyExtensionProps(ExtensionProps from, ExtensionProps to) {
        String keyConvertor = StringUtils.trim(from.getKeyConvertor());
        if (StringUtils.hasLength(keyConvertor)) {
            to.setKeyConvertor(keyConvertor);
        }

        String cacheLock = StringUtils.trim(from.getCacheLock());
        if (StringUtils.hasLength(cacheLock)) {
            to.setCacheLock(cacheLock);
        }

        Integer cacheLockSize = from.getCacheLockSize();
        if (cacheLockSize != null) {
            to.setCacheLockSize(cacheLockSize);
        }

        String containsPredicate = StringUtils.trim(from.getContainsPredicate());
        if (StringUtils.hasLength(containsPredicate)) {
            to.setContainsPredicate(containsPredicate);
        }

        String cacheSync = StringUtils.trim(from.getCacheSync());
        if (StringUtils.hasLength(cacheSync)) {
            to.setCacheSync(cacheSync);
        }

        String cacheSyncChannel = StringUtils.trim(from.getCacheSyncChannel());
        if (StringUtils.hasLength(cacheSyncChannel)) {
            to.setCacheSyncChannel(cacheSyncChannel);
        }

        String cacheSyncSerializer = StringUtils.trim(from.getCacheSyncSerializer());
        if (StringUtils.hasLength(cacheSyncSerializer)) {
            to.setCacheSyncSerializer(cacheSyncSerializer);
        }

        String cacheStat = StringUtils.trim(from.getCacheStat());
        if (StringUtils.hasLength(cacheStat)) {
            to.setCacheStat(cacheStat);
        }
    }

    @Perfect
    public static <K, V> CacheConfig<K, V> createConfig(String application, Class<K> keyType, Class<V> valueType, CacheProps cacheProps) {
        CacheConfig<K, V> config = new CacheConfig<>();
        config.setName(cacheProps.getName());
        config.setApplication(application);
        config.setCharset(getCharset(cacheProps));
        config.setKeyType(keyType);
        config.setValueType(valueType);
        config.setLocalConfig(createLocalConfig(cacheProps.getLocal()));
        config.setRemoteConfig(createRemoteConfig(cacheProps.getRemote()));
        config.setMetadata(cacheProps.getMetadata());
        return config;
    }

    @Perfect
    private static <V> LocalConfig<V> createLocalConfig(LocalProps local) {
        LocalConfig<V> config = new LocalConfig<>();
        config.setStoreName(StringUtils.trim(local.getStoreName()));
        config.setInitialCapacity(local.getInitialCapacity());
        config.setMaximumSize(local.getMaximumSize());
        config.setMaximumWeight(local.getMaximumWeight());

        ReferenceType keyStrength = local.getKeyStrength();
        if (keyStrength != null) {
            config.setKeyStrength(keyStrength);
        }

        ReferenceType valueStrength = local.getValueStrength();
        if (valueStrength != null) {
            config.setValueStrength(valueStrength);
        }

        config.setExpireAfterWrite(local.getExpireAfterWrite());
        config.setExpireAfterAccess(local.getExpireAfterAccess());
        config.setEnableRandomTtl(local.getEnableRandomTtl());
        config.setEnableNullValue(local.getEnableNullValue());

        String valueCompressor = local.getValueCompressor();
        if (valueCompressor != null && !Objects.equals(CacheConstants.NONE, StringUtils.toUpperCase(valueCompressor))) {
            config.setEnableCompressValue(true);
        }
        String valueSerializer = local.getValueSerializer();
        if (valueSerializer != null && !Objects.equals(CacheConstants.NONE, StringUtils.toUpperCase(valueSerializer))) {
            config.setEnableSerializeValue(true);
        }
        return config;
    }

    @Perfect
    private static <V> RemoteConfig<V> createRemoteConfig(RemoteProps remote) {
        RemoteConfig<V> config = new RemoteConfig<>();
        config.setStoreName(remote.getStoreName());
        config.setExpireAfterWrite(remote.getExpireAfterWrite());
        config.setEnableKeyPrefix(remote.getEnableKeyPrefix());
        config.setEnableRandomTtl(remote.getEnableRandomTtl());
        config.setEnableNullValue(remote.getEnableNullValue());
        String valueCompressor = remote.getValueCompressor();
        if (valueCompressor != null && !Objects.equals(CacheConstants.NONE, StringUtils.toUpperCase(valueCompressor))) {
            config.setEnableCompressValue(true);
        }
        return config;
    }

    @Perfect
    private static Charset getCharset(CacheProps cacheProps) {
        String charsetName = StringUtils.toUpperCase(cacheProps.getCharset());
        if (charsetName != null) {
            return Charset.forName(charsetName);
        } else {
            return StandardCharsets.UTF_8;
        }
    }

    @Perfect
    public static TemplateProps defaultTemplateProps(String templateId) {
        TemplateProps props = new TemplateProps();
        props.setTemplateId(templateId);
        props.setCharset(CacheConstants.DEFAULT_CHARSET_NAME);
        props.setCacheType(CacheConstants.DEFAULT_CACHE_TYPE);
        props.setLocal(defaultLocalProps());
        props.setRemote(defaultRemoteProps());
        props.setExtension(defaultExtensionProps());
        return props;
    }

    @Perfect
    private static LocalProps defaultLocalProps() {
        LocalProps localProps = new LocalProps();
        localProps.setCacheStore(CacheConstants.DEFAULT_LOCAL_CACHE_STORE);
        localProps.setStoreName(CacheConstants.DEFAULT_LOCAL_STORE_NAME);
        localProps.setInitialCapacity(CacheConstants.DEFAULT_LOCAL_INITIAL_CAPACITY);
        localProps.setMaximumSize(CacheConstants.DEFAULT_LOCAL_MAXIMUM_SIZE);
        localProps.setMaximumWeight(CacheConstants.DEFAULT_LOCAL_MAXIMUM_WEIGHT);
        localProps.setExpireAfterWrite(CacheConstants.DEFAULT_LOCAL_EXPIRE_AFTER_WRITE);
        localProps.setExpireAfterAccess(CacheConstants.DEFAULT_LOCAL_EXPIRE_AFTER_ACCESS);
        localProps.setKeyStrength(CacheConstants.DEFAULT_LOCAL_KEY_STRENGTH);
        localProps.setValueStrength(CacheConstants.DEFAULT_LOCAL_VALUE_STRENGTH);
        localProps.setValueSerializer(CacheConstants.DEFAULT_LOCAL_VALUE_SERIALIZER);
        localProps.setValueCompressor(CacheConstants.DEFAULT_LOCAL_VALUE_COMPRESSOR);
        localProps.setEnableRandomTtl(CacheConstants.DEFAULT_LOCAL_ENABLE_RANDOM_TTL);
        localProps.setEnableNullValue(CacheConstants.DEFAULT_LOCAL_ENABLE_NULL_VALUE);
        return localProps;
    }

    @Perfect
    private static RemoteProps defaultRemoteProps() {
        RemoteProps remoteProps = new RemoteProps();
        remoteProps.setCacheStore(CacheConstants.DEFAULT_REMOTE_CACHE_STORE);
        remoteProps.setStoreName(CacheConstants.DEFAULT_REMOTE_STORE_NAME);
        remoteProps.setExpireAfterWrite(CacheConstants.DEFAULT_REMOTE_EXPIRE_AFTER_WRITE);
        remoteProps.setValueSerializer(CacheConstants.DEFAULT_REMOTE_VALUE_SERIALIZER);
        remoteProps.setValueCompressor(CacheConstants.DEFAULT_REMOTE_VALUE_COMPRESSOR);
        remoteProps.setEnableKeyPrefix(CacheConstants.DEFAULT_REMOTE_ENABLE_KEY_PREFIX);
        remoteProps.setEnableRandomTtl(CacheConstants.DEFAULT_REMOTE_ENABLE_RANDOM_TTL);
        remoteProps.setEnableNullValue(CacheConstants.DEFAULT_REMOTE_ENABLE_NULL_VALUE);
        return remoteProps;
    }

    @Perfect
    private static ExtensionProps defaultExtensionProps() {
        ExtensionProps extensionProps = new ExtensionProps();
        extensionProps.setKeyConvertor(CacheConstants.DEFAULT_EXTENSION_KEY_CONVERTOR);
        extensionProps.setCacheLock(CacheConstants.DEFAULT_EXTENSION_CACHE_LOCK);
        extensionProps.setCacheLockSize(CacheConstants.DEFAULT_EXTENSION_CACHE_LOCK_SIZE);
        extensionProps.setContainsPredicate(CacheConstants.DEFAULT_EXTENSION_CONTAINS_PREDICATE);
        extensionProps.setCacheSync(CacheConstants.DEFAULT_EXTENSION_CACHE_SYNC);
        extensionProps.setCacheSyncChannel(CacheConstants.DEFAULT_EXTENSION_CACHE_SYNC_CHANNEL);
        extensionProps.setCacheSyncSerializer(CacheConstants.DEFAULT_EXTENSION_CACHE_SYNC_SERIALIZER);
        extensionProps.setCacheStat(CacheConstants.DEFAULT_EXTENSION_CACHE_STAT);
        extensionProps.setCacheLoader(CacheConstants.DEFAULT_EXTENSION_CACHE_LOADER);
        return extensionProps;
    }

}