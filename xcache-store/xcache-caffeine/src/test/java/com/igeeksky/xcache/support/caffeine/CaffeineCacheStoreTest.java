package com.igeeksky.xcache.support.caffeine;

import com.igeeksky.xcache.Cache;
import com.igeeksky.xcache.common.CacheValue;
import org.junit.jupiter.api.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-16
 */
class CaffeineCacheStoreTest {

    static Cache<String, String> cache;

    @BeforeAll
    static void beforeAll() {
        String name = "test";
        String namespace = "default";

    }

    @Test
    void get() throws InterruptedException {
        String key = "a";
        String value = "a";
        cache.put(key, Mono.just(value)).block();
        Thread.sleep(900);
        CacheValue<String> cacheValue = cache.get(key).block();
        Assertions.assertNotNull((null != cacheValue ? cacheValue.getValue() : null), "01");
        Thread.sleep(4500);
        cacheValue = cache.get("a").block();
        Assertions.assertNotNull((null != cacheValue ? cacheValue.getValue() : null), "02");
        Thread.sleep(3500);
        cacheValue = cache.get("a").block();
        Assertions.assertNotNull((null != cacheValue ? cacheValue.getValue() : null), "03");
        Thread.sleep(5000);
        cacheValue = cache.get("a").block();
        Assertions.assertNull((null != cacheValue ? cacheValue.getValue() : null), "04");
    }

    @Test
    void getAll() {
        LinkedHashSet<String> keySet = new LinkedHashSet<>(128);
        String prefix = "a";
        Map<String, String> map = new HashMap<>(128);
        for (int i = 0; i < 100; i++) {
            map.put(prefix + i, prefix + i);
            keySet.add(prefix + i);
        }

        cache.putAll(Mono.just(map)).subscribe();

        cache.getAll(keySet).subscribe(kv -> System.out.println(kv.getKey() + " : " + kv.getValue().getValue()));

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
    void beforeEach() {
        System.out.println("start-------------");
    }

    @AfterEach
    void afterEach() {
        System.out.println("end-------------");
    }

}