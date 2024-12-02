package com.igeeksky.redis.lettuce;

import com.igeeksky.xcache.common.StoreTestCase;
import com.igeeksky.xcache.core.store.StoreConfig;
import com.igeeksky.xcache.props.RedisType;
import com.igeeksky.xcache.redis.store.RedisStoreProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Lettuce Hash 存储测试
 *
 * @author patrick
 * @since 0.0.4 2024/5/30
 */
class LettuceHashStoreTest {

    private static StoreTestCase extraStoreTestCase;

    @BeforeAll
    public static void beforeAll() {
        RedisStoreProvider provider = LettuceTestHelper.createStandaloneRedisStoreProvider();

        StoreConfig<String> storeConfig = LettuceTestHelper.createRedisStoreConfig(RedisType.HASH);

        extraStoreTestCase = new StoreTestCase(provider.getStore(storeConfig));
    }

    @Test
    void get() {
        extraStoreTestCase.get();
    }

    @Test
    void getAll() {
        extraStoreTestCase.getAll();
    }

    @Test
    void put() {
        // 测试保存空值
        extraStoreTestCase.putNullValue();

        // 测试保存空白字符串 “”
        extraStoreTestCase.putEmptyValue();
    }

    @Test
    void putAll() {
        extraStoreTestCase.putAll();
    }

    @Test
    void evict() {
        extraStoreTestCase.evict();
    }

    @Test
    void evictAll() {
        extraStoreTestCase.evictAll();
    }

    @Test
    void clear() {
        extraStoreTestCase.clear();
    }

}