package com.igeeksky.xcache.props;

/**
 * 配置信息抽象类
 *
 * @author patrick
 * @since 0.0.4 2024/6/3
 */
public abstract class AbstractProps {

    private String charset;

    private String keyCodec;

    private String cacheStat;

    private SyncProps cacheSync = new SyncProps();

    private LockProps cacheLock = new LockProps();

    private RefreshProps cacheRefresh = new RefreshProps();

    private StoreProps first = new StoreProps();

    private StoreProps second = new StoreProps();

    private StoreProps third = new StoreProps();

    /**
     * 字符集
     * <p>
     * 默认：UTF-8
     * <p>
     * {@link CacheConstants#DEFAULT_CHARSET_NAME}
     *
     * @return {@link String} – 字符集
     */
    public String getCharset() {
        return charset;
    }

    /**
     * 设置字符集
     *
     * @param charset 字符集
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * CodecProviderId
     * <p>
     * 用于获取 KeyCodec，处理 键 与 String 相互转换。
     * <p>
     * 默认值：jackson
     * <p>
     * {@link CacheConstants#DEFAULT_KEY_CODEC_PROVIDER}
     *
     * @return {@link String} – CodecProviderId
     */
    public String getKeyCodec() {
        return keyCodec;
    }

    /**
     * 设置 CodecProviderId
     *
     * @param keyCodec CodecProviderId
     */
    public void setKeyCodec(String keyCodec) {
        this.keyCodec = keyCodec;
    }

    /**
     * 缓存锁配置
     * <p>
     * 用于数据回源加锁。
     * <p>
     * 默认值：{@link PropsUtil#defaultLockProps()}
     * <p>
     * 调用 {@code Cache.get(K key, CacheLoader loader)} 方法时，
     * 多个线程同时使用相同的 key 回源查询数据时，通过加锁保证仅有一个线程回源获取数据并存入缓存，
     * 其它线程则等待该线程完成后再直接读取缓存数据，从而减小数据源的查询压力.
     *
     * @return {@link LockProps} – 数据同步配置
     */
    public LockProps getCacheLock() {
        return cacheLock;
    }

    /**
     * @param cacheLock 缓存锁配置
     */
    public void setCacheLock(LockProps cacheLock) {
        this.cacheLock = cacheLock;
    }

    /**
     * CacheStatProviderId
     * <p>
     * 用于缓存指标统计信息采集和输出。
     * <p>
     * 默认值：log
     * <p>
     * {@link CacheConstants#DEFAULT_STAT_PROVIDER}
     * <p>
     * 如果配置为 log，统计信息将输出到日志，日志级别为 info。 <p>
     * 日志输出类为 { com.igeeksky.xcache.extension.stat.LogCacheStatProvider} ，可将此类的日志配置为单独输出到一个文件.
     *
     * @return {@link String} – CacheStatProviderId
     */
    public String getCacheStat() {
        return cacheStat;
    }

    /**
     * @param cacheStat CacheStatProvider 的 beanId
     */
    public void setCacheStat(String cacheStat) {
        this.cacheStat = cacheStat;
    }

    /**
     * 缓存刷新配置
     * <p>
     * 用于缓存数据刷新。
     * <p>
     * 默认值：{@link PropsUtil#defaultRefreshProps()}
     * <p>
     * 通过缓存查询过的 key，定期通过 {@code CacheLoader} 回源取值并更新缓存数据.
     *
     * @return {@code RefreshProps} – 缓存刷新配置
     */
    public RefreshProps getCacheRefresh() {
        return cacheRefresh;
    }

    /**
     * 设置缓存刷新
     *
     * @param cacheRefresh 缓存刷新配置
     */
    public void setCacheRefresh(RefreshProps cacheRefresh) {
        this.cacheRefresh = cacheRefresh;
    }

