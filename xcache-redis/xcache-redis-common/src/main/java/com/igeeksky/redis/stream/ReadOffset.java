package com.igeeksky.redis.stream;

import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.lang.Assert;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/20
 */
public class ReadOffset {

    public static final String first = "0-0";
    public static final String latest = "$";
    public static final String lastConsumed = ">";

    private final byte[] key;

    private final String offset;

    private ReadOffset(byte[] key, String offset) {
        Assert.isTrue(ArrayUtils.isNotEmpty(key), "key must not be null or empty");
        Assert.hasText(offset, "offset must not be null or empty");
        this.key = key;
        this.offset = offset;
    }

    public byte[] getKey() {
        return key;
    }

    public String getOffset() {
        return offset;
    }

    public static ReadOffset from(byte[] key, String offset) {
        return new ReadOffset(key, offset);
    }

    /**
     * 从 Stream Key 的第一个消息开始读取
     *
     * @param key Stream name
     * @return ReadOffset 消息的偏移量
     */
    public static ReadOffset first(byte[] key) {
        return new ReadOffset(key, first);
    }

    /**
     * 从 Stream Key 中读取所有新到达的消息
     *
     * @param key Stream name
     * @return ReadOffset 消息的偏移量
     */
    public static ReadOffset latest(byte[] key) {
        return new ReadOffset(key, latest);
    }

    /**
     * 读取所有新到达的且 ID 大于消费者组的最后消费位置的消息
     *
     * @param key Stream name
     * @return ReadOffset 消息的偏移量
     */
    public static ReadOffset lastConsumed(byte[] key) {
        return new ReadOffset(key, lastConsumed);
    }

    @Override
    public String toString() {
        return new String(key) + ":" + offset;
    }

}