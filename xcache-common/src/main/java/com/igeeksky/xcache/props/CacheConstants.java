package com.igeeksky.xcache.props;


import com.igeeksky.xcache.common.ReferenceType;

/**
 * 缓存配置项常量
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-20
 */
public interface CacheConstants {

    /**
     * 停用某个 String 类型的配置项，可以配置为 none
     */
    String NONE = "NONE";


    // 默认组件注册 ID
    String CAFFEINE_STORE = "caffeine";
    String JDK_CODEC = "jdk";
    String JACKSON_CODEC = "jackson";
    String JACKSON_SPRING_CODEC = "jackson-spring";
    String GZIP_COMPRESSOR = "gzip";
    String DEFLATE_COMPRESSOR = "deflate";
    String LOG_CACHE_STAT = "log";
    String EMBED_CACHE_LOCK = "embed";
    String EMBED_CACHE_REFRESH = "embed";

    // 默认配置 start
    String DEFAULT_TEMPLATE_ID = "t0";
    String DEFAULT_CHARSET_NAME = "UTF-8";


    // 内嵌缓存默认配置 start
    String DEFAULT_EMBED_STORE_PROVIDER = CAFFEINE_STORE;
    int DEFAULT_EMBED_INITIAL_CAPACITY = 65536;
    long DEFAULT_EMBED_MAXIMUM_SIZE = 65536;
    long DEFAULT_EMBED_MAXIMUM_WEIGHT = 0;
    long DEFAULT_EMBED_EXPIRE_AFTER_WRITE = 3600000;
    long DEFAULT_EMBED_EXPIRE_AFTER_ACCESS = 300000;
    ReferenceType DEFAULT_EMBED_KEY_STRENGTH = ReferenceType.STRONG;
    ReferenceType DEFAULT_EMBED_VALUE_STRENGTH = ReferenceType.STRONG;
    String DEFAULT_EMBED_VALUE_CODEC = NONE;
    boolean DEFAULT_EMBED_ENABLE_GROUP_PREFIX = false;
    boolean DEFAULT_EMBED_ENABLE_RANDOM_TTL = true;
    boolean DEFAULT_EMBED_ENABLE_NULL_VALUE = true;
    // 内嵌缓存默认配置 end


    // 外部缓存默认配置 start
    String DEFAULT_EXTRA_STORE_PROVIDER = NONE;
    RedisType DEFAULT_EXTRA_REDIS_TYPE = RedisType.STRING;
    long DEFAULT_EXTRA_EXPIRE_AFTER_WRITE = 7200000L;
    String DEFAULT_EXTRA_VALUE_CODEC = JACKSON_CODEC;
    boolean DEFAULT_EXTRA_ENABLE_GROUP_PREFIX = true;
    boolean DEFAULT_EXTRA_ENABLE_RANDOM_TTL = true;
    boolean DEFAULT_EXTRA_ENABLE_NULL_VALUE = true;
    // 外部缓存默认配置 end


    // 扩展属性默认配置 start
    boolean DEFAULT_ENABLE_GROUP_PREFIX = true;
    String DEFAULT_KEY_CODEC_PROVIDER = JACKSON_CODEC;

    String DEFAULT_VALUE_COMPRESSOR = NONE;
    int DEFAULT_VALUE_COMPRESSOR_LEVEL = -1;
    boolean DEFAULT_VALUE_COMPRESSOR_WRAP = false;

    String DEFAULT_LOCK_PROVIDER = EMBED_CACHE_LOCK;
    long DEFAULT_LOCK_LEASE_TIME = 1000;
    int DEFAULT_LOCK_INITIAL_CAPACITY = 256;

    String DEFAULT_REFRESH_PROVIDER = NONE;
    long DEFAULT_REFRESH_PERIOD = 2400000;
    long DEFAULT_REFRESH_STOP_AFTER_ACCESS = 7200000;

    String DEFAULT_SYNC_PROVIDER = NONE;
    long DEFAULT_SYNC_MAX_LEN = 10000;

    String DEFAULT_STAT_PROVIDER = LOG_CACHE_STAT;
    long DEFAULT_STAT_PERIOD = 60000;

    // 扩展属性默认配置 end

}