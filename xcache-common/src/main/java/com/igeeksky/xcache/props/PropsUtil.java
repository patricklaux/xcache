package com.igeeksky.xcache.props;

import com.igeeksky.xcache.common.CacheConfigException;
import com.igeeksky.xcache.common.ReferenceType;
import com.igeeksky.xcache.common.StoreType;
import com.igeeksky.xtool.core.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 配置工具类
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-20
 */
public class PropsUtil {

    private static final Logger log = LoggerFactory.getLogger(PropsUtil.class);

    /**
     * “用户模板配置”覆盖 “默认模板配置”，生成 “最终模板配置”
     *
     * @param from 用户模板配置
     * @param to   默认模板配置
     */
    public static Template replaceTemplate(Template from, Template to) {
        to.setId(from.getId());
        replaceProps(from, to);
        return to;
    }

    /**
     * “最终模板配置” 转换为 “初始缓存配置(CacheProps 类型)”
     *
     * @param name     缓存名称
     * @param template 最终模板配置
     * @return 初始缓存配置
     */
    public static CacheProps buildCacheProps(String name, Template template) {
        CacheProps initProps = new CacheProps();
        initProps.setName(name);
        initProps.setTemplateId(template.getId());
        replaceProps(template, initProps);
        return initProps;
    }

    /**
     * “用户缓存配置” 覆盖 “初始缓存配置”，生成 “最终缓存配置”
     *
     * @param from “用户缓存配置”
     * @param to   “初始缓存配置”
     * @return “最终缓存配置”
     */
    public static CacheProps replaceCacheProps(CacheProps from, CacheProps to) {
        to.setName(from.getName());
        to.setTemplateId(from.getTemplateId());
        replaceProps(from, to);
        return to;
    }

    private static void replaceProps(AbstractProps from, AbstractProps to) {
        String charset = StringUtils.toUpperCase(from.getCharset());
        if (charset != null) {
            to.setCharset(charset);
        }

        String keyCodec = StringUtils.trimToNull(from.getKeyCodec());
        if (keyCodec != null) {
            to.setKeyCodec(keyCodec);
        }

        String cacheStat = StringUtils.trimToNull(from.getCacheStat());
        if (cacheStat != null) {
            to.setCacheStat(cacheStat);
        }

        String containsPredicate = StringUtils.trimToNull(from.getContainsPredicate());
        if (containsPredicate != null) {
            to.setContainsPredicate(containsPredicate);
        }

        replaceProps(from.getCacheLock(), to.getCacheLock());
        replaceProps(from.getCacheSync(), to.getCacheSync());
        replaceProps(from.getCacheRefresh(), to.getCacheRefresh());

        StoreProps first = from.getFirst();
        if (first != null) {
            StoreType storeType = first.getStoreType();
            if (Objects.equals(StoreType.EXTRA, storeType)) {
                to.setFirst(defaultExtraStoreProps());
            }
            replaceProps(first, to.getFirst());
        }

        StoreProps second = from.getSecond();
        if (second != null) {
            StoreType storeType = second.getStoreType();
            if (Objects.equals(StoreType.EMBED, storeType)) {
                to.setSecond(defaultEmbedStoreProps());
            }
            replaceProps(second, to.getSecond());
        }

        StoreProps third = from.getThird();
        if (third != null) {
            StoreType storeType = third.getStoreType();
            if (Objects.equals(StoreType.EMBED, storeType)) {
                to.setThird(defaultEmbedStoreProps());
            }
            replaceProps(third, to.getThird());
        }

        checkStoreType(new StoreProps[]{to.getFirst(), to.getSecond(), to.getThird()});
    }

    private static void checkStoreType(StoreProps[] stores) {
        int extraIndex = -1, embedCount = 0;
        for (int i = 0; i < stores.length; i++) {
            StoreType storeType = stores[i].getStoreType();
            if (storeType == StoreType.EXTRA) {
                extraIndex = i;
                continue;
            }
            // 内嵌缓存数量大于 1，输出警告日志
            if (++embedCount > 1) {
                log.warn("embed-store more than one.");
            }
            // 如果外部缓存的级别低于内嵌缓存，抛出异常
            if (extraIndex >= 0) {
                String error = String.format("extra-store level [%d] is lower than embed-store level [%d].", extraIndex + 1, i + 1);
                throw new CacheConfigException(error);
            }
        }
    }

