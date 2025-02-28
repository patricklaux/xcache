package com.igeeksky.xcache.redis.stat;

import com.igeeksky.xcache.extension.stat.CacheStatMessage;
import com.igeeksky.xcache.extension.stat.CacheStatistics;
import com.igeeksky.xredis.common.stream.StreamCodec;
import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.ByteArray;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存统计消息编解码
 * <p>
 * 字段名和字段值分别编解码，转换成键值对形式存入 map
 * <p>
 * 主要用于适配 RedisStream 数据结构，便于在可视化界面查看
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/22
 */
public class RedisCacheStatMessageCodec implements StreamCodec<byte[], byte[], CacheStatMessage> {

    private final StringCodec stringCodec;
    private final Codec<CacheStatistics> statCodec;

    private final ByteArray name;
    private final ByteArray group;
    private final ByteArray hitLoads;
    private final ByteArray missLoads;
    private final ByteArray noop;
    private final ByteArray first;
    private final ByteArray second;
    private final ByteArray third;

    public RedisCacheStatMessageCodec(Codec<CacheStatistics> statCodec, StringCodec stringCodec) {
        Assert.notNull(statCodec, "StatCodec must not be null");
        Assert.notNull(stringCodec, "StringCodec must not be null");

        this.statCodec = statCodec;
        this.stringCodec = stringCodec;
        this.name = ByteArray.wrap(this.stringCodec.encode("name"));
        this.group = ByteArray.wrap(this.stringCodec.encode("group"));
        this.hitLoads = ByteArray.wrap(this.stringCodec.encode("hitLoads"));
        this.missLoads = ByteArray.wrap(this.stringCodec.encode("missLoads"));
        this.noop = ByteArray.wrap(this.stringCodec.encode("noop"));
        this.first = ByteArray.wrap(this.stringCodec.encode("first"));
        this.second = ByteArray.wrap(this.stringCodec.encode("second"));
        this.third = ByteArray.wrap(this.stringCodec.encode("third"));
    }

    /**
     * 键编码
     *
     * @param key 键
     * @return 字节数组
     */
    public byte[] encodeKey(String key) {
        return stringCodec.encode(key);
    }

    /**
     * 键解码
     *
     * @param key 字节数组
     * @return 字符串
     */
    public String decodeKey(byte[] key) {
        return stringCodec.decode(key);
    }

    /**
     * 消息编码
     *
     * @param message 消息
     * @return 键值对形式的消息体
     */
    public Map<byte[], byte[]> encodeMsg(CacheStatMessage message) {
        Map<byte[], byte[]> body = new HashMap<>();
        body.put(this.name.getValue(), stringCodec.encode(message.getName()));
        body.put(this.group.getValue(), stringCodec.encode(message.getGroup()));
        body.put(this.hitLoads.getValue(), stringCodec.encode(String.valueOf(message.getHitLoads())));
        body.put(this.missLoads.getValue(), stringCodec.encode(String.valueOf(message.getMissLoads())));
        if (message.getNoop() != null) {
            body.put(this.noop.getValue(), statCodec.encode(message.getNoop()));
        }
        if (message.getFirst() != null) {
            body.put(this.first.getValue(), statCodec.encode(message.getFirst()));
        }
        if (message.getSecond() != null) {
            body.put(this.second.getValue(), statCodec.encode(message.getSecond()));
        }
        if (message.getThird() != null) {
            body.put(this.third.getValue(), statCodec.encode(message.getThird()));
        }
        return body;
    }

    @Override
    public CacheStatMessage decodeMsg(Map<byte[], byte[]> body) {

        Map<ByteArray, byte[]> temp = HashMap.newHashMap(body.size());
        body.forEach((k, v) -> temp.put(ByteArray.wrap(k), v));

        CacheStatMessage message = new CacheStatMessage();
        byte[] nameBytes = temp.get(name);
        if (ArrayUtils.isNotEmpty(nameBytes)) {
            message.setName(stringCodec.decode(nameBytes));
        }
        byte[] groupBytes = temp.get(group);
        if (ArrayUtils.isNotEmpty(groupBytes)) {
            message.setGroup(stringCodec.decode(groupBytes));
        }
        byte[] hitLoadsBytes = temp.get(hitLoads);
        if (ArrayUtils.isNotEmpty(hitLoadsBytes)) {
            message.setHitLoads(Long.parseLong(stringCodec.decode(hitLoadsBytes)));
        }
        byte[] missLoadsBytes = temp.get(missLoads);
        if (ArrayUtils.isNotEmpty(missLoadsBytes)) {
            message.setMissLoads(Long.parseLong(stringCodec.decode(missLoadsBytes)));
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