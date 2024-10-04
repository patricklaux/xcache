package com.igeeksky.redis.stream;

import java.util.List;
import java.util.Map;

/**
 * Redis Stream 客户端
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/20
 */
public interface RedisStreamOperator extends AutoCloseable {

    /**
     * 添加一个新元素到指定的 Stream
     *
     * @param key  Stream 的名称
     * @param body Stream 元素的 body，一个元素可以包含多个 field 和 value
     * @return {@link String} – 新添加的 Stream 元素的 ID
     */
    String xadd(byte[] key, Map<byte[], byte[]> body);

    /**
     * 添加一个新元素到指定的 Stream
     *
     * @param key  Stream 的名称
     * @param args 添加元素时的参数，如：ID、MAXLEN、MINID 等
     * @param body Stream 元素的 body，一个元素可以包含多个 field 和 value
     * @return {@link String} – 新添加的 Stream 元素的 ID
     */
    String xadd(byte[] key, AddOptions args, Map<byte[], byte[]> body);

    /**
     * 从一个或多个 Stream 中读取数据。
     *
     * @param streams 可以指定多个 Stream，每个 Stream 需指定一个偏移量。
     * @return {@link List<StreamMessage>} – 读取到的元素列表
     */
    List<StreamMessage> xread(ReadOffset... streams);

    /**
     * 从一个或多个 Stream 中读取数据。
     *
     * @param args    读取参数，如：BLOCK、COUNT 等
     * @param streams 可以指定多个 Stream，每个 Stream 需指定一个偏移量。
     * @return {@link List<StreamMessage>} – 读取到的元素列表
     */
    List<StreamMessage> xread(ReadOptions args, ReadOffset... streams);

    /**
     * 创建一个 Stream 的 Consumer Group。
     *
     * @param group        Consumer Group 的名称
     * @param streamOffset 偏移量，如："0-0"、"$"、">" 等
     * @return {@link String} – 创建结果，如：OK、NOGROUP、ALREADYEXISTS 等
     */
    String xgroupCreate(ReadOffset streamOffset, byte[] group);

    /**
     * 创建一个 Stream 的 Consumer Group。
     *
     * @param streamOffset 偏移量，如："0-0"、"$"、">" 等
     * @param group        Consumer Group 的名称
     * @param args         创建参数，如：MKSTREAM、ENTRIESREAD 等
     * @return {@link String} – 创建结果，如：OK、NOGROUP、ALREADYEXISTS 等
     */
    String xgroupCreate(ReadOffset streamOffset, byte[] group, GroupCreateOptions args);

    /**
     * 从一个或多个 Stream 中读取数据。
     *
     * @param group    Consumer Group 的名称
     * @param consumer Consumer 的名称
     * @param streams  可以指定多个 Stream，每个 Stream 需指定一个偏移量。
     * @return {@link List<StreamMessage>} – 读取到的元素列表
     */
    List<StreamMessage> xreadgroup(byte[] group, byte[] consumer, ReadOffset... streams);

    /**
     * 从一个或多个 Stream 中读取数据。
     *
     * @param group    Consumer Group 的名称
     * @param consumer Consumer 的名称
     * @param args     读取参数，如：BLOCK、COUNT 等
     * @param streams  可以指定多个 Stream，每个 Stream 需指定一个偏移量。
     * @return {@link List<StreamMessage>} – 读取到的元素列表
     */
    List<StreamMessage> xreadgroup(byte[] group, byte[] consumer, GroupReadOptions args, ReadOffset... streams);

    /**
     * 确认一个或多个 Stream 消息。
     *
     * @param key        Stream 通道名称
     * @param group      Consumer Group 的名称
     * @param messageIds 消息 ID，可以有多个
     * @return {@link Long} – 确认成功的消息数量
     */
    Long xack(byte[] key, byte[] group, String... messageIds);

    /**
     * 删除一个或多个 Stream 消息。
     *
     * @param key        Stream 通道名称
     * @param messageIds 消息 ID，可以有多个
     * @return {@link Long} – 删除成功的消息数量
     */
    Long xdel(byte[] key, String... messageIds);

    /**
     * 判断连接是否未关闭
     *
     * @return {@link Boolean} – true：未关闭；false：已关闭
     */
    boolean isOpen();
}