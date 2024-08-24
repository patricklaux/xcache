package com.igeeksky.redis;

import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.function.tuple.ExpiryKeyValue;
import com.igeeksky.xtool.core.function.tuple.KeyValue;
import com.igeeksky.xtool.core.lang.RandomUtils;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import org.junit.jupiter.api.Assertions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author patrick
 * @since 1.0.0 2024/5/31
 */
public class RedisOperatorTestCase {

    private final StringCodec codec = StringCodec.getInstance(StandardCharsets.UTF_8);

    private final RedisOperator redisOperator;

    public RedisOperatorTestCase(RedisOperator redisOperator) {
        this.redisOperator = redisOperator;
    }

    public boolean isCluster() {
        return redisOperator.isCluster();
    }

    // String Command --start--
    public void get() {
        byte[] key = codec.encode("test-get");

        redisOperator.del(key);
        redisOperator.set(key, key);

        byte[] result = redisOperator.get(key);

        Assertions.assertArrayEquals(key, result);

        redisOperator.del(key);
    }

    public void mget() {
        int size = 1024, limit = 768;

        String[] keys = createKeys(size, "mget");
        byte[][] keyBytes = this.toKeysArray(size, keys);

        // 删除 redis 中的 key-value
        redisOperator.del(keyBytes);

        // 保存 key-value 到 redis
        redisOperator.mset(createKeyValues(limit, keyBytes));

        // 读取 redis 数据
        Map<String, String> map = this.hmgetResultMap(redisOperator.mget(keyBytes));

        // 验证读取数据是否正确
        this.validateValues(keys, map, size, limit);

        redisOperator.del(keyBytes);
    }


