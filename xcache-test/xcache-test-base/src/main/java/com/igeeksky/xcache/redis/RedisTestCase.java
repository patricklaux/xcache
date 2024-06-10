package com.igeeksky.xcache.redis;

import com.igeeksky.xcache.common.ExpiryKeyValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.extension.serializer.StringSerializer;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.RandomUtils;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author patrick
 * @since 1.0.0 2024/5/31
 */
public class RedisTestCase {

    private static final StringSerializer SERIALIZER = StringSerializer.UTF_8;

    private final RedisConnection connection;

    public RedisTestCase(RedisConnection connection) {
        this.connection = connection;
    }

    public boolean isCluster() {
        return connection.isCluster();
    }

    // String Command --start--
    public void get() {
        byte[] key = SERIALIZER.serialize("test-get");

        connection.del(key);
        connection.set(key, key);

        byte[] result = connection.get(key);

        Assertions.assertArrayEquals(key, result);

        connection.del(key);
    }

    public void mget() {
        int size = 1024, limit = 768;

        String[] keys = getKeys(size, "mget");
        byte[][] keyBytes = getBytes(size, keys);

        // 删除 redis 中的 key-value
        connection.del(keyBytes);

        // 保存 key-value 到 redis
        connection.mset(getKeyValues(limit, keyBytes));

        // 读取 redis 数据
        Map<String, String> map = hmgetResultMap(connection.mget(keyBytes));

        // 验证读取数据是否正确
        validateValues(keys, map, size, limit);

        connection.del(keyBytes);
    }


    public void set() {
        byte[] bytes = SERIALIZER.serialize("test-set");

        connection.del(bytes);
        connection.set(bytes, bytes);

        byte[] result = connection.get(bytes);
        Assertions.assertArrayEquals(bytes, result);

        connection.del(bytes);
    }

    /**
     * 性能测试（200万数据，单线程）
     */
    public void psetex() {
        getPsetexRunnable(2000000).run();
    }

    /**
     * 性能测试（200万数据，10线程）
     */
    public void psetex2() throws InterruptedException {
        Runnable runnable = getPsetexRunnable(200000);

        for (int i = 0; i < 10; i++) {
            new Thread(runnable).start();
        }

        Thread.sleep(20000);
    }

    private Runnable getPsetexRunnable(int size) {
        return () -> {
            long start = System.currentTimeMillis();
            for (int i = 0; i < size; i++) {
                byte[] temp = SERIALIZER.serialize("user:" + RandomUtils.nextString(18));
                connection.psetex(temp, RandomUtils.nextInt(2000000, 3000000), temp);
            }
            long end = System.currentTimeMillis();
            System.out.println(end - start);
        };
    }

    public void mset() {
        int size = 3;
        String[] keys = getKeys(size, "test-mset:");
        byte[][] keyBytes = getBytes(size, keys);

        // 删除 redis 中的 key-value
        connection.del(keyBytes);

        // 保存 key-value 到 redis
        connection.mset(getKeyValues(size, keyBytes));

        // 读取 redis 数据
        Map<String, String> map = hmgetResultMap(connection.mget(keyBytes));

        // 验证读取数据是否正确
        for (String key : keys) {
            Assertions.assertEquals(key, map.get(key));
        }

        connection.del(keyBytes);
    }

    /**
     * 单线程，批量保存 1000 万数据
     */
    public void msetPerformance() {
        msetRunnable(10000000).run();
    }

