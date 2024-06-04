package com.igeeksky.xcache.config;


/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
public abstract class PropertiesKey {

    public static final long UN_SET = -1L;


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
}
