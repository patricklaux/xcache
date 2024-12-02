package com.igeeksky.xcache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Weigher;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.store.StoreConfig;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * CaffeineStore 测试
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
class CaffeineStoreTest {

    private static final StringCodec CODEC = StringCodec.getInstance(StandardCharsets.UTF_8);
    private CaffeineStore<Object> store;

    @BeforeEach
    void setUp() {
        Cache<String, CacheValue<Object>> caffeine = Caffeine.newBuilder()
                .expireAfterWrite(3, TimeUnit.SECONDS)
                .initialCapacity(128)
                .maximumSize(1024)
                .build();

        StoreConfig<Object> config = StoreConfig.builder(Object.class)
                .enableNullValue(true)
                .build();

        store = new CaffeineStore<>(caffeine, new CaffeineConfig<>(config));
    }

    @Test
    void get() throws InterruptedException {
        store.put("a", "a");
        Thread.sleep(2500);
        CacheValue<Object> cacheValue = store.getCacheValue("a");
        Assertions.assertEquals("a", cacheValue.getValue());
    }

    @Test
    void testGet() {
        String key = "temp_key";
        byte[] value = CODEC.encode("temp_value");
        store.put(key, value);

        CacheValue<Object> cacheValue = store.getCacheValue(key);
        Assertions.assertArrayEquals(value, (byte[]) cacheValue.getValue());
    }

    @Test
    void testWeigher() {
        Cache<String, CacheValue<Object>> caffeine = Caffeine.newBuilder()
                .expireAfterWrite(3, TimeUnit.SECONDS)
                .maximumWeight(100)
                .weigher(Weigher.singletonWeigher())
                .build();

        StoreConfig<byte[]> storeConfig = StoreConfig.builder(byte[].class)
                .enableNullValue(true)
                .build();

        CaffeineStore<byte[]> cache = new CaffeineStore<>(caffeine, new CaffeineConfig<>(storeConfig));

        String key = "temp_key";
        byte[] value = CODEC.encode("temp_value");
        cache.put(key, value);

        CacheValue<byte[]> cacheValue = cache.getCacheValue(key);
        Assertions.assertArrayEquals(value, cacheValue.getValue());
    }

    @Test
    void doGetAll() {
        String key = "temp_key";
        byte[] value = CODEC.encode("temp_value");
        store.put(key, value);

        Set<String> set = new HashSet<>();
        set.add(key);

        Map<String, CacheValue<Object>> all = store.getAllCacheValues(set);
        Assertions.assertTrue(all.containsKey(key));
        Assertions.assertArrayEquals(value, (byte[]) all.get(key).getValue());
    }

    @Test
    void put() {
    }

    @Test
    void doPutAll() {
    }

    @Test
    void remove() {
    }

    @Test
    void doRemoveAll() {
    }

    @Test
    void toStoreKey() {
    }

    @Test
    void getAll() {
    }

    @Test
    void putAll() {
    }

    @Test
    void removeAll() {
    }

    @Test
    void doStoreGet() {
    }

    @Test
    void doStorePut() {
    }

    @Test
    void doStorePutAll() {
    }

    @Test
    void doStoreRemove() {
    }

    @Test
    void doStoreRemoveAll() {
    }

    @Test
    void clear() {
    }

}