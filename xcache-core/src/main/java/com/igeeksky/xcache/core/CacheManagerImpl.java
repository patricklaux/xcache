package com.igeeksky.xcache.core;

import com.igeeksky.xcache.common.*;
import com.igeeksky.xcache.core.store.StoreConfig;
import com.igeeksky.xcache.core.store.StoreProvider;
import com.igeeksky.xcache.extension.codec.CodecConfig;
import com.igeeksky.xcache.extension.codec.CodecProvider;
import com.igeeksky.xcache.extension.compress.CompressConfig;
import com.igeeksky.xcache.extension.compress.CompressorProvider;
import com.igeeksky.xcache.extension.contains.ContainsConfig;
import com.igeeksky.xcache.extension.contains.ContainsPredicate;
import com.igeeksky.xcache.extension.contains.ContainsPredicateProvider;
import com.igeeksky.xcache.extension.contains.EmbedContainsPredicate;
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

    private final ComponentRegister register;

    private final ConcurrentMap<String, CacheProps> caches = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Template> templates = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Cache<?, ?>> cached = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, CacheLoader<?, ?>> loaders = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheWriter<?, ?>> writers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, StoreProvider> storeProviders = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheSyncProvider> syncProviders = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheRefreshProvider> refreshProviders = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheStatProvider> statProviders = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CacheLockProvider> lockProviders = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CodecProvider> codecProviders = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CompressorProvider> compressorProviders = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ContainsPredicateProvider> predicateProviders = new ConcurrentHashMap<>();

    public CacheManagerImpl(CacheManagerConfig managerConfig) {
        this.app = managerConfig.getApp();
        this.register = new ComponentRegister(managerConfig.getScheduler(), managerConfig.getStatPeriod());

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
        CacheConfig<K, V> cacheConfig = CacheConfig.builder(keyType, keyParams, valueType, valueParams)
                .sid(sid)
                .name(name)
                .app(app)
                .charset(cacheProps.getCharset())
                .build();

        Store<V>[] stores = new Store[3];
        stores[0] = this.getStore(this.buildStoreConfig(cacheProps.getFirst(), cacheConfig));
        stores[1] = this.getStore(this.buildStoreConfig(cacheProps.getSecond(), cacheConfig));
        stores[2] = this.getStore(this.buildStoreConfig(cacheProps.getThird(), cacheConfig));

        CacheLoader<K, V> cacheLoader = this.getCacheLoader(name);
        CacheWriter<K, V> cacheWriter = this.getCacheWriter(name);

        int count = CacheBuilder.count(stores);
        if (count == 0) {
            return new NoOpCache<>(cacheConfig, cacheLoader, cacheWriter);
        }

        LockConfig lockConfig = this.buildLockConfig(cacheProps.getCacheLock(), cacheConfig);
        LockService cacheLock = this.getCacheLock(lockConfig);

        StatConfig statConfig = this.buildStatConfig(cacheProps.getCacheStat(), cacheConfig);
        CodecConfig<K> keyCodecConfig = this.buildKeyCodecConfig(cacheConfig);
        ContainsConfig<K> containsConfig = this.buildContainsConfig(cacheProps.getContainsPredicate(), cacheConfig);
        RefreshConfig refreshConfig = this.buildRefreshConfig(cacheProps.getCacheRefresh(), cacheLock, cacheConfig);
        SyncConfig<V> syncConfig = this.buildSyncConfig(cacheProps.getCacheSync(), stores[0], stores[1], cacheConfig);

        ExtendConfig<K, V> extendConfig = ExtendConfig.builder(cacheLoader)
                .cacheWriter(cacheWriter)
                .cacheLock(cacheLock)
                .keyCodec(this.getKeyCodec(cacheProps.getKeyCodec(), keyCodecConfig))
                .statMonitor(this.getStatMonitor(statConfig))
                .syncMonitor(this.getSyncMonitor(syncConfig))
                .containsPredicate(this.getContainsPredicate(containsConfig))
                .cacheRefresh(this.getCacheRefresh(refreshConfig))
                .build();

        return CacheBuilder.builder(cacheConfig)
                .extendConfig(extendConfig)
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
            String id = getTemplateId(userProps.getTemplateId());
            userProps.setTemplateId(id);

            // 获取 “最终模板配置”
            Template template = templates.get(id);
            requireNonNull(template, () -> "cache:[" + nameKey + "], template:[" + id + "] doesn't exist.");

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

    /**
     * @param config 缓存锁配置
     * @return <p>{@link LockService}</p>
     * <p>如果未配置，默认返回 {@link EmbedLockService}</p>
     * <p>如果有配置：配置 bean 正确，返回配置的 CacheLock；配置 bean 错误，抛出异常 {@link CacheConfigException} </p>
     */
    private LockService getCacheLock(LockConfig config) {
        String beanId = config.getProvider();
        if (beanId == null || Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(beanId))) {
            return EmbedCacheLockProvider.getInstance().get(config);
        }

        CacheLockProvider provider = lockProviders.get(beanId);
        requireNonNull(provider, () -> "CacheLockProvider:[" + beanId + "] is undefined.");

        LockService cacheLock = provider.get(config);
        requireNonNull(cacheLock, () -> "Unable to get lock from provider:[" + beanId + "].");

        return cacheLock;
    }

    /**
     * @param config 配置
     * @param <K>    键泛型参数
     * @return 如果未配置，或配置为 "NONE"，返回默认的 {@link EmbedContainsPredicate}。 <p>
     * 如果有配置：配置正确，返回配置的 {@link ContainsPredicate}；配置错误，抛出异常 {@link CacheConfigException}
     */
    private <K> ContainsPredicate<K> getContainsPredicate(ContainsConfig<K> config) {
        String beanId = StringUtils.trimToNull(config.getProvider());
        if (beanId == null || Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(beanId))) {
            return EmbedContainsPredicate.getInstance();
        }

        ContainsPredicateProvider provider = predicateProviders.get(beanId);
        requireNonNull(provider, () -> "ContainsPredicateProvider:[" + beanId + "] is undefined.");

        ContainsPredicate<K> predicate = provider.get(config);
        requireNonNull(predicate, () -> "Unable to get predicate from provider:[" + beanId + "].");

        return predicate;
    }

    private CacheStatMonitor getStatMonitor(StatConfig config) {
        String beanId = config.getProvider();
        if (beanId == null || Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(beanId))) {
            return null;
        }

        CacheStatProvider provider = statProviders.get(beanId);
        if (provider == null) {
            provider = register.logCacheStat(beanId, this);
            requireNonNull(provider, () -> "CacheStatProvider:[" + beanId + "] is undefined.");
        }

        return provider.getMonitor(config);
    }

    private <K, V> CacheLoader<K, V> getCacheLoader(String name) {
        return (CacheLoader<K, V>) loaders.get(name);
    }

    private <K, V> CacheWriter<K, V> getCacheWriter(String name) {
        return (CacheWriter<K, V>) writers.get(name);
    }

    private CacheRefresh getCacheRefresh(RefreshConfig config) {
        String beanId = config.getProvider();
        if (beanId == null || Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(beanId))) {
            return null;
        }

        CacheRefreshProvider provider = refreshProviders.get(beanId);
        if (provider == null) {
            provider = register.embedCacheRefresh(beanId, this);
            requireNonNull(provider, () -> "RefreshProvider:[" + beanId + "] is undefined.");
        }

        return provider.getCacheRefresh(config);
    }

    private <V> CacheSyncMonitor getSyncMonitor(SyncConfig<V> config) {
        String beanId = config.getProvider();
        if (beanId == null || Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(beanId))) {
            return new CacheSyncMonitor(config, null);
        }

        CacheSyncProvider provider = syncProviders.get(beanId);
        requireNonNull(provider, () -> "CacheSyncProvider:[" + beanId + "] is undefined");

        provider.register(config.getChannel(), new SyncMessageListener<>(config));

        CacheSyncMonitor monitor = provider.getMonitor(config);
        requireNonNull(monitor, () -> "Unable to get monitor from provider:[" + beanId + "].");

        return monitor;
    }

    private <V> Store<V> getStore(StoreConfig<V> config) {
        String name = config.getName();
        String beanId = config.getProvider();

        if (beanId == null) {
            return null;
        }

        StoreProvider provider = storeProviders.get(beanId);
        requireNonNull(provider, () -> "Cache:[" + name + "], CacheStoreProvider:[" + beanId + "] is undefined.");

        Store<V> store = provider.getStore(config);
        requireNonNull(store, () -> "Cache:[" + name + "], Unable to get store from provider:[" + beanId + "].");

        return store;
    }

    private <K> KeyCodec<K> getKeyCodec(String beanId, CodecConfig<K> config) {
        String name = config.getName();
        CodecProvider provider = codecProviders.get(beanId);
        requireNonNull(provider, () -> "Cache:[" + name + "], CodecProvider:[" + beanId + "] is undefined.");

        KeyCodec<K> keyCodec = provider.getKeyCodec(config);
        requireNonNull(keyCodec, () -> "Unable to get KeyCodec from provider:[" + beanId + "].");

        return keyCodec;
    }

    private <V> Codec<V> getCodec(String beanId, CodecConfig<V> config) {
        String name = config.getName();

        CodecProvider provider = codecProviders.get(beanId);
        requireNonNull(provider, () -> "Cache:[" + name + "], CodecProvider:[" + beanId + "] is undefined.");

        Codec<V> codec = provider.getCodec(config);
        requireNonNull(codec, () -> "Cache:[" + name + "], unable to get codec from provider:[" + beanId + "].");

        return codec;
    }

    private <K, V> CodecConfig<K> buildKeyCodecConfig(CacheConfig<K, V> config) {
        return CodecConfig.builder(config.getKeyType(), config.getKeyParams())
                .name(config.getName())
                .charset(config.getCharset())
                .build();
    }

    private <K, V> StatConfig buildStatConfig(String provider, CacheConfig<K, V> config) {
        return StatConfig.builder()
                .name(config.getName())
                .app(config.getApp())
                .provider(provider)
                .build();
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

    private <K, V> ContainsConfig<K> buildContainsConfig(String provider, CacheConfig<K, V> config) {
        return ContainsConfig.builder(config.getKeyType(), config.getKeyParams())
                .name(config.getName())
                .provider(provider)
                .build();
    }

    private <K, V> SyncConfig<V> buildSyncConfig(SyncProps props, Store<V> first, Store<V> second, CacheConfig<K, V> config) {
        return SyncConfig.builder(first, second)
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


    private <K, V> StoreConfig<V> buildStoreConfig(StoreProps storeProps, CacheConfig<K, V> config) {
        String provider = storeProps.getProvider();
        if (provider == null || Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(provider))) {
            return StoreConfig.builder(config.getValueType(), config.getValueParams()).build();
        }

        return StoreConfig.builder(config.getValueType(), config.getValueParams())
                .name(config.getName())
                .app(config.getApp())
                .charset(config.getCharset())
                .provider(provider)
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
                .valueCodec(this.getValueCodec(storeProps.getValueCodec(), config))
                .valueCompressor(this.getCompressor(storeProps.getValueCompressor()))
                .params(storeProps.getParams())
                .build();
    }

    private <K, V> Codec<V> getValueCodec(String provider, CacheConfig<K, V> config) {
        if (provider == null || Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(provider))) {
            return null;
        }

        CodecConfig<V> codecConfig = CodecConfig.builder(config.getValueType(), config.getValueParams())
                .name(config.getName())
                .charset(config.getCharset())
                .build();

        return this.getCodec(provider, codecConfig);
    }

    private Compressor getCompressor(CompressProps props) {
        String provider = props.getProvider();
        if (provider == null || Objects.equals(CacheConstants.NONE, StringUtils.toLowerCase(provider))) {
            return null;
        }

        CompressConfig compressConfig = CompressConfig.builder()
                .provider(provider)
                .level(props.getLevel())
                .nowrap(props.getNowrap())
                .build();

        return this.getCompressor(compressConfig);
    }

    private Compressor getCompressor(CompressConfig config) {
        String beanId = config.getProvider();

        CompressorProvider provider = compressorProviders.get(beanId);
        requireNonNull(provider, () -> "CompressorProvider:[" + beanId + "] is undefined.");

        Compressor compressor = provider.get(config);
        requireNonNull(compressor, () -> "Unable to get compressor from provider:[" + beanId + "].");

        return compressor;
    }

    @Override
    public Collection<Cache<?, ?>> getAll() {
        return Collections.unmodifiableCollection(cached.values());
    }

    @Override
    public Collection<String> getAllCacheNames() {
        return Collections.unmodifiableCollection(cached.keySet());
    }

    @Override
    public void addProvider(String beanId, CodecProvider provider) {
        this.codecProviders.put(beanId, provider);
    }

    @Override
    public void addProvider(String beanId, CompressorProvider provider) {
        this.compressorProviders.put(beanId, provider);
    }

    @Override
    public void addProvider(String beanId, CacheSyncProvider provider) {
        this.syncProviders.put(beanId, provider);
    }

    @Override
    public void addProvider(String beanId, CacheStatProvider provider) {
        this.statProviders.put(beanId, provider);
    }

    @Override
    public void addProvider(String beanId, CacheLockProvider provider) {
        this.lockProviders.put(beanId, provider);
    }

    @Override
    public void addProvider(String beanId, ContainsPredicateProvider provider) {
        this.predicateProviders.put(beanId, provider);
    }

    @Override
    public void addProvider(String beanId, StoreProvider provider) {
        this.storeProviders.put(beanId, provider);
    }

    @Override
    public void addCacheLoader(String beanId, CacheLoader<?, ?> loader) {
        this.loaders.put(beanId, loader);
    }

    @Override
    public void addCacheWriter(String beanId, CacheWriter<?, ?> writer) {
        this.writers.put(beanId, writer);
    }

    @Override
    public void addProvider(String beanId, CacheRefreshProvider provider) {
        this.refreshProviders.put(beanId, provider);
    }

    private static void requireNonNull(Object obj, Supplier<String> errMsg) {
        if (obj == null) {
            throw new CacheConfigException(errMsg.get());
        }
    }

}