package com.igeeksky.redis.lettuce;

import com.igeeksky.xcache.core.Store;
import com.igeeksky.xcache.core.store.ExtraStoreTestCase;
import com.igeeksky.xcache.core.store.StoreConfig;
import com.igeeksky.xcache.props.RedisType;
import com.igeeksky.xcache.redis.store.RedisStoreProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author patrick
 * @since 0.0.4 2024/5/30
 */
class LettuceStringStoreTest {

    private static ExtraStoreTestCase remoteStoreTest;

    @BeforeAll
    public static void beforeAll() {
        RedisStoreProvider provider = LettuceTestHelper.createStandaloneRedisStoreProvider();

        StoreConfig<String> storeConfig = LettuceTestHelper.createRedisStoreConfig(RedisType.STRING);

        Store<String> store = provider.getStore(storeConfig);

        remoteStoreTest = new ExtraStoreTestCase(store);
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
    void clear() {
        remoteStoreTest.clear();
    }

}