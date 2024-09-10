package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.*;
import com.igeeksky.xcache.core.store.StoreConfig;
import com.igeeksky.xcache.core.store.StoreProvider;
import com.igeeksky.xcache.extension.codec.CodecConfig;
import com.igeeksky.xcache.extension.codec.CodecProvider;
import com.igeeksky.xcache.extension.compress.CompressConfig;
import com.igeeksky.xcache.extension.compress.CompressorProvider;
import com.igeeksky.xcache.extension.contains.ContainsPredicate;
import com.igeeksky.xcache.extension.contains.ContainsPredicateProvider;
import com.igeeksky.xcache.extension.contains.PredicateConfig;
import com.igeeksky.xcache.extension.lock.*;
import com.igeeksky.xcache.extension.refresh.CacheRefreshProvider;
import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xcache.extension.stat.CacheStatMonitor;
import com.igeeksky.xcache.extension.stat.CacheStatProvider;
import com.igeeksky.xcache.extension.stat.StatConfig;
import com.igeeksky.xcache.extension.sync.CacheSyncMonitor;
import com.igeeksky.xcache.extension.sync.CacheSyncProvider;
import com.igeeksky.xcache.extension.sync.SyncConfig;
import com.igeeksky.xcache.extension.sync.SyncMessageListener;
import com.igeeksky.xcache.props.*;
import com.igeeksky.xtool.core.lang.StringUtils;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.codec.KeyCodec;
import com.igeeksky.xtool.core.lang.compress.Compressor;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * 缓存管理者实现类
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-13
 */
@SuppressWarnings("unchecked")
public class CacheManagerImpl implements CacheManager {

    private final String app;
    private final String sid = UUID.randomUUID().toString();

    private final ComponentManager componentManager;

    private final ConcurrentMap<String, CacheProps> caches = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Template> templates = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Cache<?, ?>> cached = new ConcurrentHashMap<>();

    public CacheManagerImpl(CacheManagerConfig managerConfig) {
        this.app = managerConfig.getApp();
        this.componentManager = managerConfig.getComponentManager();

        managerConfig.getTemplates().forEach((id, template) -> {
            Template finalTemplate = PropsUtil.replaceTemplate(template, PropsUtil.defaultTemplate(id));
            this.templates.put(id, finalTemplate);
        });

        this.caches.putAll(managerConfig.getCaches());
    }

    @Override
    public <K, V> Cache<K, V> getOrCreateCache(String cacheName, Class<K> keyType, Class<?>[] keyParams,
                                               Class<V> valueType, Class<?>[] valueParams) {
        String name = StringUtils.trimToNull(cacheName);
        requireNonNull(keyType, () -> "keyType must not be null");
        requireNonNull(valueType, () -> "valueType must not be null");
        requireNonNull(name, () -> "cacheName must not be null or empty");
        return (Cache<K, V>) cached.computeIfAbsent(name, k -> createCache(k, keyType, keyParams,
                valueType, valueParams));
    }

    private <K, V> Cache<K, V> createCache(String name, Class<K> keyType, Class<?>[] keyParams,
                                           Class<V> valueType, Class<?>[] valueParams) {
        // 1. 获取配置
        CacheProps cacheProps = this.getOrCreateCacheProps(name);

        // 2. 创建 CacheConfig
        CacheConfig<K, V> cacheConfig = this.buildCacheConfig(cacheProps, keyType, keyParams, valueType, valueParams);

        Store<V>[] stores = new Store[3];
        stores[0] = this.getStore(cacheProps.getFirst(), cacheConfig);
        stores[1] = this.getStore(cacheProps.getSecond(), cacheConfig);
        stores[2] = this.getStore(cacheProps.getThird(), cacheConfig);

        CacheLoader<K, V> cacheLoader = componentManager.getCacheLoader(name);
        CacheWriter<K, V> cacheWriter = componentManager.getCacheWriter(name);

        if (CacheBuilder.count(stores) == 0) {
            return new NoopCache<>(cacheConfig, cacheLoader, cacheWriter);
        }

        CodecConfig<K> keyCodecConfig = this.buildKeyCodecConfig(cacheConfig);
        KeyCodec<K> keyCodec = this.getKeyCodec(cacheProps.getKeyCodec(), keyCodecConfig);

        LockConfig lockConfig = this.buildLockConfig(cacheProps.getCacheLock(), cacheConfig);
        LockService cacheLock = this.getCacheLock(lockConfig);

        PredicateConfig<K> predicateConfig = this.buildPredicateConfig(cacheProps.getContainsPredicate(), cacheConfig);
        ContainsPredicate<K> predicate = this.getContainsPredicate(predicateConfig);

        RefreshConfig refreshConfig = this.buildRefreshConfig(cacheProps.getCacheRefresh(), cacheLock, cacheConfig);
        CacheRefresh cacheRefresh = this.getCacheRefresh(refreshConfig);

        StatConfig statConfig = this.buildStatConfig(cacheProps.getCacheStat(), cacheConfig);
        CacheStatMonitor statMonitor = this.getStatMonitor(statConfig);

        SyncConfig<V> syncConfig = this.buildSyncConfig(cacheProps.getCacheSync(), stores, cacheConfig);
        CacheSyncMonitor syncMonitor = this.getSyncMonitor(syncConfig);

        ExtendConfig.Builder<K, V> extendBuilder = ExtendConfig.builder();
        extendBuilder.cacheLoader(cacheLoader)
                .cacheWriter(cacheWriter)
                .cacheLock(cacheLock)
                .keyCodec(keyCodec)
                .statMonitor(statMonitor)
                .syncMonitor(syncMonitor)
                .containsPredicate(predicate)
                .cacheRefresh(cacheRefresh)
                .build();

        return CacheBuilder.builder(cacheConfig)
                .extendConfig(extendBuilder.build())
                .firstStore(stores[0])
                .secondStore(stores[1])
                .thirdStore(stores[2])
                .build();
    }

