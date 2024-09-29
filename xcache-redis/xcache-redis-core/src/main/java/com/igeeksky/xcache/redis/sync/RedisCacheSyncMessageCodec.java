package com.igeeksky.xcache.redis.sync;


import com.igeeksky.xcache.extension.sync.CacheSyncMessage;
import com.igeeksky.xcache.redis.StreamMessageCodec;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.lang.ByteArray;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.util.Map;
import java.util.Set;

/**
 * 缓存数据同步广播消息编解码
 * <p>
 * 字段名和字段值分别编解码，转换成键值对形式存入 map
 * <p>
 * 主要用于适配 RedisStream 数据结构，便于在可视化界面查看
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/22
 */
public class RedisCacheSyncMessageCodec implements StreamMessageCodec<CacheSyncMessage> {

    private final StringCodec stringCodec;
    private final Codec<Set<String>> setCodec;

    private final ByteArray sid;
    private final ByteArray type;
    private final ByteArray keys;

    public RedisCacheSyncMessageCodec(Codec<Set<String>> setCodec, StringCodec stringCodec) {
        this.setCodec = setCodec;
        this.stringCodec = stringCodec;
        this.sid = ByteArray.of(stringCodec.encode("sid"));
        this.type = ByteArray.of(stringCodec.encode("type"));
        this.keys = ByteArray.of(stringCodec.encode("keys"));
    }

    public byte[] encodeKey(String key) {
        return stringCodec.encode(key);
    }

    public String decodeKey(byte[] key) {
        return stringCodec.decode(key);
    }

    public Map<byte[], byte[]> encodeMsg(CacheSyncMessage message) {
        Map<byte[], byte[]> body = Maps.newHashMap(4);
        body.put(sid.getValue(), stringCodec.encode(message.getSid()));
        body.put(type.getValue(), stringCodec.encode(Integer.toString(message.getType())));
        Set<String> keys = message.getKeys();
        if (CollectionUtils.isNotEmpty(keys)) {
            body.put(this.keys.getValue(), setCodec.encode(keys));
        }
        return body;
    }

    public CacheSyncMessage decodeMsg(Map<byte[], byte[]> body) {
        Map<ByteArray, byte[]> temp = Maps.newHashMap(body.size());
        body.forEach((k, v) -> temp.put(ByteArray.of(k), v));

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