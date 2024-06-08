package com.igeeksky.xcache.redis.lettuce;

import com.igeeksky.xcache.core.config.CacheConfig;
import com.igeeksky.xcache.core.store.RemoteStoreTest;
import com.igeeksky.xcache.redis.RedisHashStore;
import com.igeeksky.xcache.redis.RedisStoreProvider;
import com.igeeksky.xtool.core.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author patrick
 * @since 0.0.4 2024/5/30
 */
class RedisHashStoreTest {

    private static RedisStoreProvider provider;

    private static RemoteStoreTest remoteStoreTest;

    @BeforeAll
    public static void beforeAll() {
        provider = LettuceTestHelper.createStandaloneRedisStoreProvider();

        CacheConfig<String, byte[]> cacheConfig = LettuceTestHelper.createCacheConfig(RedisHashStore.STORE_NAME);

        remoteStoreTest = new RemoteStoreTest(provider.getRemoteCacheStore(cacheConfig));
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
        Assertions.assertEquals(RedisHashStore.STORE_NAME, remoteStoreTest.getStoreName());
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