    /**
     * 通过缓存名称获取用户缓存配置
     * <p>
     * “用户缓存配置”覆盖“最终模板配置”，生成“最终缓存配置”
     *
     * @param name 缓存名称
     * @return {@link CacheProps} 缓存配置
     */
    private CacheProps getOrCreateCacheProps(String name) {
        return caches.compute(name, (nameKey, userProps) -> {
            if (userProps == null) {
                userProps = new CacheProps(nameKey);
            }

            // 获取 “模板 ID”
            String templateId = getTemplateId(userProps.getTemplateId());
            userProps.setTemplateId(templateId);

            // 获取 “最终模板配置”
            Template template = templates.get(templateId);
            requireNonNull(template, () -> "cache:[" + nameKey + "], template:[" + templateId + "] doesn't exist.");

            // “最终模板配置” 转换为 “初始缓存配置”
            CacheProps initProps = PropsUtil.buildCacheProps(nameKey, template);

            // “用户缓存配置” 覆盖 “初始缓存配置”，生成 “最终缓存配置”
            return PropsUtil.replaceCacheProps(userProps, initProps);
        });
    }

    private static String getTemplateId(String templateId) {
        String id = StringUtils.trimToNull(templateId);
        return id != null ? id : CacheConstants.DEFAULT_TEMPLATE_ID;
    }

    private <K, V> CacheConfig<K, V> buildCacheConfig(CacheProps cacheProps, Class<K> keyType, Class<?>[] keyParams,
                                                      Class<V> valueType, Class<?>[] valueParams) {
        return CacheConfig.builder(keyType, keyParams, valueType, valueParams)
                .sid(this.sid)
                .name(cacheProps.getName())
                .app(this.app)
                .charset(cacheProps.getCharset())
                .build();
    }

    private <K, V> Store<V> getStore(StoreProps storeProps, CacheConfig<K, V> cacheConfig) {
        String beanId = storeProps.getProvider();
        if (beanId == null || Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(beanId))) {
            return null;
        }

        String name = cacheConfig.getName();
        StoreConfig<V> storeConfig = StoreConfig.builder(cacheConfig.getValueType(), cacheConfig.getValueParams())
                .name(name)
                .app(cacheConfig.getApp())
                .charset(cacheConfig.getCharset())
                .provider(beanId)
                .storeType(storeProps.getStoreType())
                .initialCapacity(storeProps.getInitialCapacity())
                .maximumSize(storeProps.getMaximumSize())
                .maximumWeight(storeProps.getMaximumWeight())
                .keyStrength(storeProps.getKeyStrength())
                .valueStrength(storeProps.getValueStrength())
                .expireAfterWrite(storeProps.getExpireAfterWrite())
                .expireAfterAccess(storeProps.getExpireAfterAccess())
                .enableKeyPrefix(storeProps.getEnableKeyPrefix())
                .enableRandomTtl(storeProps.getEnableRandomTtl())
                .enableNullValue(storeProps.getEnableNullValue())
                .redisType(storeProps.getRedisType())
                .valueCodec(this.getValueCodec(storeProps.getValueCodec(), cacheConfig))
                .valueCompressor(this.getCompressor(storeProps.getValueCompressor()))
                .params(storeProps.getParams())
                .build();