    private static void replaceProps(LockProps from, LockProps to) {
        if (from == null) {
            return;
        }

        String provider = StringUtils.trimToNull(from.getProvider());
        if (provider != null) {
            to.setProvider(provider);
        }

        Integer initialCapacity = from.getInitialCapacity();
        if (initialCapacity != null) {
            to.setInitialCapacity(initialCapacity);
        }

        Long leaseTime = from.getLeaseTime();
        if (leaseTime != null) {
            to.setLeaseTime(leaseTime);
        }

        Boolean enableGroupPrefix = from.getEnableGroupPrefix();
        if (enableGroupPrefix != null) {
            to.setEnableGroupPrefix(enableGroupPrefix);
        }

        to.setParams(from.getParams());
    }

    private static void replaceProps(SyncProps from, SyncProps to) {
        if (from == null) {
            return;
        }

        SyncType first = from.getFirst();
        if (first != null) {
            to.setFirst(first);
        }

        SyncType second = from.getSecond();
        if (second != null) {
            to.setSecond(second);
        }

        Boolean enableGroupPrefix = from.getEnableGroupPrefix();
        if (enableGroupPrefix != null) {
            to.setEnableGroupPrefix(enableGroupPrefix);
        }

        Long maxLen = from.getMaxLen();
        if (maxLen != null) {
            to.setMaxLen(maxLen);
        }

        String provider = StringUtils.trimToNull(from.getProvider());
        if (provider != null) {
            to.setProvider(provider);
        }

        to.setParams(from.getParams());
    }

    private static void replaceProps(RefreshProps from, RefreshProps to) {
        if (from == null) {
            return;
        }

        Long period = from.getPeriod();
        if (period != null) {
            to.setPeriod(period);
        }

        String provider = StringUtils.trimToNull(from.getProvider());
        if (provider != null) {
            to.setProvider(provider);
        }

        Long stopAfterAccess = from.getStopAfterAccess();
        if (stopAfterAccess != null) {
            to.setStopAfterAccess(stopAfterAccess);
        }

        Boolean enableGroupPrefix = from.getEnableGroupPrefix();
        if (enableGroupPrefix != null) {
            to.setEnableGroupPrefix(enableGroupPrefix);
        }

        to.setParams(from.getParams());
    }

    private static void replaceProps(StoreProps from, StoreProps to) {
        if (from == null) {
            return;
        }

        StoreType storeType = from.getStoreType();
        if (storeType != null) {
            to.setStoreType(storeType);
        }

        RedisType redisType = from.getRedisType();
        if (redisType != null) {
            to.setRedisType(redisType);
        }

        String storeProvider = StringUtils.trimToNull(from.getProvider());
        if (storeProvider != null) {
            to.setProvider(storeProvider);
        }

        Integer initialCapacity = from.getInitialCapacity();
        if (initialCapacity != null) {
            to.setInitialCapacity(initialCapacity);
        }

        Long maximumSize = from.getMaximumSize();
        if (maximumSize != null) {
            to.setMaximumSize(maximumSize);
        }

        Long maximumWeight = from.getMaximumWeight();
        if (maximumWeight != null) {
            to.setMaximumWeight(maximumWeight);
        }

        Long expireAfterWrite = from.getExpireAfterWrite();
        if (expireAfterWrite != null) {
            to.setExpireAfterWrite(expireAfterWrite);
        }

        Long expireAfterAccess = from.getExpireAfterAccess();
        if (expireAfterAccess != null) {
            to.setExpireAfterAccess(expireAfterAccess);
        }

        ReferenceType keyStrength = from.getKeyStrength();
        if (keyStrength != null) {
            to.setKeyStrength(keyStrength);
        }

        ReferenceType valueStrength = from.getValueStrength();
        if (valueStrength != null) {
            to.setValueStrength(valueStrength);
        }

        String valueSerializer = StringUtils.trimToNull(from.getValueCodec());
        if (valueSerializer != null) {
            to.setValueCodec(valueSerializer);
        }

        Boolean enableGroupPrefix = from.getEnableGroupPrefix();
        if (enableGroupPrefix != null) {
            to.setEnableGroupPrefix(enableGroupPrefix);
        }

        Boolean enableRandomTtl = from.getEnableRandomTtl();
        if (enableRandomTtl != null) {
            to.setEnableRandomTtl(enableRandomTtl);
        }

        Boolean enableNullValue = from.getEnableNullValue();
        if (enableNullValue != null) {
            to.setEnableNullValue(enableNullValue);
        }

        replaceProps(from.getValueCompressor(), to.getValueCompressor());

        to.setParams(from.getParams());
    }

