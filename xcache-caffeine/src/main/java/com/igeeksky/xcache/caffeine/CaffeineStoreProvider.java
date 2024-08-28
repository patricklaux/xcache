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
import com.igeeksky.xtool.core.collection.CollectionUtils;

import java.time.Duration;
import java.util.List;

/**
 * Caffeine 缓存提供者
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-13
 */
public class CaffeineStoreProvider implements StoreProvider {

    private final List<CaffeineExpiryProvider> expiryProviders;
    private final List<CaffeineWeigherProvider> weigherProviders;

    public CaffeineStoreProvider(List<CaffeineExpiryProvider> expiryProviders,
                                 List<CaffeineWeigherProvider> weigherProviders) {
        this.expiryProviders = expiryProviders;
        this.weigherProviders = weigherProviders;
    }

    @Override
    public <V> Store<V> getStore(StoreConfig<V> storeConfig) {
        CaffeineConfig<V> config = new CaffeineConfig<>(storeConfig);

        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        // 1. 设置基于时间的驱逐策略
        // 1.1. 基于时间的自定义驱逐策略
        if (CollectionUtils.isNotEmpty(expiryProviders)) {
            for (CaffeineExpiryProvider expiryProvider : expiryProviders) {
                Expiry<String, CacheValue<Object>> expiry = expiryProvider.get(config.getName());
                if (expiry != null) {
                    builder.expireAfter(expiry);
                    return createCaffeineStore(config, builder);
                }
            }
        }

        boolean enableRandomTtl = config.isEnableRandomTtl();
        long expireAfterWrite = config.getExpireAfterWrite();
        long expireAfterAccess = config.getExpireAfterAccess();

        // 1.2. 基于随机时间的驱逐策略
        if (enableRandomTtl) {
            if (expireAfterWrite <= 0L) {
                throw new CacheConfigException("enableRandomTtl: expireAfterWrite must be greater than 0");
            }
            Duration durationWrite = Duration.ofMillis(expireAfterWrite);
            Duration durationAccess = Duration.ofMillis(expireAfterAccess);
            RandomRangeExpiry<String, Object> expiry = new RandomRangeExpiry<>(durationWrite, durationAccess);
            builder.expireAfter(expiry);
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
            boolean hasWeigher = false;
            builder.maximumWeight(maximumWeight);
            if (CollectionUtils.isNotEmpty(weigherProviders)) {
                for (CaffeineWeigherProvider weigherProvider : weigherProviders) {
                    Weigher<String, CacheValue<Object>> weigher = weigherProvider.get(config.getName());
                    if (null != weigher) {
                        builder.weigher(weigher);
                        hasWeigher = true;
                        break;
                    }
                }
            }
            if (!hasWeigher) {
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
