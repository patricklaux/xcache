package com.igeeksky.xcache.common.store;

import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.common.CacheValue;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

/**
 * @author patrick
 * @since 1.0.0 2024/5/30
 */
public class ExtraStoreTestCase {

    private final Store<String> store;

    public ExtraStoreTestCase(Store<String> store) {
        this.store = store;
    }

    public void get() {
        String key = "test-get";
        store.put(key, key);

        Assertions.assertEquals(key, store.get(key).getValue());

        store.evict(key);
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

        Map<String, CacheValue<String>> all = store.getAll(keyValues.keySet());
        keyValues.forEach((key, value) -> Assertions.assertEquals(value, all.get(key).getValue()));

        store.evictAll(keyValues.keySet());
    }

    /**
     * 测试空值
     * <p>
     * put(key, null)
     */
    public void putNullValue() {
        String key = "test-put-1";
        store.put(key, null);

        Assertions.assertNull(store.get(key).getValue());
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

        Assertions.assertEquals(value, store.get(key).getValue());
    }

    public void putAll() {
        // do nothing
    }

    public void evict() {
        String key = "test-evict";

        store.put(key, key);
        Assertions.assertEquals(key, store.get(key).getValue());

        store.evict(key);
        Assertions.assertNull(store.get(key));
    }

    public void evictAll() {
        // do nothing
    }

    public void clear() {
        store.clear();
    }

}