    private static void replaceProps(CompressProps from, CompressProps to) {
        if (from == null) {
            return;
        }

        Integer level = from.getLevel();
        if (level != null) {
            to.setLevel(level);
        }

        Boolean nowrap = from.getNowrap();
        if (nowrap != null) {
            to.setNowrap(nowrap);
        }

        String provider = StringUtils.trimToNull(from.getProvider());
        if (provider != null) {
            to.setProvider(provider);
        }

        to.setParams(from.getParams());
    }

    /**
     * 模板配置默认值
     *
     * @param id 缓存模板ID
     * @return {@link Template} 模板配置默认值
     */
    public static Template defaultTemplate(String id) {
        Template props = new Template();
        props.setId(id);
        props.setCharset(CacheConstants.DEFAULT_CHARSET_NAME);
        props.setKeyCodec(CacheConstants.DEFAULT_KEY_CODEC_PROVIDER);
        props.setCacheStat(CacheConstants.DEFAULT_STAT_PROVIDER);
        props.setContainsPredicate(CacheConstants.DEFAULT_PREDICATE_PROVIDER);

        props.setCacheLock(defaultLockProps());
        props.setCacheSync(defaultSyncProps());
        props.setCacheRefresh(defaultRefreshProps());

        props.setFirst(defaultEmbedStoreProps());
        props.setSecond(defaultExtraStoreProps());

        StoreProps third = defaultExtraStoreProps();
        third.setProvider(CacheConstants.NONE);
        props.setThird(third);

        return props;
    }

    /**
     * 缓存数据刷新默认配置
     *
     * @return {@link RefreshProps} 缓存数据刷新默认配置
     */
    public static RefreshProps defaultRefreshProps() {
        RefreshProps props = new RefreshProps();
        props.setPeriod(CacheConstants.DEFAULT_REFRESH_PERIOD);
        props.setProvider(CacheConstants.DEFAULT_REFRESH_PROVIDER);
        props.setStopAfterAccess(CacheConstants.DEFAULT_REFRESH_STOP_AFTER_ACCESS);
        props.setEnableGroupPrefix(CacheConstants.DEFAULT_ENABLE_GROUP_PREFIX);
        return props;
    }

    /**
     * 缓存锁默认配置
     *
     * @return {@link LockProps} 默认缓存锁配置
     */
    public static LockProps defaultLockProps() {
        LockProps props = new LockProps();
        props.setProvider(CacheConstants.DEFAULT_LOCK_PROVIDER);
        props.setLeaseTime(CacheConstants.DEFAULT_LOCK_LEASE_TIME);
        props.setInitialCapacity(CacheConstants.DEFAULT_LOCK_INITIAL_CAPACITY);
        props.setEnableGroupPrefix(CacheConstants.DEFAULT_ENABLE_GROUP_PREFIX);
        return props;
    }

    /**
     * 缓存数据同步默认配置
     *
     * @return {@link SyncProps} 缓存数据同步默认配置
     */
    public static SyncProps defaultSyncProps() {
        SyncProps props = new SyncProps();
        props.setFirst(SyncType.ALL);
        props.setSecond(SyncType.NONE);
        props.setMaxLen(CacheConstants.DEFAULT_SYNC_MAX_LEN);
        props.setProvider(CacheConstants.DEFAULT_SYNC_PROVIDER);
        props.setEnableGroupPrefix(CacheConstants.DEFAULT_ENABLE_GROUP_PREFIX);
        return props;
    }

