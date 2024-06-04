package com.igeeksky.xcache.caffeine;

import com.github.benmanes.caffeine.cache.Expiry;
import com.igeeksky.xcache.common.ExpiryCacheValue;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-16
 */
public class RandomRangeCacheExpiry<K, V> implements Expiry<K, ExpiryCacheValue<V>> {

    private final long originExpireAfterCreateNanos;
    private final long nullValueExpireAfterCreateNanos;
    private final long expireAfterCreateNanos;
    private final long expiresAfterAccessNanos;

    public RandomRangeCacheExpiry(@NonNull Duration expireAfterCreate, @NonNull Duration expiresAfterAccess) {
        this.expireAfterCreateNanos = expireAfterCreate.toNanos();
        this.originExpireAfterCreateNanos = expireAfterCreateNanos - (long) (expireAfterCreateNanos * 0.1);
        this.nullValueExpireAfterCreateNanos = (long) (expireAfterCreateNanos * 0.5);
        this.expiresAfterAccessNanos = expiresAfterAccess.toNanos();
    }

    @Override
    public long expireAfterCreate(@NonNull K key, @NonNull ExpiryCacheValue<V> cacheValue, long currentTime) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        long duration = (cacheValue.getValue() != null) ?
                random.nextLong(originExpireAfterCreateNanos, expireAfterCreateNanos)
                : random.nextLong(nullValueExpireAfterCreateNanos, originExpireAfterCreateNanos);
        cacheValue.setExpiryTime(currentTime + duration);
        return duration;
    }

    @Override
    public long expireAfterUpdate(@NonNull K key, @NonNull ExpiryCacheValue<V> cacheValue,
                                  long currentTime, @NonNegative long currentDuration) {
        return expireAfterCreate(key, cacheValue, currentTime);
    }

    @Override
    public long expireAfterRead(@NonNull K key, @NonNull ExpiryCacheValue<V> cacheValue,
                                long currentTime, @NonNegative long currentDuration) {
        long duration = cacheValue.getExpiryTime() - currentTime;
        if (duration < 0) {
            return 0L;
        }
        return Math.min(duration, expiresAfterAccessNanos);
    }
}