    /**
     * 缓存数据同步配置
     * <p>
     * 用于多个缓存实例之间的数据同步。
     * <p>
     * 默认值：{@link PropsUtil#defaultSyncProps()}
     * <p>
     * 注意：只有使用本地缓存才需要数据同步。
     * 譬如内嵌缓存（如 caffeine），或缓存实例之间相互隔离的外部缓存（如 localhost:6379 的 redis）。
     * <p>
     *
     * <b>示例一</b><p>
     * 假设有三级缓存：<p>
     * first 为内嵌的 caffeine；<p>
     * second 为 应用实例所在主机部署的 redis，且仅该应用实例可访问； <p>
     * third 为 当前应用的所有实例均可访问的 redis 集群。 <p>
     * 此时建议配置为：{first: all, second: all, …… } <p>
     * 当应用的某一实例执行缓存数据的更新、删除、清空等操作后（put、putAll、evict、evictAll, clear），
     * 该应用实例的缓存会发送 remove 或 clear 事件，
     * 其它应用实例的缓存收到通知后会执行 evict 或 clear 操作，将 first 和 second 的相应缓存数据删除或清空。<p>
     * <p>
     *
     * <b>示例二</b><p>
     * 假设有两级缓存：<p>
     * first 为内嵌的 caffeine；<p>
     * second 为 当前应用的所有实例均可访问的 redis 集群。 <p>
     * 此时建议配置为：{first: all, second: none, …… } <p>
     * 当应用的某一实例执行缓存数据的更新、删除、清空等操作后（put、putAll、evict、evictAll, clear），
     * 该应用实例的缓存会发送 remove 或 clear 事件，
     * 其它应用实例的缓存收到通知后会执行 evict 或 clear 操作，将 first 的相应缓存数据删除或清空。<p>
     *
     * <b>示例三</b><p>
     * 假设仅有一级缓存：<p>
     * first 为内嵌的 caffeine。<p>
     * 此时建议配置为：{first: clear, second: none, …… } <p>
     * 当应用的某一实例执行缓存数据的清空操作后（clear），该应用实例的缓存会发出 clear 事件通知，
     * 其它应用实例的缓存收到通知后会执行 clear 操作，将 first 的缓存数据清空。<p>
     * 当然，如果应用无需执行缓存清空操作，配置成 {first: none, second: none, …… } 亦可。<p>
     * <p>
     * <b>提示：</b><p>
     * 1. 如果仅使用本地缓存，不能配置成 {first: all, second: none, …… }，否则会导致每次读取缓存数据均需回源查询。<p>
     * 2. 如果仅使用远程缓存，配置成 {first: none, second: none, …… } 即可，因为任意一个缓存实例的操作完成后都会被其它实例实时感知，因此根本无需数据同步。
     *
     * @return {@link SyncProps} – 数据同步配置
     */
    public SyncProps getCacheSync() {
        return cacheSync;
    }

    /**
     * @param cacheSync 数据同步配置
     */
    public void setCacheSync(SyncProps cacheSync) {
        this.cacheSync = cacheSync;
    }

    /**
     * 一级缓存配置
     * <p>
     * 默认采用 Caffeine 作为一级缓存。
     * <p>
     * {@link PropsUtil#defaultEmbedStoreProps()}
     *
     * @return {@link StoreProps} – 一级缓存配置
     */
    public StoreProps getFirst() {
        return first;
    }

    /**
     * @param first 一级缓存配置
     */
    public void setFirst(StoreProps first) {
        this.first = first;
    }

    /**
     * 二级缓存配置
     * <p>
     * 默认无缓存。
     * <p>
     * {@link PropsUtil#defaultExtraStoreProps}
     *
     * @return {@link StoreProps} – 二级缓存配置
     */
    public StoreProps getSecond() {
        return second;
    }

    /**
     * @param second 二级缓存配置
     */
    public void setSecond(StoreProps second) {
        this.second = second;
    }

    /**
     * 三级缓存配置
     * <p>
     * 默认无缓存。
     * <p>
     * {@link PropsUtil#defaultExtraStoreProps}
     *
     * @return {@link StoreProps} – 三级缓存配置
     */
    public StoreProps getThird() {
        return third;
    }

    /**
     * @param third 三级缓存配置
     */
    public void setThird(StoreProps third) {
        this.third = third;
    }

}