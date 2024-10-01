package com.igeeksky.redis.stream;

import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.lang.Assert;

/**
 * 读取偏移
 *
 * @author Patrick.Lau
 * @see <a href="https://redis.io/commands/xread">XREAD</a>
 * @see <a href="https://redis.io/docs/latest/commands/xreadgroup/">XREADGROUP</a>
 * @since 1.0.0 2024/7/20
 */
public class ReadOffset {

    /**
     * 从第一个消息开始读取
     */
    public static final String first = "0-0";

    /**
     * 读取新到达的消息（适用于阻塞模式）
     */
    public static final String latest = "$";

    /**
     * 读取大于最后消费位置的消息（适用于消费者组）
     */
    public static final String lastConsumed = ">";

    private final byte[] key;

    private final String offset;

    /**
     * 构造函数
     *
     * @param key    流名称
     * @param offset 偏移量
     */
    private ReadOffset(byte[] key, String offset) {
        Assert.isTrue(ArrayUtils.isNotEmpty(key), "key must not be null or empty");
        Assert.hasText(offset, "offset must not be null or empty");
        this.key = key;
        this.offset = offset;
    }

    /**
     * 获取流名称
     *
     * @return {@code byte[]} – 流名称
     */
    public byte[] getKey() {
        return key;
    }

    /**
     * 获取偏移量
     *
     * @return {@link String} – 偏移量
     */
    public String getOffset() {
        return offset;
    }

    /**
     * 指定读取偏移量
     *
     * @param key    流名称
     * @param offset 偏移量
     * @return {@code ReadOffset}
     */
    public static ReadOffset from(byte[] key, String offset) {
        return new ReadOffset(key, offset);
    }

    /**
     * 从第一个消息开始读取
     *
     * @param key 流名称
     * @return {@code ReadOffset}
     */
    public static ReadOffset first(byte[] key) {
        return new ReadOffset(key, first);
    }

    /**
     * 读取新到达的消息（适用于阻塞模式）
     *
     * @param key 流名称
     * @return {@code ReadOffset}
     */
    public static ReadOffset latest(byte[] key) {
        return new ReadOffset(key, latest);
    }

    /**
     * 读取大于最后消费位置的消息（适用于消费者组）
     *
     * @param key 流名称
     * @return {@code ReadOffset}
     */
    public static ReadOffset lastConsumed(byte[] key) {
        return new ReadOffset(key, lastConsumed);
    }

    @Override
    public String toString() {
        return new String(key) + ":" + offset;
    }

}