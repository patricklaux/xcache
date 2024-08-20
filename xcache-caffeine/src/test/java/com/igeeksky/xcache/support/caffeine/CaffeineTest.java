package com.igeeksky.xcache.support.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.igeeksky.xcache.caffeine.RandomRangeExpiry;
import com.igeeksky.xcache.core.ExpiryCacheValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-16
 */
class CaffeineTest {

    final Cache<String, ExpiryCacheValue<String>> cacheStore
            = Caffeine.newBuilder()
            .expireAfter(new RandomRangeExpiry<String, String>(Duration.ofMillis(5000), Duration.ofMillis(5000)))
            .maximumSize(128)
            .build();

    @Test
    void get() throws InterruptedException {
        String key = "a";
        ExpiryCacheValue<String> value = new ExpiryCacheValue<>("a");
        cacheStore.put(key, value);
        Thread.sleep(1000);
        ExpiryCacheValue<String> cacheValue = cacheStore.getIfPresent(key);
        System.out.println("01:   " + (null != cacheValue ? cacheValue.getValue() : null));
        Thread.sleep(1500);
        cacheValue = cacheStore.getIfPresent(key);
        System.out.println("02:   " + (null != cacheValue ? cacheValue.getValue() : null));
        Thread.sleep(4900);
        cacheValue = cacheStore.getIfPresent(key);
        System.out.println("03:   " + (null != cacheValue ? cacheValue.getValue() : null));
        Thread.sleep(5000);
        cacheValue = cacheStore.getIfPresent(key);
        System.out.println("04:   " + (null != cacheValue ? cacheValue.getValue() : null));
    }

    @Test
    void getAll() {
    }

    @Test
    void put() {

    }

    @Test
    void putAll() {
    }

    @Test
    void remove() {
    }

    @Test
    void clear() {
    }


    @BeforeEach
    void setUp() {
        System.out.println("start-------------");
    }

    @AfterEach
    void tearDown() {
        System.out.println("end-------------");
    }

}