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
    String NONE = "none";


    // 默认组件注册 ID
    String LETTUCE = "lettuce";
    String CAFFEINE_STORE = "caffeine";
    String JDK_CODEC = "jdk";
    String JACKSON_CODEC = "jackson";
    String JACKSON_SPRING_CODEC = "jackson-spring";
    String GZIP_COMPRESSOR = "gzip";
    String DEFLATER_COMPRESSOR = "deflate";
    String LOG_CACHE_STAT = "log";
    String EMBED_CACHE_LOCK = "embed";
    String EMBED_CACHE_REFRESH = "embed";

    // 默认配置 start
    String DEFAULT_TEMPLATE_ID = "t0";
    String DEFAULT_CHARSET_NAME = "UTF-8";


    // 内嵌缓存默认配置 start
    String DEFAULT_EMBED_STORE_PROVIDER = CAFFEINE_STORE;
    int DEFAULT_EMBED_INITIAL_CAPACITY = 8192;
    long DEFAULT_EMBED_MAXIMUM_SIZE = 8192L;
    long DEFAULT_EMBED_MAXIMUM_WEIGHT = 0L;
    long DEFAULT_EMBED_EXPIRE_AFTER_WRITE = 3600000L;
    long DEFAULT_EMBED_EXPIRE_AFTER_ACCESS = 300000L;
    ReferenceType DEFAULT_EMBED_KEY_STRENGTH = ReferenceType.STRONG;
    ReferenceType DEFAULT_EMBED_VALUE_STRENGTH = ReferenceType.STRONG;
    String DEFAULT_EMBED_VALUE_CODEC = NONE;
    String DEFAULT_EMBED_VALUE_COMPRESSOR = NONE;
    boolean DEFAULT_EMBED_ENABLE_KEY_PREFIX = false;
    boolean DEFAULT_EMBED_ENABLE_RANDOM_TTL = true;
    boolean DEFAULT_EMBED_ENABLE_NULL_VALUE = true;
    // 内嵌缓存默认配置 end


    // 外部缓存默认配置 start
    String DEFAULT_EXTRA_STORE_PROVIDER = LETTUCE;
    RedisType DEFAULT_EXTRA_REDIS_TYPE = RedisType.STRING;
    long DEFAULT_EXTRA_EXPIRE_AFTER_WRITE = 7200000L;
    String DEFAULT_EXTRA_VALUE_CODEC = JACKSON_CODEC;
    boolean DEFAULT_EXTRA_ENABLE_KEY_PREFIX = true;
    boolean DEFAULT_EXTRA_ENABLE_RANDOM_TTL = true;
    boolean DEFAULT_EXTRA_ENABLE_NULL_VALUE = true;
    // 外部缓存默认配置 end


    // 扩展属性默认配置 start
    String DEFAULT_KEY_CODEC_PROVIDER = JACKSON_CODEC;

    String DEFAULT_VALUE_COMPRESSOR = NONE;
    int DEFAULT_VALUE_COMPRESSOR_LEVEL = -1;
    boolean DEFAULT_VALUE_COMPRESSOR_WRAP = false;

    String DEFAULT_LOCK_PROVIDER = EMBED_CACHE_LOCK;
    long DEFAULT_LOCK_LEASE_TIME = 1000;
    int DEFAULT_LOCK_INITIAL_CAPACITY = 256;

    String DEFAULT_REFRESH_PROVIDER = NONE;
    long DEFAULT_REFRESH_PERIOD = 1800000;
    long DEFAULT_REFRESH_STOP_AFTER_ACCESS = 7200000;

    String DEFAULT_SYNC_PROVIDER = LETTUCE;
    long DEFAULT_SYNC_MAX_LEN = 10000;

    String DEFAULT_STAT_PROVIDER = LOG_CACHE_STAT;
    long DEFAULT_STAT_PERIOD = 60000;

    // 扩展属性默认配置 end

}