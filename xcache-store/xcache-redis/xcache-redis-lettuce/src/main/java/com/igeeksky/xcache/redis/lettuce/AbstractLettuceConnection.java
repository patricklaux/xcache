package com.igeeksky.xcache.redis.lettuce;

import com.igeeksky.xcache.common.ExpiryKeyValue;
import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.redis.RedisConnection;
import com.igeeksky.xcache.redis.RedisOperationException;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.io.IOUtils;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.api.sync.RedisHashCommands;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.lettuce.core.api.sync.RedisStringCommands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-03
 */
public abstract class AbstractLettuceConnection implements RedisConnection {

    /**
     * 限定提交批处理命令后单次等待的最大超时
     */
    private static final long TIMEOUT = 50L;

    /**
     * 限定单次删除 或 仅获取键 的最大命令数量
     */
    private static final int KEYS_LIMIT = 20000;

    /**
     * 限定单次查询值 或 保存值 的最大命令数量
     */
    private static final int VALUES_LIMIT = 10000;

    private final StatefulConnection<byte[], byte[]> connection;

    private final RedisStringCommands<byte[], byte[]> stringCommands;
    private final RedisHashCommands<byte[], byte[]> hashCommands;
    private final RedisKeyCommands<byte[], byte[]> keyCommands;

    private final StatefulConnection<byte[], byte[]> bashConnection;
    private final RedisStringAsyncCommands<byte[], byte[]> bashCommands;

    public AbstractLettuceConnection(StatefulConnection<byte[], byte[]> connection, RedisStringCommands<byte[], byte[]> stringCommands, RedisHashCommands<byte[], byte[]> hashCommands, RedisKeyCommands<byte[], byte[]> keyCommands, StatefulConnection<byte[], byte[]> bashConnection, RedisStringAsyncCommands<byte[], byte[]> bashCommands) {
        this.connection = connection;
        this.stringCommands = stringCommands;
        this.hashCommands = hashCommands;
        this.keyCommands = keyCommands;
        this.bashConnection = bashConnection;
        this.bashCommands = bashCommands;
    }

    @Override
    public byte[] get(byte[] key) {
        return stringCommands.get(key);
    }

    @Override
    public List<KeyValue<byte[], byte[]>> mget(byte[]... keys) {
        int size = keys.length;

        // 当数据量较少时，直接查询（小于等于限定数量）
        List<KeyValue<byte[], byte[]>> result = new ArrayList<>(size);
        if (size <= VALUES_LIMIT) {
            addToResult(stringCommands.mget(keys), result);
            return result;
        }

        // 当数据量过多时，分批查询
        int capacity = VALUES_LIMIT;
        byte[][] subKeys = new byte[capacity][];
        for (int i = 0, j = 0; i < size; ) {
            subKeys[j++] = keys[i++];
            if (j == capacity) {
                addToResult(stringCommands.mget(subKeys), result);
                int remaining = size - i;
                if (remaining > 0 && remaining < capacity) {
                    capacity = remaining;
                    subKeys = new byte[capacity][];
                }
                j = 0;
            }
        }

        return result;
    }

    @Override
    public void set(byte[] key, byte[] value) {
        String result = stringCommands.set(key, value);
        checkResult(key, value, "set", result);
    }

    @Override
    public void psetex(byte[] key, long milliseconds, byte[] value) {
        String result = stringCommands.psetex(key, milliseconds, value);
        checkResult(key, value, "psetex", result);
    }

    @Override
    public void mset(Map<byte[], byte[]> keyValues) {
        int size = keyValues.size();

        if (size <= VALUES_LIMIT) {
            checkResult(stringCommands.mset(keyValues));
            return;
        }

        int i = 0;
        Map<byte[], byte[]> subKeyValues = Maps.newHashMap(VALUES_LIMIT);
        for (Map.Entry<byte[], byte[]> entry : keyValues.entrySet()) {
            i++;
            subKeyValues.put(entry.getKey(), entry.getValue());
            if (subKeyValues.size() == VALUES_LIMIT || i == size) {
                checkResult(stringCommands.mset(subKeyValues));
                subKeyValues.clear();
            }
        }
    }

    @Override
    public void mpsetex(List<ExpiryKeyValue<byte[], byte[]>> keyValues) {
        int i = 0, j = 0;
        int size = keyValues.size();
        int capacity = Math.min(VALUES_LIMIT, size);

        RedisFuture<?>[] futures = new RedisFuture[capacity];
        for (ExpiryKeyValue<byte[], byte[]> kv : keyValues) {
            i++;
            futures[j++] = bashCommands.psetex(kv.getKey(), kv.getTtl(), kv.getValue());
            if (j == capacity) {
                bashConnection.flushCommands();
                int index = Futures.awaitAll(TIMEOUT, TimeUnit.MILLISECONDS, 0, futures);
                while (index < capacity) {
                    index = Futures.awaitAll(TIMEOUT, TimeUnit.MILLISECONDS, index, futures);
                }
                int remaining = size - i;
                if (remaining > 0 && remaining < capacity) {
                    capacity = remaining;
                    futures = new RedisFuture[capacity];
                }
                j = 0;
            }
        }
    }

