package com.igeeksky.xcache.redis.lettuce;

import com.igeeksky.xcache.core.config.CacheConfig;
import com.igeeksky.xcache.core.store.RemoteStore;
import com.igeeksky.xcache.core.store.RemoteTestCase;
import com.igeeksky.xcache.redis.RedisStoreProvider;
import com.igeeksky.xcache.redis.RedisStringStore;
import com.igeeksky.xtool.core.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author patrick
 * @since 0.0.4 2024/5/30
 */
class RedisStringStoreTest {

    private static RedisStoreProvider provider;

    private static RemoteTestCase remoteStoreTest;

    @BeforeAll
    public static void beforeAll() {
        provider = LettuceTestHelper.createStandaloneRedisStoreProvider();

        CacheConfig<String, byte[]> cacheConfig = LettuceTestHelper.createCacheConfig(RedisStringStore.STORE_NAME);

        RemoteStore remoteStore = provider.getRemoteCacheStore(cacheConfig);

        remoteStoreTest = new RemoteTestCase(remoteStore);
    }

    @Test
    void get() {
        remoteStoreTest.get();
    }

    @Test
    void getAll() {
        remoteStoreTest.getAll();
    }

    @Test
    void put() {
        // 测试保存空值
        remoteStoreTest.putNullValue();

        // 测试保存空白字符串 “”
        remoteStoreTest.putEmptyValue();
    }

    @Test
    void putAll() {
        remoteStoreTest.putAll();
    }

    @Test
    void evict() {
        remoteStoreTest.evict();
    }

    @Test
    void evictAll() {
        remoteStoreTest.evictAll();
    }

    @Test
    void getStoreName() {
        Assertions.assertEquals(RedisStringStore.STORE_NAME, remoteStoreTest.getStoreName());
    }

    @Test
    void clear() {
        remoteStoreTest.clear();
    }

    @AfterAll
    public static void afterAll() {
        IOUtils.closeQuietly(provider);
    }
}