    public void set() {
        byte[] bytes = codec.encode("test-set");

        redisOperator.del(bytes);
        redisOperator.set(bytes, bytes);

        byte[] result = redisOperator.get(bytes);
        Assertions.assertArrayEquals(bytes, result);

        redisOperator.del(bytes);
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
                byte[] temp = codec.encode("user:" + RandomUtils.nextString(18));
                redisOperator.psetex(temp, RandomUtils.nextInt(2000000, 3000000), temp);
            }
            long end = System.currentTimeMillis();
            System.out.println(end - start);
        };
    }

    public void mset() {
        int size = 3;
        String[] keys = createKeys(size, "test-mset:");
        byte[][] keyBytes = this.toKeysArray(size, keys);

        // 删除 redis 中的 key-value
        redisOperator.del(keyBytes);

        // 保存 key-value 到 redis
        redisOperator.mset(createKeyValues(size, keyBytes));

        // 读取 redis 数据
        Map<String, String> map = this.hmgetResultMap(redisOperator.mget(keyBytes));

        // 验证读取数据是否正确
        for (String key : keys) {
            Assertions.assertEquals(key, map.get(key));
        }

        redisOperator.del(keyBytes);
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
                byte[] temp = codec.encode("test-mset:" + RandomUtils.nextString(18));
                keyValues.put(temp, temp);
                if (keyValues.size() == 50000 || i == size) {
                    redisOperator.mset(keyValues);
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
        String[] keys = createKeys(size, prefix);

        byte[][] keyBytes = this.toKeysArray(size, keys);

        // 删除 redis 中的 key-value
        redisOperator.del(keyBytes);

        List<ExpiryKeyValue<byte[], byte[]>> keyValues = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            keyValues.add(new ExpiryKeyValue<>(keyBytes[i], keyBytes[i], RandomUtils.nextInt(1000000, 2000000)));
        }

        // 保存 key-value 到 redis
        redisOperator.mpsetex(keyValues);

        // 读取 redis 数据
        Map<String, String> map = this.hmgetResultMap(redisOperator.mget(keyBytes));

        // 验证读取数据是否正确
        for (String key : keys) {
            Assertions.assertEquals(key, map.get(key));
        }

        List<byte[]> matchKeys = redisOperator.keys(codec.encode(prefix + "*"));
        Assertions.assertEquals(keys.length, matchKeys.size());

        redisOperator.del(keyBytes);

        matchKeys = redisOperator.keys(codec.encode(prefix + "*"));
        Assertions.assertEquals(0, matchKeys.size());
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
                byte[] temp = codec.encode("test-mpsetex:" + RandomUtils.nextString(18));
                keyValues.add(new ExpiryKeyValue<>(temp, temp, RandomUtils.nextInt(2000000, 3000000)));
                if (keyValues.size() == capacity) {
                    redisOperator.mpsetex(keyValues);
                    keyValues.clear();
                    capacity = Math.min(10000, size - i - 1);
                }
            }
            long end = System.currentTimeMillis();
            System.out.printf("size: [%d], mpsetex-time: [%d] \n", size, end - start);
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
            keys[i] = codec.encode(prefix + RandomUtils.nextString(18));
        }

        for (byte[] key : keys) {
            redisOperator.set(key, key);
        }

        for (byte[] key : keys) {
            Assertions.assertArrayEquals(key, redisOperator.get(key));
        }

        redisOperator.del(keys);

        for (byte[] key : keys) {
            Assertions.assertNull(redisOperator.get(key));
        }
    }
    // String Command --end--

    // Hash Command --start--
    public void hget() {
        byte[] key = codec.encode("test-hget");
        byte[] field = codec.encode("hget-field-1");

        redisOperator.del(key);

        Assertions.assertNull(redisOperator.hget(key, field));

        redisOperator.hset(key, field, field);

        Assertions.assertArrayEquals(field, redisOperator.hget(key, field));

        redisOperator.del(key);
    }

    public void hset() {
        // do nothing
    }

    public void hmget() {
        byte[] key = codec.encode("test-hmget");

        int size = 8, limit = 5;
        String[] fields = createKeys(size, "test-hmget-");
        byte[][] fieldBytes = this.toKeysArray(size, fields);

        // 删除 redis 中的 key-value
        redisOperator.del(key);

        Map<byte[], byte[]> keyValues = createKeyValues(limit, fieldBytes);

        // 保存 key-value 到 redis
        redisOperator.hmset(key, keyValues);

        // 读取 redis 数据
        Map<String, String> map = this.hmgetResultMap(redisOperator.hmget(key, fieldBytes));

        // 验证读取数据是否正确
        this.validateValues(fields, map, size, limit);

        redisOperator.hdel(key, fieldBytes);

        Map<String, String> resultMap = this.hmgetResultMap(redisOperator.hmget(key, fieldBytes));
        for (int i = 0; i < size; i++) {
            Assertions.assertNull(resultMap.get(fields[i]));
        }
    }

    public void hmset() {
        int size = 16384, len = 100;
        Map<byte[], Map<byte[], byte[]>> keyFieldValues = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = "test-hmset-" + i;
            Map<byte[], byte[]> fieldValues = new HashMap<>();
            keyFieldValues.put(codec.encode(key), fieldValues);
            for (int j = 0; j < len; j++) {
                byte[] field = codec.encode("field-" + j);
                fieldValues.put(field, field);
            }
        }
        redisOperator.hmset(keyFieldValues);

        Map<byte[], List<byte[]>> keyFields = HashMap.newHashMap(size);
        keyFieldValues.forEach((k, v) -> keyFields.put(k, new ArrayList<>(v.keySet())));

        int total = size * len;
        List<KeyValue<byte[], byte[]>> keyValues = redisOperator.hmget(keyFields, total);
        Assertions.assertEquals(total, keyValues.size());
        keyValues.forEach(kv -> Assertions.assertArrayEquals(kv.getKey(), kv.getValue()));

        redisOperator.del(keyFieldValues.keySet().toArray(new byte[0][]));
    }

    public void hdel() {
        // do nothing
    }
    // Hash Command --end--

    public void keys() {
        int size = 10;
        String prefix = "test-clear:";
        byte[] matches = codec.encode(prefix + "*");

        Map<byte[], byte[]> keyValues = Maps.newHashMap(size);
        for (int i = 0; i < size; i++) {
            byte[] keyBytes = codec.encode(prefix + i);
            keyValues.put(keyBytes, keyBytes);
        }

        redisOperator.mset(keyValues);

        Map<String, String> result = getKeysResult(matches);
        for (int i = 0; i < 10; i++) {
            String key = prefix + i;
            Assertions.assertEquals(key, result.get(key));
        }

        redisOperator.clear(matches);

        result = getKeysResult(matches);
        for (int i = 0; i < 10; i++) {
            String key = prefix + i;
            Assertions.assertNull(result.get(key));
        }
    }

    private Map<String, String> getKeysResult(byte[] matches) {
        List<byte[]> keys = redisOperator.keys(matches);
        Map<String, String> result = Maps.newHashMap(keys.size());
        for (byte[] keyBytes : keys) {
            String key = codec.decode(keyBytes);
            result.put(key, key);
        }
        return result;
    }

    public void clear(String match) {
        long start = System.currentTimeMillis();
        redisOperator.clear(codec.encode(match));
        System.out.println("clear:" + (System.currentTimeMillis() - start));
    }

    private byte[][] toKeysArray(int size, String[] keys) {
        byte[][] keyBytes = new byte[size][];
        for (int i = 0; i < size; i++) {
            keyBytes[i] = codec.encode(keys[i]);
        }
        return keyBytes;
    }

    private Map<String, String> hmgetResultMap(List<KeyValue<byte[], byte[]>> keyValues) {
        Map<String, String> map = Maps.newHashMap(keyValues.size());
        keyValues.forEach(keyValue -> {
            String field = codec.decode(keyValue.getKey());
            String value = codec.decode(keyValue.getValue());
            map.put(field, value);
        });
        return map;
    }

    private void validateValues(String[] keys, Map<String, String> map, int size, int limit) {
        for (int i = 0; i < size; i++) {
            String key = keys[i];
            if (i < limit) {
                Assertions.assertEquals(key, map.get(key));
            } else {
                Assertions.assertNull(map.get(key));
            }
        }
    }

    private static String[] createKeys(int size, String prefix) {
        String[] keys = new String[size];
        for (int i = 0; i < size; i++) {
            keys[i] = prefix + RandomUtils.nextString(18);
        }
        return keys;
    }

    private static Map<byte[], byte[]> createKeyValues(int size, byte[][] keyBytes) {
        Map<byte[], byte[]> keyValues = Maps.newHashMap(size);
        for (int i = 0; i < size; i++) {
            keyValues.put(keyBytes[i], keyBytes[i]);
        }
        return keyValues;
    }

}