    /**
     * 内嵌缓存默认配置
     *
     * @return {@link StoreProps} 内嵌缓存默认配置
     */
    public static StoreProps defaultEmbedStoreProps() {
        StoreProps props = new StoreProps();
        props.setStoreType(StoreType.EMBED);
        props.setRedisType(RedisType.STRING);

        props.setProvider(CacheConstants.DEFAULT_EMBED_STORE_PROVIDER);

        props.setInitialCapacity(CacheConstants.DEFAULT_EMBED_INITIAL_CAPACITY);
        props.setMaximumSize(CacheConstants.DEFAULT_EMBED_MAXIMUM_SIZE);
        props.setMaximumWeight(CacheConstants.DEFAULT_EMBED_MAXIMUM_WEIGHT);

        props.setExpireAfterWrite(CacheConstants.DEFAULT_EMBED_EXPIRE_AFTER_WRITE);
        props.setExpireAfterAccess(CacheConstants.DEFAULT_EMBED_EXPIRE_AFTER_ACCESS);

        props.setKeyStrength(CacheConstants.DEFAULT_EMBED_KEY_STRENGTH);
        props.setValueStrength(CacheConstants.DEFAULT_EMBED_VALUE_STRENGTH);

        props.setValueCodec(CacheConstants.DEFAULT_EMBED_VALUE_CODEC);
        props.setValueCompressor(defaultCompressProps());

        props.setEnableRandomTtl(CacheConstants.DEFAULT_EMBED_ENABLE_RANDOM_TTL);
        props.setEnableNullValue(CacheConstants.DEFAULT_EMBED_ENABLE_NULL_VALUE);

        props.setEnableGroupPrefix(CacheConstants.DEFAULT_EMBED_ENABLE_GROUP_PREFIX);
        return props;
    }

    /**
     * 外部缓存默认配置
     *
     * @return {@link StoreProps} 外部缓存默认配置
     */
    public static StoreProps defaultExtraStoreProps() {
        StoreProps props = new StoreProps();
        props.setStoreType(StoreType.EXTRA);
        props.setRedisType(RedisType.STRING);

        props.setProvider(CacheConstants.DEFAULT_EXTRA_STORE_PROVIDER);

        props.setInitialCapacity(CacheConstants.DEFAULT_EMBED_INITIAL_CAPACITY);
        props.setMaximumSize(CacheConstants.DEFAULT_EMBED_MAXIMUM_SIZE);
        props.setMaximumWeight(CacheConstants.DEFAULT_EMBED_MAXIMUM_WEIGHT);

        props.setExpireAfterWrite(CacheConstants.DEFAULT_EXTRA_EXPIRE_AFTER_WRITE);
        props.setExpireAfterAccess(CacheConstants.DEFAULT_EMBED_EXPIRE_AFTER_ACCESS);

        props.setKeyStrength(CacheConstants.DEFAULT_EMBED_KEY_STRENGTH);
        props.setValueStrength(CacheConstants.DEFAULT_EMBED_VALUE_STRENGTH);

        props.setValueCodec(CacheConstants.DEFAULT_EXTRA_VALUE_CODEC);
        props.setValueCompressor(defaultCompressProps());

        props.setEnableRandomTtl(CacheConstants.DEFAULT_EXTRA_ENABLE_RANDOM_TTL);
        props.setEnableNullValue(CacheConstants.DEFAULT_EXTRA_ENABLE_NULL_VALUE);

        props.setEnableGroupPrefix(CacheConstants.DEFAULT_EXTRA_ENABLE_GROUP_PREFIX);
        return props;
    }

    /**
     * 缓存数据压缩默认配置
     *
     * @return {@link CompressProps} 缓存数据压缩默认配置
     */
    public static CompressProps defaultCompressProps() {
        CompressProps props = new CompressProps();
        props.setLevel(CacheConstants.DEFAULT_VALUE_COMPRESSOR_LEVEL);
        props.setNowrap(CacheConstants.DEFAULT_VALUE_COMPRESSOR_WRAP);
        props.setProvider(CacheConstants.DEFAULT_VALUE_COMPRESSOR);
        return props;
    }

}