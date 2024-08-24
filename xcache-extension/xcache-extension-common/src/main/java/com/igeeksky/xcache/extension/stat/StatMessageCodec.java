package com.igeeksky.xcache.extension.stat;


import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.lang.ByteArray;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/22
 */
public class StatMessageCodec {

    private final StringCodec codec = StringCodec.getInstance(StandardCharsets.UTF_8);
    private final Codec<CacheStatistics> statCodec;

    private final ByteArray name;
    private final ByteArray app;
    private final ByteArray hitLoads;
    private final ByteArray missLoads;
    private final ByteArray noop;
    private final ByteArray first;
    private final ByteArray second;
    private final ByteArray third;

    public StatMessageCodec(Codec<CacheStatistics> statCodec) {
        this.statCodec = statCodec;

        this.name = ByteArray.of(this.codec.encode("name"));
        this.app = ByteArray.of(this.codec.encode("app"));
        this.hitLoads = ByteArray.of(this.codec.encode("hitLoads"));
        this.missLoads = ByteArray.of(this.codec.encode("missLoads"));
        this.noop = ByteArray.of(this.codec.encode("noop"));
        this.first = ByteArray.of(this.codec.encode("first"));
        this.second = ByteArray.of(this.codec.encode("second"));
        this.third = ByteArray.of(this.codec.encode("third"));
    }

    public byte[] encode(String key) {
        return codec.encode(key);
    }

    public String decode(byte[] key) {
        return codec.decode(key);
    }

    public Map<byte[], byte[]> encodeMsg(CacheStatMessage message) {
        Map<byte[], byte[]> body = new HashMap<>();
        body.put(this.name.getValue(), codec.encode(message.getName()));
        body.put(this.app.getValue(), codec.encode(message.getApp()));
        body.put(this.hitLoads.getValue(), codec.encode(String.valueOf(message.getHitLoads())));
        body.put(this.missLoads.getValue(), codec.encode(String.valueOf(message.getMissLoads())));
        if (message.getNoop() != null) {
            body.put(this.noop.getValue(), codec.encode(message.getNoop().toString()));
        }
        if (message.getFirst() != null) {
            body.put(this.first.getValue(), codec.encode(message.getFirst().toString()));
        }
        if (message.getSecond() != null) {
            body.put(this.second.getValue(), codec.encode(message.getSecond().toString()));
        }
        if (message.getThird() != null) {
            body.put(this.third.getValue(), codec.encode(message.getThird().toString()));
        }
        return body;
    }

    public CacheStatMessage decodeMsg(Map<byte[], byte[]> body) {
        Map<ByteArray, byte[]> temp = HashMap.newHashMap(body.size());
        body.forEach((k, v) -> temp.put(ByteArray.of(k), v));

        CacheStatMessage message = new CacheStatMessage();
        byte[] nameBytes = temp.get(name);
        if (ArrayUtils.isNotEmpty(nameBytes)) {
            message.setName(codec.decode(nameBytes));
        }
        byte[] appBytes = temp.get(app);
        if (ArrayUtils.isNotEmpty(appBytes)) {
            message.setApp(codec.decode(appBytes));
        }
        byte[] hitLoadsBytes = temp.get(hitLoads);
        if (ArrayUtils.isNotEmpty(hitLoadsBytes)) {
            message.setHitLoads(Long.parseLong(codec.decode(hitLoadsBytes)));
        }
        byte[] missLoadsBytes = temp.get(missLoads);
        if (ArrayUtils.isNotEmpty(missLoadsBytes)) {
            message.setMissLoads(Long.parseLong(codec.decode(missLoadsBytes)));
        }
        byte[] noopBytes = temp.get(noop);
        if (ArrayUtils.isNotEmpty(noopBytes)) {
            message.setNoop(this.statCodec.decode(noopBytes));
        }
        byte[] firstBytes = temp.get(first);
        if (ArrayUtils.isNotEmpty(firstBytes)) {
            message.setFirst(this.statCodec.decode(firstBytes));
        }
        byte[] secondBytes = temp.get(second);
        if (ArrayUtils.isNotEmpty(secondBytes)) {
            message.setSecond(this.statCodec.decode(secondBytes));
        }
        byte[] thirdBytes = temp.get(third);
        if (ArrayUtils.isNotEmpty(thirdBytes)) {
            message.setThird(this.statCodec.decode(thirdBytes));
        }
        return message;
    }

}