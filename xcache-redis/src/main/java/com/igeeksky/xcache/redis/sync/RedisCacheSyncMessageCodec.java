package com.igeeksky.xcache.redis.sync;

import com.igeeksky.xcache.extension.sync.CacheSyncMessage;
import com.igeeksky.xredis.common.stream.StreamCodec;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.ByteArray;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 缓存数据同步广播消息编解码
 * <p>
 * 字段名和字段值分别编解码，转换成键值对形式存入 map
 * <p>
 * 适配 RedisStream 数据结构，便于在可视化界面查看
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/22
 */
public class RedisCacheSyncMessageCodec implements StreamCodec<byte[], byte[], CacheSyncMessage> {

    private final StringCodec stringCodec;
    private final Codec<Set<String>> setCodec;

    private final ByteArray sid;
    private final ByteArray type;
    private final ByteArray keys;

    public RedisCacheSyncMessageCodec(Codec<Set<String>> setCodec, StringCodec stringCodec) {
        Assert.notNull(setCodec, "setCodec must not be null");
        Assert.notNull(stringCodec, "stringCodec must not be null");

        this.setCodec = setCodec;
        this.stringCodec = stringCodec;
        this.sid = ByteArray.wrap(stringCodec.encode("sid"));
        this.type = ByteArray.wrap(stringCodec.encode("type"));
        this.keys = ByteArray.wrap(stringCodec.encode("keys"));
    }

    /**
     * 编码 Redis-Stream 键
     *
     * @param key Redis-Stream 键
     * @return {@code byte[]} – 序列化数据
     */
    public byte[] encodeKey(String key) {
        return stringCodec.encode(key);
    }

    /**
     * 解码 Redis-Stream 键
     *
     * @param key 序列化数据
     * @return {@code String} – Redis-Stream 键
     */
    public String decodeKey(byte[] key) {
        return stringCodec.decode(key);
    }

    public Map<byte[], byte[]> encodeMsg(CacheSyncMessage message) {
        Map<byte[], byte[]> body = HashMap.newHashMap(4);
        body.put(sid.getValue(), stringCodec.encode(message.getSid()));
        body.put(type.getValue(), stringCodec.encode(Integer.toString(message.getType())));
        Set<String> keys = message.getKeys();
        if (CollectionUtils.isNotEmpty(keys)) {
            body.put(this.keys.getValue(), setCodec.encode(keys));
        }
        return body;
    }

    public CacheSyncMessage decodeMsg(Map<byte[], byte[]> body) {
        Map<ByteArray, byte[]> temp = HashMap.newHashMap(body.size());
        body.forEach((k, v) -> temp.put(ByteArray.wrap(k), v));

        CacheSyncMessage message = new CacheSyncMessage();
        byte[] sidBytes = temp.get(sid);
        if (ArrayUtils.isNotEmpty(sidBytes)) {
            message.setSid(stringCodec.decode(sidBytes));
        }
        byte[] typeBytes = temp.get(type);
        if (ArrayUtils.isNotEmpty(typeBytes)) {
            message.setType(Integer.parseInt(stringCodec.decode(typeBytes)));
        }
        byte[] keysBytes = temp.get(keys);
        if (ArrayUtils.isNotEmpty(keysBytes)) {
            Set<String> keys = setCodec.decode(keysBytes);
            message.setKeys(keys);
        }
        return message;
    }

}