    public void msetPerformance2() {
        Runnable runnable = msetRunnable(5000000);

        for (int i = 0; i < 2; i++) {
            new Thread(runnable).start();
        }

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Runnable msetRunnable(int size) {
        return () -> {
            long start = System.currentTimeMillis();
            Map<byte[], byte[]> keyValues = Maps.newHashMap(50000);
            for (int i = 0; i < size; ) {
                i++;
                byte[] temp = SERIALIZER.serialize("test-mset:" + RandomUtils.nextString(18));
                keyValues.put(temp, temp);
                if (keyValues.size() == 50000 || i == size) {
                    connection.mset(keyValues);
                    keyValues.clear();
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("mset-time:" + (end - start));
        };
    }

    public void mpsetex() {
        testMpsetex(9998);
        testMpsetex(9999);
        testMpsetex(10000);
        testMpsetex(10001);
        testMpsetex(10002);
        testMpsetex(19998);
        testMpsetex(19999);
        testMpsetex(20000);
        testMpsetex(20001);
        testMpsetex(20002);
    }

    private void testMpsetex(int size) {
        String prefix = "test-mpsetex:";
        String[] keys = getKeys(size, prefix);

        byte[][] keyBytes = getBytes(size, keys);

        // 删除 redis 中的 key-value
        connection.del(keyBytes);

        List<ExpiryKeyValue<byte[], byte[]>> keyValues = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            keyValues.add(new ExpiryKeyValue<>(keyBytes[i], keyBytes[i], RandomUtils.nextInt(1000000, 2000000)));
        }

        // 保存 key-value 到 redis
        connection.mpsetex(keyValues);

        // 读取 redis 数据
        Map<String, String> map = hmgetResultMap(connection.mget(keyBytes));

        // 验证读取数据是否正确
        for (String key : keys) {
            Assertions.assertEquals(key, map.get(key));
        }

        List<byte[]> matchKeys = connection.keys(SERIALIZER.serialize(prefix + "*"));
        Assertions.assertEquals(keys.length, matchKeys.size());

        connection.del(keyBytes);

        matchKeys = connection.keys(SERIALIZER.serialize(prefix + "*"));
        Assertions.assertEquals(0, matchKeys.size());
    }

    private static String[] getKeys(int size, String prefix) {
        String[] keys = new String[size];
        for (int i = 0; i < size; i++) {
            keys[i] = prefix + RandomUtils.nextString(18);
        }
        return keys;
    }

    /**
     * 性能测试（1000万数据，单线程）
     */
    public void mpsetex1() {
        getMpsetexRunnable(10000000).run();
    }

    /**
     * 性能测试（1000万数据，2线程）
     */
    public void mpsetex2() {
        Runnable runnable = getMpsetexRunnable(5000000);

        for (int i = 0; i < 2; i++) {
            new Thread(runnable).start();
        }

        try {
            Thread.sleep(40000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Runnable getMpsetexRunnable(int size) {
        return () -> {
            long start = System.currentTimeMillis();
            int capacity = Math.min(50000, size);
            List<ExpiryKeyValue<byte[], byte[]>> keyValues = new ArrayList<>(capacity);
            for (int i = 0; i < size; i++) {
                byte[] temp = SERIALIZER.serialize("test-mpsetex:" + RandomUtils.nextString(18));
                keyValues.add(new ExpiryKeyValue<>(temp, temp, RandomUtils.nextInt(2000000, 3000000)));
                if (keyValues.size() == capacity) {
                    connection.mpsetex(keyValues);
                    keyValues.clear();
                    capacity = Math.min(10000, size - i - 1);
                }
            }
            long end = System.currentTimeMillis();
            System.out.println(end - start);
        };
    }

    public void del() {
        del(500);
        del(501);
        del(502);
        del(999);
        del(1000);
        del(1001);
    }

    private void del(int size) {
        String prefix = "test-del-";
        byte[][] keys = new byte[size][];
        for (int i = 0; i < size; i++) {
            keys[i] = SERIALIZER.serialize(prefix + RandomUtils.nextString(18));
        }

        for (byte[] key : keys) {
            connection.set(key, key);
        }

        for (byte[] key : keys) {
            Assertions.assertArrayEquals(key, connection.get(key));
        }

        connection.del(keys);

        for (byte[] key : keys) {
            Assertions.assertNull(connection.get(key));
        }
    }
    // String Command --end--

    // Hash Command --start--
    public void hget() {
        byte[] key = SERIALIZER.serialize("test-hget");
        byte[] field = SERIALIZER.serialize("hget-field-1");

        connection.del(key);

        Assertions.assertNull(connection.hget(key, field));

        connection.hset(key, field, field);

        Assertions.assertArrayEquals(field, connection.hget(key, field));

        connection.del(key);
    }

    public void hset() {
        // do nothing
    }

    public void hmget() {
        byte[] key = SERIALIZER.serialize("test-hmget");

        int size = 8, limit = 5;
        String[] fields = getKeys(size, "test-hmget-");
        byte[][] fieldBytes = getBytes(size, fields);

        // 删除 redis 中的 key-value
        connection.del(key);

        Map<byte[], byte[]> keyValues = getKeyValues(limit, fieldBytes);

        // 保存 key-value 到 redis
        connection.hmset(key, keyValues);

        // 读取 redis 数据
        Map<String, String> map = hmgetResultMap(connection.hmget(key, fieldBytes));

        // 验证读取数据是否正确
        validateValues(fields, map, size, limit);

        connection.hdel(key, fieldBytes);

        Map<String, String> resultMap = hmgetResultMap(connection.hmget(key, fieldBytes));
        for (int i = 0; i < size; i++) {
            Assertions.assertNull(resultMap.get(fields[i]));
        }
    }


    public void hmset() {
        // do nothing
    }

    public void hdel() {
        // do nothing
    }
    // Hash Command --end--

    public void keys() {
        int size = 10;
        String prefix = "test-clear:";
        byte[] matches = SERIALIZER.serialize(prefix + "*");

        Map<byte[], byte[]> keyValues = Maps.newHashMap(size);
        for (int i = 0; i < size; i++) {
            byte[] keyBytes = SERIALIZER.serialize(prefix + i);
            keyValues.put(keyBytes, keyBytes);
        }

        connection.mset(keyValues);

        Map<String, String> result = getKeysResult(matches);
        for (int i = 0; i < 10; i++) {
            String key = prefix + i;
            Assertions.assertEquals(key, result.get(key));
        }

        connection.clear(matches);

        result = getKeysResult(matches);
        for (int i = 0; i < 10; i++) {
            String key = prefix + i;
            Assertions.assertNull(result.get(key));
        }
    }

    private Map<String, String> getKeysResult(byte[] matches) {
        List<byte[]> keys = connection.keys(matches);
        Map<String, String> result = Maps.newHashMap(keys.size());
        for (byte[] keyBytes : keys) {
            String key = SERIALIZER.deserialize(keyBytes);
            result.put(key, key);
        }
        return result;
    }

    public void clear(String match) {
        long start = System.currentTimeMillis();
        connection.clear(SERIALIZER.serialize(match));
        System.out.println("clear:" + (System.currentTimeMillis() - start));
    }

    private static byte[][] getBytes(int size, String[] keys) {
        byte[][] keyBytes = new byte[size][];
        for (int i = 0; i < size; i++) {
            keyBytes[i] = SERIALIZER.serialize(keys[i]);
        }
        return keyBytes;
    }

    private static Map<byte[], byte[]> getKeyValues(int size, byte[][] keyBytes) {
        Map<byte[], byte[]> keyValues = Maps.newHashMap(size);
        for (int i = 0; i < size; i++) {
            keyValues.put(keyBytes[i], keyBytes[i]);
        }
        return keyValues;
    }

    private static Map<String, String> hmgetResultMap(List<KeyValue<byte[], byte[]>> keyValues) {
        Map<String, String> map = Maps.newHashMap(keyValues.size());
        keyValues.forEach(keyValue -> {
            String field = SERIALIZER.deserialize(keyValue.getKey());
            String value = SERIALIZER.deserialize(keyValue.getValue());
            map.put(field, value);
        });
        return map;
    }

    private static void validateValues(String[] keys, Map<String, String> map, int size, int limit) {
        for (int i = 0; i < size; i++) {
            String key = keys[i];
            if (i < limit) {
                Assertions.assertEquals(key, map.get(key));
            } else {
                Assertions.assertNull(map.get(key));
            }
        }
    }

}