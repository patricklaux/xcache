package com.igeeksky.xcache.caffeine;

import com.github.benmanes.caffeine.cache.Expiry;
import com.igeeksky.xcache.common.CacheValue;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p><b>存活时间随机策略</b></p>
 * <p>创建（更新）后的最大存活时间：expireAfterCreate 的设定值</p>
 * <p>创建（更新）后的最小存活时间：expireAfterCreate * 0.8</p>
 * <p>访问后的存活时间：expiresAfterAccess 与 当前剩余存活时间 两者的大值，</p>
 * 即 Math.max(expiresAfterAccess, currentDuration)
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-16
 */
public class RandomRangeExpiry<K, V> implements Expiry<K, CacheValue<V>> {

    // 创建（更新）后的最小存活时间（单位：nanos）
    private final long expireAfterCreateNanosMin;

    // 创建（更新）后的最大存活时间（单位：nanos）
    private final long expireAfterCreateNanosMax;

    // 访问后的存活时间
    private final long expireAfterAccessNanos;

    public RandomRangeExpiry(@NonNull Duration expireAfterCreate, @NonNull Duration expireAfterAccess) {
        this.expireAfterCreateNanosMax = expireAfterCreate.toNanos();
        this.expireAfterCreateNanosMin = (long) (expireAfterCreateNanosMax * 0.8);
        this.expireAfterAccessNanos = expireAfterAccess.toNanos();
    }

    @Override
    public long expireAfterCreate(@NonNull K key, @NonNull CacheValue<V> cacheValue, long currentTime) {
        return ThreadLocalRandom.current().nextLong(expireAfterCreateNanosMin, expireAfterCreateNanosMax);
    }

    @Override
    public long expireAfterUpdate(@NonNull K key, @NonNull CacheValue<V> cacheValue, long currentTime, @NonNegative long currentDuration) {
        return expireAfterCreate(key, cacheValue, currentTime);
    }

    @Override
    public long expireAfterRead(@NonNull K key, @NonNull CacheValue<V> cacheValue, long currentTime, @NonNegative long currentDuration) {
        return Math.max(currentDuration, expireAfterAccessNanos);
    }

}