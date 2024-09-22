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

    private SyncProps cacheSync = new SyncProps();

    private LockProps cacheLock = new LockProps();

    private String cacheStat;

    private RefreshProps cacheRefresh = new RefreshProps();

    private String containsPredicate;

    private StoreProps first = new StoreProps();

    /**
     * 二级缓存配置
     */
    private StoreProps second = new StoreProps();

    /**
     * 三级缓存配置
     */
    private StoreProps third = new StoreProps();

    /**
     * 字符集（可不填）<p>
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
     * KeyCodecProvider 的 beanId（可不填）
     * <p>
     * <b>默认</b>：jackson
     * <p>
     * {@link CacheConstants#DEFAULT_KEY_CODEC_PROVIDER}
     * <p>
     * 用于存取缓存数据时，将键转换成 String 对象
     *
     * @return {@link String} – KeyCodecProvider 的 beanId
     */
    public String getKeyCodec() {
        return keyCodec;
    }

    /**
     * 设置 KeyCodecProvider 的 beanId
     *
     * @param keyCodec KeyCodecProvider 的 beanId
     */
    public void setKeyCodec(String keyCodec) {
        this.keyCodec = keyCodec;
    }

    /**
     * 缓存锁配置（可不填）
     * <p>
     * 用于数据回源加锁。<p>
     * 调用 get(K key, CacheLoader loader) 方法时，
     * 如果多个线程同时使用相同的 key 回源查询数据，通过加锁保证只有一个线程回源，
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
     * 默认值：log <br>
     * 统计信息将输出到日志，日志级别为 info。
     * <p>
     * {@link CacheConstants#DEFAULT_STAT_PROVIDER}
     * <p>
     * 日志输出类为 { com.igeeksky.xcache.extension.stat.LogCacheStatProvider} ，可将此类的日志配置为单独输出到一个文件。
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
     * 缓存刷新配置（可不填）
     * <p>
     * 如果配置了 CacheRefresh，所有通过 get、getAll 方法查询过的 Key，会定期回源取值并刷新缓存数据.
     * <p>
     * 注意：<br>
     * CacheRefresh 依赖于 CacheLoader，如配置了 CacheRefresh，但无 CacheLoader，则会抛出异常。
     *
     * @return {@code RefreshProps} – 缓存刷新配置
     */
    public RefreshProps getCacheRefresh() {
        return cacheRefresh;
    }

    /**
     * @param cacheRefresh 缓存刷新配置
     */
    public void setCacheRefresh(RefreshProps cacheRefresh) {
        this.cacheRefresh = cacheRefresh;
    }

    /**
     * ContainsPredicateProvider 的 beanId
     * <p>
     * 默认值：none
     * <p>
     * {@link CacheConstants#DEFAULT_PREDICATE_PROVIDER} <p>
     * 调用 {@code cache.get(K key, CacheLoader loader) }方法时，用于判断数据源是否存在相应数据，从而避免无效的回源查询.
     *
     * @return String – ContainsPredicateProvider 的 beanId
     */
    public String getContainsPredicate() {
        return containsPredicate;
    }

    /**
     * @param containsPredicate ContainsPredicateProvider 的 beanId
     */
    public void setContainsPredicate(String containsPredicate) {
        this.containsPredicate = containsPredicate;
    }

    /**
     * <b>数据同步配置（可为空）</b><p>
     * 只有满足以下条件才需配置此选项： <p>
     * 1. 使用内嵌缓存（如 caffeine），或每个应用实例都有一个私有访问互相隔离的外部缓存（如 localhost:6379 的 redis）<p>
     * 2. 存在 {@code com.igeeksky.xcache.extension.sync.CacheSyncProvider} 接口的实现类对象，如
     * {@code com.igeeksky.xcache.redis.sync.RedisCacheSyncProvider} <p>
     * 3. 可选条件：最好有两级缓存，如果只有一级缓存，只能处理缓存清空操作（ clear） <p>
     *
     * <b>示例说明 1</b><p>
     * 假设有三级缓存：<p>
     * first 为内嵌的 caffeine；<p>
     * second 为 应用实例所在主机部署的 redis，且仅该应用实例可访问； <p>
     * third 为当前应用的所有实例均可访问的 redis 集群。 <p>
     * 此时应配置为：{first: all, second: all, …… } <p>
     * 当应用的某一实例执行缓存数据的更新、删除、清空等操作后（put、putAll、evict、evictAll, clear），
     * 该应用实例的缓存会发出 remove 或 clear 事件通知，
     * 其它应用实例的缓存收到通知后会执行 evict 或 clear 操作，将 first 和 second 的相应缓存数据删除或清空。<p>
     *
     * <b>示例说明 2</b><p>
     * 假设仅有一级缓存：first 为内嵌的 caffeine。<p>
     * 此时应配置为：{first: clear, …… } <p>
     * 当应用的某一实例执行缓存数据的清空操作后（clear），该应用实例的缓存会发出 clear 事件通知，
     * 其它应用实例的缓存收到通知后会执行 clear 操作，将 first 的相应缓存数据清空。<p>
     * 如果只有一级缓存，通常应用不会执行缓存清空操作，所以配置成 {first: none, …… } 亦可。<p>
     * 特别注意：仅有一级缓存不能配置成 all，否则会导致每次读取缓存数据均需回源查询。
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
     *
     * @return {@link StoreProps} – 一级缓存配置
     */
    public StoreProps getFirst() {
        return first;
    }

    public void setFirst(StoreProps first) {
        this.first = first;
    }

    /**
     * 二级缓存配置
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