        StoreProvider storeProvider = componentManager.getStoreProvider(beanId);
        requireNonNull(beanId, () -> "Cache:[" + name + "], CacheStoreProvider:[" + beanId + "] is undefined.");

        Store<V> store = storeProvider.getStore(storeConfig);
        requireNonNull(store, () -> "Cache:[" + name + "], Unable to get store from beanId:[" + beanId + "].");

        return store;
    }

    private <K, V> Codec<V> getValueCodec(String beanId, CacheConfig<K, V> cacheConfig) {
        CodecConfig<V> codecConfig = CodecConfig.builder(cacheConfig.getValueType(), cacheConfig.getValueParams())
                .name(cacheConfig.getName())
                .charset(cacheConfig.getCharset())
                .build();

        return this.getCodec(beanId, codecConfig);
    }

    private <V> Codec<V> getCodec(String beanId, CodecConfig<V> config) {
        if (beanId == null || Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(beanId))) {
            return null;
        }

        String name = config.getName();

        CodecProvider codecProvider = componentManager.getCodecProvider(beanId);
        requireNonNull(codecProvider, () -> "Cache:[" + name + "], CodecProvider:[" + beanId + "] is undefined.");

        Codec<V> codec = codecProvider.getCodec(config);
        requireNonNull(codec, () -> "Cache:[" + name + "], unable to get codec from codecProvider:[" + beanId + "].");

        return codec;
    }

    private Compressor getCompressor(CompressProps props) {
        CompressConfig compressConfig = CompressConfig.builder()
                .provider(props.getProvider())
                .level(props.getLevel())
                .nowrap(props.getNowrap())
                .build();

        return this.getCompressor(compressConfig);
    }

    private Compressor getCompressor(CompressConfig config) {
        String beanId = config.getProvider();
        if (beanId == null || Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(beanId))) {
            return null;
        }

        CompressorProvider compressorProvider = componentManager.getCompressorProvider(beanId);
        requireNonNull(compressorProvider, () -> "CompressorProvider:[" + beanId + "] is undefined.");

        Compressor compressor = compressorProvider.get(config);
        requireNonNull(compressor, () -> "Unable to get compressor from compressorProvider:[" + beanId + "].");

        return compressor;
    }

    private <K, V> CodecConfig<K> buildKeyCodecConfig(CacheConfig<K, V> cacheConfig) {
        return CodecConfig.builder(cacheConfig.getKeyType(), cacheConfig.getKeyParams())
                .name(cacheConfig.getName())
                .charset(cacheConfig.getCharset())
                .build();
    }

    private <K> KeyCodec<K> getKeyCodec(String beanId, CodecConfig<K> config) {
        String name = config.getName();

        requireNonNull(beanId, () -> "Cache:[" + name + "], KeyCodec must not be null.");

        if (Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(beanId))) {
            throw new CacheConfigException("KeyCodec is required and cannot be set to 'none'.");
        }

        CodecProvider provider = componentManager.getCodecProvider(beanId);
        requireNonNull(provider, () -> "Cache:[" + name + "], CodecProvider:[" + beanId + "] is undefined.");

        KeyCodec<K> keyCodec = provider.getKeyCodec(config);
        requireNonNull(keyCodec, () -> "Unable to get KeyCodec from provider:[" + beanId + "].");

        return keyCodec;
    }

    private <K, V> LockConfig buildLockConfig(LockProps props, CacheConfig<K, V> config) {
        return LockConfig.builder()
                .sid(config.getSid())
                .name(config.getName())
                .charset(config.getCharset())
                .infix((props.getInfix()) != null ? props.getInfix() : config.getApp())
                .provider(props.getProvider())
                .initialCapacity(props.getInitialCapacity())
                .leaseTime(props.getLeaseTime())
                .params(props.getParams())
                .build();
    }

    /**
     * @param config 缓存锁配置
     * @return <p>{@link LockService}</p>
     * <p>如果未配置，默认返回 {@link EmbedLockService}</p>
     * <p>如果配置错误，抛出异常 {@link CacheConfigException} </p>
     */
    private LockService getCacheLock(LockConfig config) {
        String beanId = config.getProvider();
        if (beanId == null) {
            return EmbedCacheLockProvider.getInstance().get(config);
        }

        if (Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(beanId))) {
            throw new CacheConfigException("Cache lock is required and cannot be set to 'none'.");
        }

        CacheLockProvider provider = componentManager.getLockProvider(beanId);
        requireNonNull(provider, () -> "CacheLockProvider:[" + beanId + "] is undefined.");

        LockService cacheLock = provider.get(config);
        requireNonNull(cacheLock, () -> "Unable to get lock from provider:[" + beanId + "].");

        return cacheLock;
    }

    private <K, V> PredicateConfig<K> buildPredicateConfig(String beanId, CacheConfig<K, V> config) {
        return PredicateConfig.builder(config.getKeyType(), config.getKeyParams())
                .name(config.getName())
                .provider(beanId)
                .build();
    }

    /**
     * @param config 断言配置
     * @param <K>    键泛型参数
     * @return ContainsPredicate 数据存在断言
     * <p>
     * 如果未配置，或者配置为 “none”，返回 null；如果配置错误，抛出异常 {@link CacheConfigException}
     */
    private <K> ContainsPredicate<K> getContainsPredicate(PredicateConfig<K> config) {
        String beanId = config.getProvider();
        if (beanId == null || Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(beanId))) {
            return null;
        }

        ContainsPredicateProvider predicateProvider = componentManager.getPredicateProvider(beanId);
        requireNonNull(predicateProvider, () -> "ContainsPredicateProvider:[" + beanId + "] is undefined.");

        ContainsPredicate<K> predicate = predicateProvider.get(config);
        requireNonNull(predicate, () -> "Unable to get predicate from ContainsPredicateProvider:[" + beanId + "].");

        return predicate;
    }

    private <K, V> RefreshConfig buildRefreshConfig(RefreshProps props, LockService lock, CacheConfig<K, V> config) {
        return RefreshConfig.builder()
                .name(config.getName())
                .app(config.getApp())
                .infix(props.getInfix())
                .charset(config.getCharset())
                .provider(props.getProvider())
                .period(props.getPeriod())
                .stopAfterAccess(props.getStopAfterAccess())
                .cacheLock(lock)
                .build();
    }

    private CacheRefresh getCacheRefresh(RefreshConfig config) {
        String beanId = config.getProvider();
        if (beanId == null || Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(beanId))) {
            return null;
        }

        CacheRefreshProvider provider = componentManager.getRefreshProvider(beanId);
        requireNonNull(provider, () -> "RefreshProvider:[" + beanId + "] is undefined.");

        return provider.getCacheRefresh(config);
    }

    private <K, V> StatConfig buildStatConfig(String provider, CacheConfig<K, V> config) {
        return StatConfig.builder()
                .name(config.getName())
                .app(config.getApp())
                .provider(provider)
                .build();
    }

    private CacheStatMonitor getStatMonitor(StatConfig config) {
        String beanId = config.getProvider();
        if (beanId == null || Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(beanId))) {
            return null;
        }

        CacheStatProvider provider = componentManager.getStatProvider(beanId);
        if (provider == null) {
            provider = componentManager.getStatProvider(beanId);
            requireNonNull(provider, () -> "CacheStatProvider:[" + beanId + "] is undefined.");
        }

        return provider.getMonitor(config);
    }

    private <K, V> SyncConfig<V> buildSyncConfig(SyncProps props, Store<V>[] stores, CacheConfig<K, V> config) {
        return SyncConfig.builder(stores[0], stores[1])
                .name(config.getName())
                .app(config.getApp())
                .sid(config.getSid())
                .charset(config.getCharset())
                .first(props.getFirst())
                .second(props.getSecond())
                .provider(props.getProvider())
                .maxLen(props.getMaxLen())
                .infix(props.getInfix())
                .params(props.getParams())
                .build();
    }

    private <V> CacheSyncMonitor getSyncMonitor(SyncConfig<V> config) {
        String beanId = config.getProvider();
        if (beanId == null || Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(beanId))) {
            return null;
        }

        CacheSyncProvider provider = componentManager.getSyncProvider(beanId);
        requireNonNull(provider, () -> "CacheSyncProvider:[" + beanId + "] is undefined");

        provider.register(config.getChannel(), new SyncMessageListener<>(config));

        CacheSyncMonitor monitor = provider.getMonitor(config);
        requireNonNull(monitor, () -> "Unable to get monitor from provider:[" + beanId + "].");

        return monitor;
    }

    @Override
    public Collection<Cache<?, ?>> getAll() {
        return Collections.unmodifiableCollection(cached.values());
    }

    @Override
    public Collection<String> getAllCacheNames() {
        return Collections.unmodifiableCollection(cached.keySet());
    }

    private static void requireNonNull(Object obj, Supplier<String> errMsg) {
        if (obj == null) {
            throw new CacheConfigException(errMsg.get());
        }
    }

}