package com.igeeksky.xcache.core.config;


import com.igeeksky.xcache.common.CacheType;
import com.igeeksky.xcache.common.ReferenceType;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-20
 */
public interface CacheConstants {

    /**
     * 停用某个 String 类型的配置项，可以配置为 none
     */
    String NONE = "NONE";

    /**
     * <p>缓存数据同步广播的通道名的中缀</p>
     * 完整通道名：配置项（cache-sync-channel） + ":sync:" + 配置项（cache-name）
     */
    String DEFAULT_SYNC_CHANNEL_INFIX = ":sync:";

    /**
     * <p>缓存数据统计信息的队列名的中缀</p>
     * 完整通道名：配置项（cache-stat-channel） + ":stat:" + 配置项（cache-name）
     */
    String DEFAULT_STAT_CHANNEL_INFIX = ":stat:";


    String DEFAULT_TEMPLATE_ID = "t0";
    String DEFAULT_CHARSET_NAME = "UTF-8";
    CacheType DEFAULT_CACHE_TYPE = CacheType.BOTH;


    // 本地缓存默认配置 start
    String DEFAULT_LOCAL_CACHE_STORE = "caffeineCacheStoreProvider";
    String DEFAULT_LOCAL_STORE_NAME = "caffeine";
    int DEFAULT_LOCAL_INITIAL_CAPACITY = 1024;
    long DEFAULT_LOCAL_MAXIMUM_SIZE = 2048L;
    long DEFAULT_LOCAL_MAXIMUM_WEIGHT = 0L;
    long DEFAULT_LOCAL_EXPIRE_AFTER_WRITE = 3600000L;
    long DEFAULT_LOCAL_EXPIRE_AFTER_ACCESS = 360000L;
    ReferenceType DEFAULT_LOCAL_KEY_STRENGTH = ReferenceType.STRONG;
    ReferenceType DEFAULT_LOCAL_VALUE_STRENGTH = ReferenceType.STRONG;
    String DEFAULT_LOCAL_VALUE_SERIALIZER = NONE;
    String DEFAULT_LOCAL_VALUE_COMPRESSOR = NONE;
    boolean DEFAULT_LOCAL_ENABLE_RANDOM_TTL = true;
    boolean DEFAULT_LOCAL_ENABLE_NULL_VALUE = true;
    // 本地缓存默认配置 end


    // 远程缓存默认配置 start
    String DEFAULT_REMOTE_CACHE_STORE = "lettuceCacheStoreProvider";
    String DEFAULT_REMOTE_STORE_NAME = "redis-string";
    long DEFAULT_REMOTE_EXPIRE_AFTER_WRITE = 7200000L;
    String DEFAULT_REMOTE_VALUE_SERIALIZER = "jacksonSerializerProvider";
    String DEFAULT_REMOTE_VALUE_COMPRESSOR = NONE;
    boolean DEFAULT_REMOTE_ENABLE_KEY_PREFIX = true;
    boolean DEFAULT_REMOTE_ENABLE_RANDOM_TTL = true;
    boolean DEFAULT_REMOTE_ENABLE_NULL_VALUE = true;
    // 远程缓存默认配置 end


    // 扩展属性默认配置 start
    String DEFAULT_EXTENSION_KEY_CONVERTOR = "jacksonKeyConvertorProvider";
    String DEFAULT_EXTENSION_CACHE_LOCK = "localCacheLockProvider";
    int DEFAULT_EXTENSION_CACHE_LOCK_SIZE = 128;
    String DEFAULT_EXTENSION_CONTAINS_PREDICATE = "alwaysTruePredicateProvider";
    String DEFAULT_EXTENSION_CACHE_SYNC = "lettuceCacheSyncManager";
    String DEFAULT_EXTENSION_CACHE_SYNC_CHANNEL = NONE;
    String DEFAULT_EXTENSION_CACHE_SYNC_SERIALIZER = "jacksonSerializerProvider";
    String DEFAULT_EXTENSION_CACHE_STAT = "logCacheStatManager";
    String DEFAULT_EXTENSION_CACHE_LOADER = NONE;
    // 扩展属性默认配置 end

}
