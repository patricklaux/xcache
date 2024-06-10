package com.igeeksky.xcache.core.store;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.extension.serializer.StringSerializer;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

/**
 * @author patrick
 * @since 1.0.0 2024/5/30
 */
public class RemoteTestCase {

    private final RemoteStore remoteStore;

    private static final StringSerializer SERIALIZER = StringSerializer.UTF_8;

    public RemoteTestCase(RemoteStore remoteStore) {
        this.remoteStore = remoteStore;
    }

    public void get() {
        String key = "test-get";
        byte[] value = SERIALIZER.serialize(key);

        remoteStore.put(key, value);

        Assertions.assertArrayEquals(value, remoteStore.get(key).getValue());

        remoteStore.evict(key);
    }

    public void getAll() {
        Map<String, byte[]> keyValues = new HashMap<>();
        String prefix = "test-getAll";
        for (int i = 0; i < 100; i++) {
            String key = prefix + i;
            keyValues.put(key, SERIALIZER.serialize(key));
        }
        remoteStore.putAll(keyValues);

        // 批量保存必须等一定时间才能完全生效
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Map<String, CacheValue<byte[]>> all = remoteStore.getAll(keyValues.keySet());
        keyValues.forEach((key, value) -> Assertions.assertArrayEquals(value, all.get(key).getValue()));

        remoteStore.evictAll(keyValues.keySet());
    }

    /**
     * 测试空值
     * <p>
     * put(key, null)
     */
    public void putNullValue() {
        String key = "test-put-1";

        remoteStore.put(key, null);

        CacheValue<byte[]> cacheValue = remoteStore.get(key);

        Assertions.assertArrayEquals(SERIALIZER.serialize(""), cacheValue.getValue());
    }

    /**
     * 测试保存空白字符串
     * <p>
     * put(key, SERIALIZER.serialize(""))
     */
    public void putEmptyValue() {
        String key = "test-put-2";
        byte[] value = SERIALIZER.serialize("");

        remoteStore.put(key, value);

        CacheValue<byte[]> cacheValue = remoteStore.get(key);

        Assertions.assertArrayEquals(value, cacheValue.getValue());
    }

    public void putAll() {
        // do nothing
    }

    public void evict() {
        String key = "test-evict";
        byte[] value = SERIALIZER.serialize(key);

        remoteStore.put(key, value);

        Assertions.assertNotNull(remoteStore.get(key).getValue());

        remoteStore.evict(key);

        Assertions.assertNull(remoteStore.get(key));
    }

    public void evictAll() {
        // do nothing
    }

    public String getStoreName() {
        return remoteStore.getStoreName();
    }

    public void clear() {
        remoteStore.clear();
    }
}