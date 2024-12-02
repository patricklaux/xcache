package com.igeeksky.xcache.common;

import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

/**
 * Store 公共测试用例
 *
 * @author patrick
 * @since 1.0.0 2024/5/30
 */
public class StoreTestCase {

    private final Store<String> store;

    public StoreTestCase(Store<String> store) {
        this.store = store;
    }

    public void get() {
        String key = "test-get";
        store.put(key, key);

        Assertions.assertEquals(key, store.getCacheValue(key).getValue());

        store.remove(key);
    }

    public void getAll() {
        Map<String, String> keyValues = new HashMap<>();
        String prefix = "test-getAll";
        for (int i = 0; i < 100; i++) {
            String key = prefix + i;
            keyValues.put(key, key);
        }
        store.putAll(keyValues);

        // 批量保存必须等一定时间才能完全生效
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Map<String, CacheValue<String>> all = store.getAllCacheValues(keyValues.keySet());
        keyValues.forEach((key, value) -> Assertions.assertEquals(value, all.get(key).getValue()));

        store.removeAll(keyValues.keySet());
    }

    /**
     * 测试空值
     * <p>
     * put(key, null)
     */
    public void putNullValue() {
        String key = "test-put-1";
        store.put(key, null);

        Assertions.assertNull(store.getCacheValue(key).getValue());
    }

    /**
     * 测试保存空白字符串
     * <p>
     * put(key, SERIALIZER.serialize(""))
     */
    public void putEmptyValue() {
        String key = "test-put-2";
        String value = "";
        store.put(key, value);

        Assertions.assertEquals(value, store.getCacheValue(key).getValue());
    }

    public void putAll() {
        // do nothing
    }

    public void evict() {
        String key = "test-evict";

        store.put(key, key);
        Assertions.assertEquals(key, store.getCacheValue(key).getValue());

        store.remove(key);
        Assertions.assertNull(store.getCacheValue(key));
    }

    public void evictAll() {
        // do nothing
    }

    public void clear() {
        store.clear();
    }

}