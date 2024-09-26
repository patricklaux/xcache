package com.igeeksky.redis.jedis;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.redis.RedisScript;
import com.igeeksky.redis.ResultType;
import com.igeeksky.redis.stream.AddOptions;
import com.igeeksky.xtool.core.function.tuple.ExpiryKeyValue;
import com.igeeksky.xtool.core.function.tuple.KeyValue;
import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.lang.codec.Codec;
import redis.clients.jedis.BuilderFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.commands.JedisBinaryCommands;

import java.util.List;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-03
 */
public abstract class AbstractJedisOperator implements RedisOperator {

    private Jedis jedis;
    private final JedisBinaryCommands commands;

    public AbstractJedisOperator(JedisBinaryCommands commands) {
        this.commands = commands;
    }

    @Override
    public byte[] get(byte[] key) {
        return commands.get(key);
    }

    @Override
    public List<KeyValue<byte[], byte[]>> mget(byte[]... keys) {
        return List.of();
    }

    @Override
    public String set(byte[] key, byte[] value) {
        return "";
    }

    @Override
    public String psetex(byte[] key, long milliseconds, byte[] value) {
        return "";
    }

    @Override
    public String mset(Map<byte[], byte[]> keyValues) {
        int i = 0;
        byte[][] params = new byte[keyValues.size() * 2][];
        for (Map.Entry<byte[], byte[]> entry : keyValues.entrySet()) {
            params[i++] = entry.getKey();
            params[i++] = entry.getValue();
        }
        return commands.mset(params);
    }

    @Override
    public String mpsetex(List<ExpiryKeyValue<byte[], byte[]>> keyValues) {
        return "";
    }

    @Override
    public long del(byte[]... keys) {
        return 0;
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        return new byte[0];
    }

    @Override
    public Map<byte[], byte[]> hgetall(byte[] key) {
        return Map.of();
    }

    @Override
    public List<KeyValue<byte[], byte[]>> hmget(byte[] key, byte[]... field) {
        return List.of();
    }

    @Override
    public List<KeyValue<byte[], byte[]>> hmget(Map<byte[], List<byte[]>> keyFields, int totalSize) {
        return List.of();
    }

    @Override
    public Boolean hset(byte[] key, byte[] field, byte[] value) {
        return commands.hset(key, field, value) == 1L;
    }

    @Override
    public String hmset(byte[] key, Map<byte[], byte[]> map) {
        return commands.hmset(key, map);
    }

    @Override
    public String hmset(Map<byte[], Map<byte[], byte[]>> keyFieldValues) {
        return "";
    }

    @Override
    public long hdel(byte[] key, byte[]... fields) {
        return 0;
    }

    @Override
    public long hdel(Map<byte[], List<byte[]>> keyFields) {
        return 0;
    }

    @Override
    public List<byte[]> keys(byte[] matches) {
        return List.of();
    }

    @Override
    public long clear(byte[] matches) {
        return 0;
    }

    @Override
    public void publish(byte[] channel, byte[] message) {

    }

    @Override
    public String xadd(byte[] key, Map<byte[], byte[]> body) {
        return "";
    }

    @Override
    public String xadd(byte[] key, AddOptions args, Map<byte[], byte[]> body) {
        return "";
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T eval(RedisScript<T> script, int keyCount, byte[]... params) {
        byte[] result = (byte[]) commands.eval(script.getScript(), keyCount, params);
        if (ArrayUtils.isEmpty(result)) {
            return null;
        }

        Codec<T> codec = script.getCodec();
        if (codec != null) {
            return codec.decode(result);
        }

        ResultType resultType = script.getResultType();
        switch (resultType) {
            case BOOLEAN -> {

            }
            case STATUS -> {
                return (T) BuilderFactory.STRING.build(result);
            }
            case MULTI -> {
                // TODO result
            }
            case VALUE -> {
                return (T) result;
            }
            case INTEGER -> {
                // TODO result
            }
        }

        return (T) result;
    }

    @Override
    public <T> T eval(RedisScript<T> script, byte[][] keys, byte[]... args) {
        return null;
    }

    @Override
    public <T> T evalReadOnly(RedisScript<T> script, int keyCount, byte[]... params) {
        return null;
    }

    @Override
    public <T> T evalReadOnly(RedisScript<T> script, byte[][] keys, byte[]... args) {
        return null;
    }

    @Override
    public <T> T evalsha(RedisScript<T> script, int keyCount, byte[]... params) {
        return null;
    }

    @Override
    public <T> T evalsha(RedisScript<T> script, byte[][] keys, byte[]... args) {
        return null;
    }

    @Override
    public <T> T evalshaReadOnly(RedisScript<T> script, int keyCount, byte[]... params) {
        return null;
    }

    @Override
    public <T> T evalshaReadOnly(RedisScript<T> script, byte[][] keys, byte[]... args) {
        return null;
    }

    @Override
    public long timeMillis() {
        List<String> time = jedis.time();
        return (Long.parseLong(time.get(0)) * 1000) + (Long.parseLong(time.get(1)) / 1000);
    }

    @Override
    public void close() {

    }
}