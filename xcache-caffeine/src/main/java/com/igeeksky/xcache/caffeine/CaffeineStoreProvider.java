package com.igeeksky.xcache.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Weigher;
import com.igeeksky.xcache.common.CacheConfigException;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.common.ReferenceType;
import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.core.store.StoreConfig;
import com.igeeksky.xcache.core.store.StoreProvider;
import com.igeeksky.xtool.core.lang.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caffeine 缓存提供者
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-13
 */
public class CaffeineStoreProvider implements StoreProvider {

    private static final Logger log = LoggerFactory.getLogger(CaffeineStoreProvider.class);

    private final Map<String, Expiry<String, CacheValue<Object>>> expires = new HashMap<>();
    private final Map<String, Weigher<String, CacheValue<Object>>> weighers = new HashMap<>();

    public CaffeineStoreProvider(List<CaffeineExpiryRegister> expiryRegisters,
                                 List<CaffeineWeigherRegister> weigherRegisters) {
        expiryRegisters.forEach(register -> {
            Map<String, Expiry<String, CacheValue<Object>>> map = register.getAll();
            map.forEach((name, expiry) -> {
                Expiry<String, CacheValue<Object>> old = expires.put(name, expiry);
                Assert.isTrue(old == null, () -> "Caffeine:Expiry: [" + name + "] duplicate id.");
            });
        });
        weigherRegisters.forEach(register -> {
            Map<String, Weigher<String, CacheValue<Object>>> map = register.getAll();
            map.forEach((name, weigher) -> {
                Weigher<String, CacheValue<Object>> old = weighers.put(name, weigher);
                Assert.isTrue(old == null, () -> "Caffeine:Weigher: [" + name + "] duplicate id.");
            });
        });
        if (log.isDebugEnabled()) {
            log.debug("Caffeine:Expires: [{}] ", expires);
            log.debug("Caffeine:Weighers: [{}] ", weighers);
        }
    }

    @Override
    public <V> Store<V> getStore(StoreConfig<V> storeConfig) {
        CaffeineConfig<V> config = new CaffeineConfig<>(storeConfig);

        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        // 1. 设置基于时间的驱逐策略
        // 1.1. 基于时间的自定义驱逐策略
        Expiry<String, CacheValue<Object>> expiry = expires.get(config.getName());
        if (expiry != null) {
            builder.expireAfter(expiry);
            return createCaffeineStore(config, builder);
        }

        boolean enableRandomTtl = config.isEnableRandomTtl();
        long expireAfterWrite = config.getExpireAfterWrite();
        long expireAfterAccess = config.getExpireAfterAccess();

        // 1.2. 基于随机时间的驱逐策略
        if (enableRandomTtl) {
            builder.expireAfter(createRandomExpiry(expireAfterWrite, expireAfterAccess));
            return createCaffeineStore(config, builder);
        }

        // 1.3. 基于固定时间的驱逐策略
        if (expireAfterWrite > 0) {
            builder.expireAfterWrite(Duration.ofMillis(expireAfterWrite));
        }
        if (expireAfterAccess > 0) {
            builder.expireAfterAccess(Duration.ofMillis(expireAfterAccess));
        }

        return createCaffeineStore(config, builder);
    }

    private static RandomRangeExpiry<String, Object> createRandomExpiry(long expireAfterWrite, long expireAfterAccess) {
        if (expireAfterWrite <= 0L) {
            throw new CacheConfigException("enableRandomTtl: expireAfterWrite must be greater than 0");
        }
        Duration durationWrite = Duration.ofMillis(expireAfterWrite);
        Duration durationWriteMin = Duration.ofMillis(expireAfterWrite * 4 / 5);
        Duration durationAccess = Duration.ofMillis(expireAfterAccess);
        return new RandomRangeExpiry<>(durationWrite, durationWriteMin, durationAccess);
    }

    private <V> Store<V> createCaffeineStore(CaffeineConfig<V> config, Caffeine<Object, Object> builder) {
        // 2. 设置初始化缓存容量
        int initialCapacity = config.getInitialCapacity();
        if (initialCapacity > 0) {
            builder.initialCapacity(initialCapacity);
        }

        // 3. 基于容量的驱逐策略
        long maximumSize = config.getMaximumSize();
        if (maximumSize > 0) {
            builder.maximumSize(maximumSize);
        }

        // 4. 基于权重的驱逐策略
        long maximumWeight = config.getMaximumWeight();
        if (maximumWeight > 0) {
            builder.maximumWeight(maximumWeight);
            Weigher<String, CacheValue<Object>> weigher = weighers.get(config.getName());
            if (null != weigher) {
                builder.weigher(weigher);
            } else {
                builder.weigher(Weigher.singletonWeigher());
            }
        }

        // 5. 基于引用的驱逐策略
        // 5.1. Key 的弱引用驱逐策略
        ReferenceType keyStrength = config.getKeyStrength();
        if (keyStrength != null && keyStrength != ReferenceType.STRONG) {
            if (keyStrength == ReferenceType.WEAK) {
                builder.weakKeys();
            } else if (keyStrength == ReferenceType.SOFT || keyStrength == ReferenceType.PHANTOM) {
                throw new CacheConfigException("keyStrength:" + keyStrength + "] can only be set to 'weak'");
            }
        }

        // 5.2. value 的弱引用和软引用驱逐策略
        ReferenceType valueStrength = config.getValueStrength();
        if (valueStrength != null && valueStrength != ReferenceType.STRONG) {
            if (valueStrength == ReferenceType.WEAK) {
                builder.weakValues();
            } else if (valueStrength == ReferenceType.SOFT) {
                builder.softValues();
            } else if (valueStrength == ReferenceType.PHANTOM) {
                throw new CacheConfigException("valueStrength:[" + valueStrength + "] can only be set to 'weak' or 'soft'");
            }
        }

        return new CaffeineStore<>(builder.build(), config);
    }

}
