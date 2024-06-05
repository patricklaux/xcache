package com.igeeksky.xcache.redis.jedis;

import com.igeeksky.xcache.common.KeyValue;
import com.igeeksky.xcache.redis.RedisConnection;
import com.igeeksky.xcache.redis.RedisOperationException;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import redis.clients.jedis.commands.JedisBinaryCommands;

import java.util.*;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-03
 */
public abstract class AbstractJedisConnection implements RedisConnection {

    private final JedisBinaryCommands jedisBinaryCommands;

    public AbstractJedisConnection(JedisBinaryCommands jedisBinaryCommands) {
        this.jedisBinaryCommands = jedisBinaryCommands;
    }

    @Override
    public byte[] get(byte[] key) {
        return jedisBinaryCommands.get(key);
    }

    @Override
    public List<KeyValue<byte[], byte[]>> mget(byte[]... keys) {
        List<byte[]> values = jedisBinaryCommands.mget(keys);
        return getKeyValues(values, keys);
    }

    @Override
    public void set(byte[] key, byte[] value) {
        String result = jedisBinaryCommands.set(key, value);
        isSetSuccess(key, value, result);
    }

    @Override
    public void psetex(byte[] key, long milliseconds, byte[] value) {
        String result = jedisBinaryCommands.psetex(key, milliseconds, value);
        isSetSuccess(key, value, result);
    }

    @Override
    public void mset(Map<byte[], byte[]> keyValues) {
        byte[][] keysValues = new byte[keyValues.size() * 2][];
        int i = 0;
        Set<Map.Entry<byte[], byte[]>> entries = keyValues.entrySet();
        for (Map.Entry<byte[], byte[]> entry : entries) {
            keysValues[i++] = entry.getKey();
            keysValues[i++] = entry.getValue();
        }

        String result = jedisBinaryCommands.mset(keysValues);
        if (!Objects.equals(OK, result)) {
            throw new RedisOperationException("redis mset error.");
        }
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        return jedisBinaryCommands.hget(key, field);
    }

    @Override
    public List<KeyValue<byte[], byte[]>> hmget(byte[] key, byte[]... fields) {
        List<byte[]> values = jedisBinaryCommands.hmget(key, fields);
        return getKeyValues(values, fields);
    }

    private List<KeyValue<byte[], byte[]>> getKeyValues(List<byte[]> values, byte[][] fields) {
        int len = fields.length;
        if (CollectionUtils.isNotEmpty(values)) {
            List<KeyValue<byte[], byte[]>> result = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                result.add(new KeyValue<>(fields[i], values.get(i)));
            }
            return result;
        }

        return Collections.emptyList();
    }

    @Override
    public void hset(byte[] key, byte[] field, byte[] value) {
        jedisBinaryCommands.hset(key, field, value);
    }

    @Override
    public void hmset(byte[] key, Map<byte[], byte[]> map) {
        String result = jedisBinaryCommands.hmset(key, map);
        if (!Objects.equals(OK, result)) {
            String errMsg = String.format("redis hmset error. key:[%s]", new String(key));
            throw new RedisOperationException(errMsg);
        }
    }

    @Override
    public void hdel(byte[] key, byte[]... fields) {
        jedisBinaryCommands.hdel(key, fields);
    }

    protected void isSetSuccess(byte[] key, byte[] value, String result) {
        if (!Objects.equals(OK, result)) {
            String errMsg = String.format("redis set error. key:[%s] value:[%s]", new String(key), new String(value));
            throw new RedisOperationException(errMsg);
        }
    }

}