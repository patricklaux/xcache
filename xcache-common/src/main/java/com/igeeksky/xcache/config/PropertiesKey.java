package com.igeeksky.xcache.config;


import com.igeeksky.xcache.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
public abstract class PropertiesKey {

    public static final long UN_SET = -1L;

    public static final String CLASS_NAME = "class-name";

    public static final String PROVIDER_ID = "id";

    /* extension----key----start */
    public static final String EXTENSION_REDIS_WRITER = "redis-writer";

    public static final String EXTENSION_COMPRESSOR = "compressor";

    public static final String EXTENSION_KEY_SERIALIZER = "key-serializer";

    public static final String EXTENSION_VALUE_SERIALIZER = "value-serializer";

    public static final String EXTENSION_MSG_SERIALIZER = "statistics-serializer";

    public static final String EXTENSION_CACHE_LOCK = "cache-lock";

    public static final String EXTENSION_CACHE_UPDATE = "cache-update";

    public static final String EXTENSION_CACHE_LOADER = "cache-loader";

    public static final String EXTENSION_CACHE_WRITER = "cache-writer";

    public static final String EXTENSION_CACHE_MONITOR = "cache-monitor";

    public static final String EXTENSION_CACHE_STATISTIC = "cache-statistics";

    public static final String EXTENSION_CONTAINS_PREDICATE = "contains-predicate";

    public static final String EXTENSION_EVENT_SERIALIZER = "event-serializer";
    /* extension----key----end */


    /* preset----extension----start */
    public static final String PRESET_CACHE_LOCK = "localCacheLockProvider";

    public static final String PRESET_CONTAINS_PREDICATE = "noOpContainsPredicateProvider";
    /* preset----extension----end */


    /* metadata----key----start */
    public static final String METADATA = "metadata";

    public static final String METADATA_LOCK_SIZE = "lock-size";

    public static final String METADATA_MAXIMUM_SIZE = "maximum-size";

    public static final String METADATA_MAXIMUM_WEIGHT = "maximum-weight";

    public static final String METADATA_EXPIRE_AFTER_ACCESS = "expire-after-access";

    public static final String METADATA_EXPIRE_AFTER_WRITE = "expire-after-write";

    public static final String METADATA_NAMESPACE = "namespace";

    public static final String METADATA_USE_KEY_PREFIX = "use-key-prefix";

    public static final String METADATA_CACHE_NULL_VALUE = "cache-null-value";

    public static final String METADATA_ENABLE_SERIALIZE = "enable-serialize";

    public static final String METADATA_ENABLE_COMPRESS = "enable-compress";

    public static final String METADATA_ENABLE_STATISTIC = "enable-statistic";

    public static final String LOCAL_LOCk_SIZE = "lock-size";
    /* metadata----key----end */

    public static <K, V> boolean getBoolean(Map<K, V> map, K key, boolean defaultValue) {
        String value = getString(map, key);
        return (StringUtils.isNotEmpty(value)) ? Boolean.parseBoolean(value) : defaultValue;
    }

    public static <K, V> Integer getInteger(Map<K, V> map, K key, Integer defaultValue) {
        String value = getString(map, key);
        return (StringUtils.isNotEmpty(value)) ? Integer.valueOf(value) : defaultValue;
    }

    public static <K, V> Long getLong(Map<K, V> map, K key, Long defaultValue) {
        String value = getString(map, key);
        return (StringUtils.isNotEmpty(value)) ? Long.valueOf(value) : defaultValue;
    }

    public static <K, V> String getString(Map<K, V> map, K key, String defaultValue) {
        String value = getString(map, key);
        return (null != value) ? value : defaultValue;
    }

    public static <K, V> String getString(Map<K, V> map, K key) {
        return StringUtils.trim(getObject(map, key).toString());
    }

    @SuppressWarnings("unchecked")
    public static <K, V> HashMap<String, Object> getHashMap(Map<K, V> map, K key) {
        return (null == map) ? null : (HashMap<String, Object>) map.get(key);
    }

    public static <K, V> V getObject(Map<K, V> map, K key) {
        return (null == map) ? null : map.get(key);
    }

    public static <K, V> V getObject(Map<K, V> map, K key, V defaultValue) {
        V value = getObject(map, key);
        return (null != value) ? value : defaultValue;
    }

}
