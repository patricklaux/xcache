package com.igeeksky.xcache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.igeeksky.xcache.core.CacheValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-14
 */
class RandomRangeExpiryTest {

    Cache<Object, CacheValue<Object>> cache;

    @BeforeEach
    void setUp() {
        Duration expireAfterCreate = Duration.ofMillis(2000);
        Duration expireAfterAccess = Duration.ofMillis(1000);
        RandomRangeExpiry<Object, Object> expiry = new RandomRangeExpiry<>(expireAfterCreate, expireAfterAccess);
        cache = Caffeine.newBuilder()
                .expireAfter(expiry)
                .maximumSize(1024)
                .build();
    }

    @Test
    void get() throws InterruptedException {
        cache.put("a", new CacheValue<>("a"));
        Thread.sleep(2001);
        CacheValue<Object> value = cache.getIfPresent("a");
        Assertions.assertNull(value);

        cache.put("b", new CacheValue<>("b"));
        Thread.sleep(1500);
        value = cache.getIfPresent("b");
        Assertions.assertNotNull(value);

        Thread.sleep(900);
        value = cache.getIfPresent("b");
        Assertions.assertNotNull(value);

        Thread.sleep(950);
        value = cache.getIfPresent("b");
        Assertions.assertNotNull(value);

        Thread.sleep(1001);
        value = cache.getIfPresent("b");
        Assertions.assertNull(value);
    }

}