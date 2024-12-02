package com.igeeksky.xcache.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.igeeksky.xcache.common.CacheValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caffeine ◊‘∂Øº”‘ÿ≤‚ ‘
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-14
 */
class LoadingCacheTest {

    Map<String, Integer> map = new ConcurrentHashMap<>();
    LoadingCache<String, CacheValue<Integer>> cache;

    @BeforeEach
    void setUp() {
        Duration expireAfterCreate = Duration.ofMillis(5000);
        Duration expireAfterAccess = Duration.ofMillis(3000);
        cache = Caffeine.newBuilder()
                .expireAfterWrite(expireAfterCreate)
                //.expireAfterAccess(expireAfterAccess)
                .refreshAfterWrite(Duration.ofMillis(1200))
                .maximumSize(1024)
                .build(key -> new CacheValue<>(map.compute(key, (k, v) -> {
                    System.out.println("compute\t" + System.currentTimeMillis());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return (v == null) ? 1 : ++v;
                })));
    }

    @Test
    void get() throws InterruptedException {
        cache.put("a", new CacheValue<>(0));
        System.out.println("1\t" + System.currentTimeMillis());
        CacheValue<Integer> value = cache.get("a");
        System.out.println("1\t" + value + System.currentTimeMillis());
        Assertions.assertEquals(0, value.getValue());

        Thread.sleep(500);
        System.out.println("2\t" + System.currentTimeMillis());
        value = cache.get("a");
        System.out.println("2\t" + value + System.currentTimeMillis());
        Assertions.assertEquals(0, value.getValue());

        Thread.sleep(500);
        System.out.println("3\t" + System.currentTimeMillis());
        value = cache.get("a");
        System.out.println("3\t" + value + System.currentTimeMillis());
        Assertions.assertEquals(0, value.getValue());

        Thread.sleep(500);
        System.out.println("4\t" + System.currentTimeMillis());
        value = cache.get("a");
        System.out.println("4\t" + value + System.currentTimeMillis());
        Assertions.assertEquals(0, value.getValue());

        Thread.sleep(500);
        System.out.println("5\t" + System.currentTimeMillis());
        value = cache.get("a");
        System.out.println("5\t" + value + System.currentTimeMillis());
        Assertions.assertEquals(1, value.getValue());

        Thread.sleep(500);
        System.out.println("6\t" + System.currentTimeMillis());
        value = cache.get("a");
        System.out.println("6\t" + value + System.currentTimeMillis());
        Assertions.assertEquals(1, value.getValue());

        Thread.sleep(500);
        System.out.println("7\t" + System.currentTimeMillis());
        value = cache.get("a");
        System.out.println("7\t" + value + System.currentTimeMillis());
        Assertions.assertEquals(1, value.getValue());

        Thread.sleep(500);
        System.out.println("8\t" + System.currentTimeMillis());
        value = cache.get("a");
        System.out.println("8\t" + value + System.currentTimeMillis());
        Assertions.assertEquals(2, value.getValue());

        // Thread.sleep(10000);
    }

    @Test
    void get2() throws InterruptedException {
        System.out.println("1");
        CacheValue<Integer> value = cache.get("a");
        System.out.println("1\t" + value);
        Assertions.assertNotNull(value);

        Thread.sleep(500);
        System.out.println("2");
        value = cache.get("a");
        System.out.println("2\t" + value);
        Assertions.assertNotNull(value);


        Thread.sleep(500);
        System.out.println("2");
        value = cache.get("a");
        System.out.println("2\t" + value);
        Assertions.assertNotNull(value);


        Thread.sleep(500);
        System.out.println("3");
        value = cache.get("a");
        System.out.println("3\t" + value);
        Assertions.assertNotNull(value);

        Thread.sleep(500);
        System.out.println("4");
        value = cache.get("a");
        System.out.println("4\t" + value);
        Assertions.assertNotNull(value);
    }

}