    @Override
    public void del(byte[]... keys) {
        int size = keys.length;

        // 当数据量较少时，直接删除（小于等于限定数量）
        if (size <= KEYS_LIMIT) {
            keyCommands.del(keys);
            return;
        }

        // 当数据量过多时，分批删除
        int capacity = KEYS_LIMIT;
        byte[][] subKeys = new byte[capacity][];
        for (int i = 0, j = 0; i < size; ) {
            subKeys[j++] = keys[i++];
            if (j == capacity) {
                keyCommands.del(subKeys);
                int remaining = size - i;
                if (remaining > 0 && remaining < capacity) {
                    capacity = remaining;
                    subKeys = new byte[capacity][];
                }
                j = 0;
            }
        }
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        return hashCommands.hget(key, field);
    }

    @Override
    public List<KeyValue<byte[], byte[]>> hmget(byte[] key, byte[]... fields) {
        int size = fields.length;

        // 当数据量较少时，直接查询（小于等于限定数量）
        List<KeyValue<byte[], byte[]>> result = new ArrayList<>(size);
        if (size <= VALUES_LIMIT) {
            addToResult(hashCommands.hmget(key, fields), result);
            return result;
        }

        // 当数据量过多时，分批查询
        int capacity = VALUES_LIMIT;
        byte[][] subFields = new byte[capacity][];
        for (int i = 0, j = 0; i < size; ) {
            subFields[j++] = fields[i++];
            if (j == capacity) {
                addToResult(hashCommands.hmget(key, subFields), result);
                int remaining = size - i;
                if (remaining > 0 && remaining < capacity) {
                    capacity = remaining;
                    subFields = new byte[capacity][];
                }
                j = 0;
            }
        }

        return result;
    }


    @Override
    public void hset(byte[] key, byte[] field, byte[] value) {
        hashCommands.hset(key, field, value);
    }

    @Override
    public void hmset(byte[] key, Map<byte[], byte[]> keyValues) {
        int size = keyValues.size();

        if (size <= VALUES_LIMIT) {
            checkResult(key, null, "hmset", hashCommands.hmset(key, keyValues));
            return;
        }

        int i = 0;
        Map<byte[], byte[]> subKeyValues = Maps.newHashMap(VALUES_LIMIT);
        for (Map.Entry<byte[], byte[]> entry : keyValues.entrySet()) {
            i++;
            subKeyValues.put(entry.getKey(), entry.getValue());
            if (subKeyValues.size() == VALUES_LIMIT || i == size) {
                checkResult(key, null, "hmset", hashCommands.hmset(key, subKeyValues));
                subKeyValues.clear();
            }
        }
    }

    @Override
    public void hdel(byte[] key, byte[]... fields) {
        int size = fields.length;

        // 当数据量较少时，直接删除（小于等于限定数量）
        if (size <= KEYS_LIMIT) {
            hashCommands.hdel(key, fields);
            return;
        }

        // 当数据量过多时，分批删除
        int capacity = KEYS_LIMIT;
        byte[][] subFields = new byte[capacity][];
        for (int i = 0, j = 0; i < size; ) {
            subFields[j++] = fields[i++];
            if (j == capacity) {
                hashCommands.hdel(key, subFields);
                int remaining = size - i;
                if (remaining > 0 && remaining < capacity) {
                    capacity = remaining;
                    subFields = new byte[capacity][];
                }
                j = 0;
            }
        }
    }

    @Override
    public void clear(byte[] matches) {
        ScanCursor cursor = ScanCursor.INITIAL;
        ScanArgs args = ScanArgs.Builder.matches(matches).limit(KEYS_LIMIT);
        while (!cursor.isFinished()) {
            KeyScanCursor<byte[]> keyScanCursor = keyCommands.scan(cursor, args);
            List<byte[]> keys = keyScanCursor.getKeys();
            if (!keys.isEmpty()) {
                keyCommands.del(keys.toArray(new byte[keys.size()][]));
            }
            cursor = keyScanCursor;
        }

        List<byte[]> keys = keyCommands.keys(matches);
        if (!keys.isEmpty()) {
            keyCommands.del(keys.toArray(new byte[keys.size()][]));
        }
    }

    @Override
    public List<byte[]> keys(byte[] matches) {
        return keyCommands.keys(matches);
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(this.connection, this.bashConnection);
    }

    private static void addToResult(List<io.lettuce.core.KeyValue<byte[], byte[]>> keyValues, List<KeyValue<byte[], byte[]>> result) {
        for (io.lettuce.core.KeyValue<byte[], byte[]> keyValue : keyValues) {
            if (keyValue.hasValue()) {
                result.add(new KeyValue<>(keyValue.getKey(), keyValue.getValue()));
            }
        }
    }

    private static void checkResult(String result) {
        if (!Objects.equals(OK, result)) {
            throw new RedisOperationException("redis mset error.");
        }
    }

    private static void checkResult(byte[] key, byte[] value, String cmd, String result) {
        if (!Objects.equals(OK, result)) {
            String msg = String.format("redis [%s] error. key:[%s] value:[%s]", cmd, new String(key), new String(value));
            throw new RedisOperationException(msg);
        }
    }

}