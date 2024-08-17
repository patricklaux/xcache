package com.igeeksky.xcache.extension.sync;


import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.lang.ByteArray;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/22
 */
public class SyncMessageCodec {

    private final StringCodec stringCodec;
    private final Codec<Set<String>> codec;

    private final ByteArray sid;
    private final ByteArray type;
    private final ByteArray keys;

    public SyncMessageCodec(Codec<Set<String>> codec, Charset charset) {
        this.codec = codec;
        this.stringCodec = StringCodec.getInstance(charset);
        this.sid = ByteArray.of(stringCodec.encode("sid"));
        this.type = ByteArray.of(stringCodec.encode("type"));
        this.keys = ByteArray.of(stringCodec.encode("keys"));
    }

    public byte[] encode(String key) {
        return stringCodec.encode(key);
    }

    public String decode(byte[] key) {
        return stringCodec.decode(key);
    }

    public Map<byte[], byte[]> encodeMsg(CacheSyncMessage message) {
        Map<byte[], byte[]> body = HashMap.newHashMap(4);
        body.put(sid.getValue(), stringCodec.encode(message.getSid()));
        body.put(type.getValue(), stringCodec.encode(Integer.toString(message.getType())));
        Set<String> keys = message.getKeys();
        if (CollectionUtils.isNotEmpty(keys)) {
            body.put(this.keys.getValue(), codec.encode(keys));
        }
        return body;
    }

    public CacheSyncMessage decodeMsg(Map<byte[], byte[]> body) {
        Map<ByteArray, byte[]> temp = HashMap.newHashMap(body.size());
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
            Set<String> keys = codec.decode(keysBytes);
            message.setKeys(keys);
        }
        return message;
    }

}