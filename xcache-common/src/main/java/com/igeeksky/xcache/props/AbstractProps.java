package com.igeeksky.xcache.props;

/**
 * @author patrick
 * @since 0.0.4 2024/6/3
 */
public abstract class AbstractProps {

    /**
     * <b>字符集</b><p>
     * 默认：UTF-8
     * <p>
     * {@link CacheConstants#DEFAULT_CHARSET_NAME} <p>
     * 主要用于键、值的序列化操作
     */
    private String charset;

    /**
     * <b>KeyConvertorProvider 的 beanId </b><p>
     * <b>默认</b>：jacksonKeyConvertorProvider
     * <p>
     * {@link CacheConstants#DEFAULT_KEY_CODEC_PROVIDER} <p>
     * 主要用于存取缓存数据时，先将键转换成 String 对象
     */
    private String keyCodec;

    /**
     * <b>数据同步配置</b><p>
     * 只有满足以下两个条件才需配置此选项： <p>
     * 1. 使用内嵌缓存（如 caffeine，guava），
     * 或应用实例私有访问的外部缓存（如与应用的某个实例部署在同一主机，且不能被其它实例访问的 redis）<p>
     * 2. 创建了 { com.igeeksky.xcache.extension.sync.CacheSyncProvider} Bean，
     * 如果引入了 xcache-lettuce-spring-boot-autoconfigure 项目，则可以通过配置创建
     * { com.igeeksky.xcache.redis.sync.RedisCacheSyncProvider} <p>
     * 3. 可选条件：最好有两级缓存，如果只有一级缓存，只能处理缓存清空操作（ clear） <p>
     *
     * <b>示例说明 1</b><p>
     * 假设有三级缓存：<p>
     * first 为内嵌的 caffeine；<p>
     * second 为 应用实例所在机器部署的本机 redis，仅该应用实例可访问； <p>
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
     * 其它应用实例的缓存收到通知后会执行 clear 操作，将 first 的相应缓存数据清空。
     * <p>
     * 如果只有一级缓存，说明对于缓存数据更新的及时性并没有要求，通常都是等待数据自动过期即可，
     * 而且大多数应用根本不会对缓存执行 clear 操作，因此配置成 {first: none, …… } 即可。
     *
     * @see SyncType
     */
    private SyncProps cacheSync = new SyncProps();

    /**
     * 数据回源的锁配置
     * <p>
     * 调用 get(key, cacheLoader)方法时，
     * 如果多个线程同时回源查询相同 key 的数据，通过加锁可以保证只有一个线程回源，
     * 其它线程则采用该线程存入缓存的数据，从而减小数据源的查询压力。
     */
    private LockProps cacheLock = new LockProps();

    /**
     * CacheStatProvider 的 beanId
     * <p>
     * 默认值：log
     * <p>
     * {@link CacheConstants#DEFAULT_STAT_PROVIDER} <p>
     * 如果采用默认配置，默认将统计信息输出到日志，日志级别为 info。
     * 用户可以将 { com.igeeksky.xcache.extension.stat.LogCacheStatProvider} 的日志信息单独输出到一个文件。
     */
    private String cacheStat;

    /**
     * 缓存刷新配置
     * <p>
     * 如果配置了 CacheLoader 和 CacheRefresh，
     * 所有通过 get、getAll 方法查询过的 Key，会根据配置定期从数据源取值并刷新缓存中的数据
     */
    private RefreshProps cacheRefresh = new RefreshProps();

    /**
     * ContainsPredicateProvider 的 beanId
     * <p>
     * 默认值：NONE
     * <p>
     * {@link CacheConstants#DEFAULT_PREDICATE_PROVIDER} <p>
     * 调用 get(key, cacheLoader)方法时，用于判断是否存在 key 对应的数据，减少无效的数据回源查询。
     */
    private String containsPredicate;

    /**
     * 第 1 级缓存配置
     */
    private StoreProps first = new StoreProps();

    /**
     * 第 2 级缓存配置
     */
    private StoreProps second = new StoreProps();

    /**
     * 第 3 级缓存配置
     */
    private StoreProps third = new StoreProps();

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getKeyCodec() {
        return keyCodec;
    }

    public void setKeyCodec(String keyCodec) {
        this.keyCodec = keyCodec;
    }

    public LockProps getCacheLock() {
        return cacheLock;
    }

    public void setCacheLock(LockProps cacheLock) {
        this.cacheLock = cacheLock;
    }

    public String getCacheStat() {
        return cacheStat;
    }

    public void setCacheStat(String cacheStat) {
        this.cacheStat = cacheStat;
    }

    public RefreshProps getCacheRefresh() {
        return cacheRefresh;
    }

    public void setCacheRefresh(RefreshProps cacheRefresh) {
        this.cacheRefresh = cacheRefresh;
    }

    public String getContainsPredicate() {
        return containsPredicate;
    }

    public void setContainsPredicate(String containsPredicate) {
        this.containsPredicate = containsPredicate;
    }

    public SyncProps getCacheSync() {
        return cacheSync;
    }

    public void setCacheSync(SyncProps cacheSync) {
        this.cacheSync = cacheSync;
    }

    public StoreProps getFirst() {
        return first;
    }

    public void setFirst(StoreProps first) {
        this.first = first;
    }

    public StoreProps getSecond() {
        return second;
    }

    public void setSecond(StoreProps second) {
        this.second = second;
    }

    public StoreProps getThird() {
        return third;
    }

    public void setThird(StoreProps third) {
        this.third = third;
    }

}