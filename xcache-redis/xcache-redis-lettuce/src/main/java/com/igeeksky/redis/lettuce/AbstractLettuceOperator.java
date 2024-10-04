package com.igeeksky.redis.lettuce;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.redis.RedisScript;
import com.igeeksky.redis.ResultType;
import com.igeeksky.redis.stream.AddOptions;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.concurrent.Futures;
import com.igeeksky.xtool.core.function.tuple.ExpiryKeyValue;
import com.igeeksky.xtool.core.function.tuple.KeyValue;
import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.async.RedisHashAsyncCommands;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.api.sync.*;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Lettuce 客户端抽象类
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-03
 */
@SuppressWarnings("unchecked")
public sealed abstract class AbstractLettuceOperator implements RedisOperator
        permits LettuceOperator, LettuceClusterOperator {

    private static final StringCodec STRING_CODEC = StringCodec.getInstance(StandardCharsets.UTF_8);

    /**
     * 限定单次删除 或 仅获取键 的最大命令数量
     */
    private static final int KEYS_LIMIT = 20000;

    /**
     * 限定单次查询值 或 保存值 的最大命令数量
     */
    private static final int VALUES_LIMIT = 10000;

    /**
     * 限定提交批处理命令后单次等待的最大超时
     */
    private final long timeout;

    private final StatefulConnection<byte[], byte[]> connection;

    private final RedisStringCommands<byte[], byte[]> stringCommands;
    private final RedisHashCommands<byte[], byte[]> hashCommands;
    private final RedisKeyCommands<byte[], byte[]> keyCommands;
    private final BaseRedisCommands<byte[], byte[]> baseCommands;
    private final RedisServerCommands<byte[], byte[]> serverCommands;
    private final RedisScriptingCommands<byte[], byte[]> scriptingCommands;
    private final RedisStreamCommands<byte[], byte[]> streamCommands;

    private final StatefulConnection<byte[], byte[]> batchConnection;
    private final RedisHashAsyncCommands<byte[], byte[]> batchHashCommands;
    private final RedisStringAsyncCommands<byte[], byte[]> batchStringCommands;

    /**
     * 构造函数（用于非集群）
     *
     * @param timeout         超时时间（毫秒）
     * @param connection      连接对象（执行单个命令，自动提交）
     * @param batchConnection 连接对象（批量执行命令，不自动提交）
     */
    public AbstractLettuceOperator(long timeout, StatefulRedisConnection<byte[], byte[]> connection, StatefulRedisConnection<byte[], byte[]> batchConnection) {
        this.timeout = timeout;

        this.connection = connection;
        RedisCommands<byte[], byte[]> sync = connection.sync();
        this.stringCommands = sync;
        this.hashCommands = sync;
        this.keyCommands = sync;
        this.baseCommands = sync;
        this.serverCommands = sync;
        this.scriptingCommands = sync;
        this.streamCommands = sync;

        this.batchConnection = batchConnection;
        RedisAsyncCommands<byte[], byte[]> async = batchConnection.async();
        this.batchHashCommands = async;
        this.batchStringCommands = async;
    }

    /**
     * 构造函数（用于集群）
     *
     * @param timeout         超时时间（毫秒）
     * @param connection      连接对象（执行单个命令，自动提交）
     * @param batchConnection 连接对象（批量执行命令，不自动提交）
     */
    public AbstractLettuceOperator(long timeout, StatefulRedisClusterConnection<byte[], byte[]> connection, StatefulRedisClusterConnection<byte[], byte[]> batchConnection) {

        this.timeout = timeout;

        this.connection = connection;
        RedisAdvancedClusterCommands<byte[], byte[]> sync = connection.sync();
        this.stringCommands = sync;
        this.hashCommands = sync;
        this.keyCommands = sync;
        this.baseCommands = sync;
        this.serverCommands = sync;
        this.scriptingCommands = sync;
        this.streamCommands = sync;

        this.batchConnection = batchConnection;
        RedisAdvancedClusterAsyncCommands<byte[], byte[]> async = batchConnection.async();
        this.batchHashCommands = async;
        this.batchStringCommands = async;
    }

    @Override
    public byte[] get(byte[] key) {
        return stringCommands.get(key);
    }

    @Override
    public List<KeyValue<byte[], byte[]>> mget(byte[]... keys) {
        int size = keys.length;

        // 当数据量低于阈值，直接查询（小于等于限定数量）
        List<KeyValue<byte[], byte[]>> result = new ArrayList<>(size);
        if (size <= VALUES_LIMIT) {
            addToResult(stringCommands.mget(keys), result);
            return result;
        }

        // 当数据量超过阈值，分批查询
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
    public String set(byte[] key, byte[] value) {
        return stringCommands.set(key, value);
    }

    @Override
    public String psetex(byte[] key, long milliseconds, byte[] value) {
        return stringCommands.psetex(key, milliseconds, value);
    }

    @Override
    public String mset(Map<byte[], byte[]> keyValues) {
        int size = keyValues.size();

        if (size <= VALUES_LIMIT) {
            return stringCommands.mset(keyValues);
        }

        int i = 0;
        Map<byte[], byte[]> subKeyValues = Maps.newHashMap(VALUES_LIMIT);
        for (Map.Entry<byte[], byte[]> entry : keyValues.entrySet()) {
            i++;
            subKeyValues.put(entry.getKey(), entry.getValue());
            if (subKeyValues.size() == VALUES_LIMIT || i == size) {
                String result = stringCommands.mset(subKeyValues);
                if (!Objects.equals(OK, result)) {
                    return result;
                }
                subKeyValues.clear();
            }
        }
        return OK;
    }

    @Override
    public String mpsetex(List<ExpiryKeyValue<byte[], byte[]>> keyValues) {
        int i = 0, j = 0;
        int size = keyValues.size();
        int capacity = Math.min(VALUES_LIMIT, size);

        RedisFuture<String>[] futures = new RedisFuture[capacity];
        for (ExpiryKeyValue<byte[], byte[]> kv : keyValues) {
            i++;
            futures[j++] = batchStringCommands.psetex(kv.getKey(), kv.getTtl(), kv.getValue());
            if (j == capacity) {
                batchConnection.flushCommands();
                awaitAll(futures);
                int remaining = size - i;
                if (remaining > 0 && remaining < capacity) {
                    capacity = remaining;
                    futures = new RedisFuture[capacity];
                }
                j = 0;
            }
        }

        awaitAll(futures);

        for (RedisFuture<String> future : futures) {
            String result = future.resultNow();
            if (!Objects.equals(OK, result)) {
                return result;
            }
        }
        return OK;
    }


    @Override
    public long del(byte[]... keys) {
        int size = keys.length;

        // 当数据量低于阈值，直接删除（小于等于限定数量）
        if (size <= KEYS_LIMIT) {
            Long result = keyCommands.del(keys);
            if (result != null) {
                return result;
            }
            return 0L;
        }

        // 当数据量超过阈值，分批删除
        long num = 0;
        int capacity = KEYS_LIMIT;
        byte[][] subKeys = new byte[capacity][];
        for (int i = 0, j = 0; i < size; ) {
            subKeys[j++] = keys[i++];
            if (j == capacity) {
                Long result = keyCommands.del(subKeys);
                num += (result != null) ? result : 0;
                int remaining = size - i;
                if (remaining > 0 && remaining < capacity) {
                    subKeys = new byte[capacity = remaining][];
                }
                j = 0;
            }
        }
        return num;
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        return hashCommands.hget(key, field);
    }

    @Override
    public Map<byte[], byte[]> hgetall(byte[] key) {
        return hashCommands.hgetall(key);
    }

    @Override
    public List<KeyValue<byte[], byte[]>> hmget(byte[] key, byte[]... fields) {
        int size = fields.length;

        // 当数据量低于阈值，直接查询（小于等于限定数量）
        List<KeyValue<byte[], byte[]>> result = new ArrayList<>(size);
        if (size <= VALUES_LIMIT) {
            addToResult(hashCommands.hmget(key, fields), result);
            return result;
        }

        // 当数据量超过阈值，分批查询
        int capacity = VALUES_LIMIT;
        byte[][] subFields = new byte[capacity][];
        for (int i = 0, j = 0; i < size; ) {
            subFields[j++] = fields[i++];
            if (j == capacity) {
                addToResult(hashCommands.hmget(key, subFields), result);
                int remaining = size - i;
                if (remaining > 0 && remaining < capacity) {
                    subFields = new byte[capacity = remaining][];
                }
                j = 0;
            }
        }

        return result;
    }

    @Override
    public List<KeyValue<byte[], byte[]>> hmget(Map<byte[], List<byte[]>> keyFields, int totalSize) {
        int i = 0, count = 0;
        RedisFuture<List<io.lettuce.core.KeyValue<byte[], byte[]>>>[] futures = new RedisFuture[keyFields.size()];

        for (Map.Entry<byte[], List<byte[]>> entry : keyFields.entrySet()) {
            byte[] key = entry.getKey();
            List<byte[]> fields = entry.getValue();
            if (CollectionUtils.isNotEmpty(fields)) {
                byte[][] array = fields.toArray(new byte[fields.size()][]);
                futures[i++] = batchHashCommands.hmget(key, array);
                if ((count += array.length) >= VALUES_LIMIT) {
                    batchConnection.flushCommands();
                    count = 0;
                }
            }
        }
        batchConnection.flushCommands();

        awaitAll(futures);

        List<KeyValue<byte[], byte[]>> result = new ArrayList<>(totalSize);
        for (RedisFuture<List<io.lettuce.core.KeyValue<byte[], byte[]>>> future : futures) {
            addToResult(future.resultNow(), result);
        }
        return result;
    }

    @Override
    public Boolean hset(byte[] key, byte[] field, byte[] value) {
        return hashCommands.hset(key, field, value);
    }

    @Override
    public String hmset(byte[] key, Map<byte[], byte[]> keyValues) {
        int size = keyValues.size();

        if (size <= VALUES_LIMIT) {
            return hashCommands.hmset(key, keyValues);
        }

        int i = 0;
        Map<byte[], byte[]> subKeyValues = Maps.newHashMap(VALUES_LIMIT);
        for (Map.Entry<byte[], byte[]> entry : keyValues.entrySet()) {
            i++;
            subKeyValues.put(entry.getKey(), entry.getValue());
            if (subKeyValues.size() == VALUES_LIMIT || i == size) {
                String result = hashCommands.hmset(key, subKeyValues);
                if (!Objects.equals(OK, result)) {
                    return result;
                }
                subKeyValues.clear();
            }
        }
        return OK;
    }

    @Override
    public String hmset(Map<byte[], Map<byte[], byte[]>> keyFieldValues) {
        int i = 0, count = 0, size = keyFieldValues.size();
        RedisFuture<String>[] futures = new RedisFuture[size];

        for (Map.Entry<byte[], Map<byte[], byte[]>> entry : keyFieldValues.entrySet()) {
            byte[] key = entry.getKey();
            Map<byte[], byte[]> fieldValues = entry.getValue();
            if (Maps.isNotEmpty(fieldValues)) {
                futures[i++] = batchHashCommands.hmset(key, fieldValues);

                if ((count += fieldValues.size()) >= VALUES_LIMIT) {
                    batchConnection.flushCommands();
                    count = 0;
                }
            }
        }
        batchConnection.flushCommands();

        awaitAll(futures);

        for (RedisFuture<String> future : futures) {
            if (future != null) {
                String result = future.resultNow();
                if (!Objects.equals(OK, result)) {
                    return result;
                }
            }
        }
        return OK;
    }

    @Override
    public long hdel(byte[] key, byte[]... fields) {
        int size = fields.length;

        // 当数据量低于阈值，直接删除（小于等于限定数量）
        if (size <= KEYS_LIMIT) {
            return hashCommands.hdel(key, fields);
        }

        // 当数据量超过阈值，分批删除
        int capacity = KEYS_LIMIT;
        // 用于统计删除数量
        long num = 0L;

        byte[][] subFields = new byte[capacity][];
        for (int i = 0, j = 0; i < size; ) {
            subFields[j++] = fields[i++];
            if (j == capacity) {
                Long result = hashCommands.hdel(key, subFields);
                num += (result != null) ? result : 0;
                int remaining = size - i;
                if (remaining > 0 && remaining < capacity) {
                    subFields = new byte[capacity = remaining][];
                }
                j = 0;
            }
        }
        return num;
    }

    @Override
    public long hdel(Map<byte[], List<byte[]>> keyFields) {
        int i = 0, count = 0, size = keyFields.size();
        RedisFuture<Long>[] futures = new RedisFuture[size];

        for (Map.Entry<byte[], List<byte[]>> entry : keyFields.entrySet()) {
            byte[] key = entry.getKey();
            List<byte[]> fields = entry.getValue();
            if (CollectionUtils.isNotEmpty(fields)) {
                byte[][] array = fields.toArray(new byte[fields.size()][]);
                futures[i++] = batchHashCommands.hdel(key, array);

                if ((count += array.length) >= VALUES_LIMIT) {
                    batchConnection.flushCommands();
                    count = 0;
                }
            }
        }
        batchConnection.flushCommands();

        awaitAll(futures);

        long num = 0L;
        for (RedisFuture<Long> future : futures) {
            if (future != null) {
                Long result = future.resultNow();
                if (result != null) {
                    num += result;
                }
            }
        }
        return num;
    }

    @Override
    public long clear(byte[] matches) {
        long num = 0;
        ScanCursor cursor = ScanCursor.INITIAL;
        ScanArgs args = ScanArgs.Builder.matches(matches).limit(KEYS_LIMIT);
        while (!cursor.isFinished()) {
            KeyScanCursor<byte[]> keyScanCursor = keyCommands.scan(cursor, args);
            List<byte[]> keys = keyScanCursor.getKeys();
            if (!keys.isEmpty()) {
                Long result = keyCommands.del(keys.toArray(new byte[keys.size()][]));
                if (result != null) {
                    num += result;
                }
            }
            cursor = keyScanCursor;
        }

        List<byte[]> keys = keyCommands.keys(matches);
        if (!keys.isEmpty()) {
            Long result = keyCommands.del(keys.toArray(new byte[keys.size()][]));
            if (result != null) {
                num += result;
            }
        }
        return num;
    }

    @Override
    public List<byte[]> keys(byte[] matches) {
        return keyCommands.keys(matches);
    }

    @Override
    public long publish(byte[] channel, byte[] message) {
        Long received = baseCommands.publish(channel, message);
        return (received == null) ? 0L : received;
    }

    @Override
    public String xadd(byte[] key, Map<byte[], byte[]> body) {
        return streamCommands.xadd(key, body);
    }

    @Override
    public String xadd(byte[] key, AddOptions options, Map<byte[], byte[]> body) {
        XAddArgs args = LettuceHelper.convert(options);
        if (args == null) {
            return streamCommands.xadd(key, body);
        }
        return streamCommands.xadd(key, args, body);
    }

    @Override
    public List<Boolean> scriptExists(String... digests) {
        return scriptingCommands.scriptExists(digests);
    }

    @Override
    public String scriptLoad(RedisScript<?> script) {
        String sha1 = scriptingCommands.scriptLoad(script.getScript());
        if (sha1 != null) {
            script.setSha1(sha1);
        }
        return sha1;
    }

    @Override
    public <T> T eval(RedisScript<T> script, int keyCount, byte[]... params) {
        if (params == null || keyCount == params.length) {
            return this.eval(script, params);
        }

        byte[][] keys = Arrays.copyOf(params, keyCount);
        byte[][] args = Arrays.copyOfRange(params, keyCount, params.length);
        return this.eval(script, keys, args);
    }

    @Override
    public <T> T eval(RedisScript<T> script, byte[][] keys, byte[]... args) {
        ScriptOutputType type = getScriptOutputType(script.getResultType());
        Object result;
        if (ArrayUtils.isEmpty(args)) {
            result = scriptingCommands.eval(script.getScript(), type, keys);
        } else {
            result = scriptingCommands.eval(script.getScript(), type, keys, args);
        }
        return processEvalResult(result, type, script.getCodec());
    }

    @Override
    public <T> T evalReadOnly(RedisScript<T> script, int keyCount, byte[]... params) {
        if (keyCount == params.length) {
            return this.evalReadOnly(script, params);
        }

        byte[][] keys = Arrays.copyOf(params, keyCount);
        byte[][] args = Arrays.copyOfRange(params, keyCount, params.length);
        return this.evalReadOnly(script, keys, args);
    }

    @Override
    public <T> T evalReadOnly(RedisScript<T> script, byte[][] keys, byte[]... args) {
        ScriptOutputType type = getScriptOutputType(script.getResultType());
        Object result = scriptingCommands.evalReadOnly(script.getScript(), type, keys, args);
        return processEvalResult(result, type, script.getCodec());
    }

    @Override
    public <T> T evalsha(RedisScript<T> script, int keyCount, byte[]... params) {
        if (keyCount == params.length) {
            return this.evalsha(script, params);
        }

        byte[][] keys = Arrays.copyOf(params, keyCount);
        byte[][] args = Arrays.copyOfRange(params, keyCount, params.length);
        return this.evalsha(script, keys, args);
    }

    @Override
    public <T> T evalsha(RedisScript<T> script, byte[][] keys, byte[]... args) {
        ScriptOutputType type = getScriptOutputType(script.getResultType());
        Object result;
        if (ArrayUtils.isEmpty(args)) {
            result = scriptingCommands.evalsha(script.getSha1(), type, keys);
        } else {
            result = scriptingCommands.evalsha(script.getSha1(), type, keys, args);
        }
        return processEvalResult(result, type, script.getCodec());
    }

    @Override
    public <T> T evalshaReadOnly(RedisScript<T> script, int keyCount, byte[]... params) {
        if (keyCount == params.length) {
            return this.evalshaReadOnly(script, params);
        }

        byte[][] keys = Arrays.copyOf(params, keyCount);
        byte[][] args = Arrays.copyOfRange(params, keyCount, params.length);
        return this.evalshaReadOnly(script, keys, args);
    }

    @Override
    public <T> T evalshaReadOnly(RedisScript<T> script, byte[][] keys, byte[]... args) {
        ScriptOutputType type = getScriptOutputType(script.getResultType());
        Object result = scriptingCommands.evalshaReadOnly(script.getSha1(), type, keys, args);
        return processEvalResult(result, type, script.getCodec());
    }

    @Override
    public long timeMillis() {
        List<byte[]> time = serverCommands.time();
        long seconds = Long.parseLong(STRING_CODEC.decode(time.get(0)));
        long micros = Long.parseLong(STRING_CODEC.decode(time.get(1)));
        return (seconds * 1000) + (micros / 1000);
    }

    @Override
    public boolean isOpen() {
        return this.connection.isOpen() && this.batchConnection.isOpen();
    }

    @Override
    public void close() {
        Futures.awaitAll(new Future[]{this.connection.closeAsync(), this.batchConnection.closeAsync()});
    }

    private static ScriptOutputType getScriptOutputType(ResultType resultType) {
        return switch (resultType) {
            case BOOLEAN -> ScriptOutputType.BOOLEAN;
            case INTEGER -> ScriptOutputType.INTEGER;
            case VALUE -> ScriptOutputType.VALUE;
            case MULTI -> ScriptOutputType.MULTI;
            case STATUS -> ScriptOutputType.STATUS;
        };
    }

    private static <T> T processEvalResult(Object result, ScriptOutputType type, Codec<T> mapping) {
        if (result == null) {
            return null;
        }
        if (ScriptOutputType.VALUE == type) {
            byte[] array = (byte[]) result;
            if (array.length == 0) {
                return null;
            }
            if (mapping != null) {
                return mapping.decode(array);
            }
        }
        return (T) result;
    }

    private static void addToResult(List<io.lettuce.core.KeyValue<byte[], byte[]>> keyValues, List<KeyValue<byte[], byte[]>> result) {
        if (CollectionUtils.isEmpty(keyValues)) {
            return;
        }
        for (io.lettuce.core.KeyValue<byte[], byte[]> keyValue : keyValues) {
            if (keyValue.hasValue()) {
                result.add(new KeyValue<>(keyValue.getKey(), keyValue.getValue()));
            }
        }
    }

    private void awaitAll(Future<?>[] futures) {
        int length = futures.length;
        int index = Futures.awaitAll(timeout, TimeUnit.MILLISECONDS, 0, futures);
        if (index < length) {
            throw new RedisCommandTimeoutException("Timed out after " + timeout + " millis, it's waiting for " + futures.length + " batch-cmd futures to complete");
        }
    }

}