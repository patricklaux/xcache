package com.igeeksky.redis.stream;

/**
 * 读取选项
 *
 * @author Patrick.Lau
 * @see <a href="https://redis.io/commands/xread">XREAD</a>
 * @since 1.0.0 2024/7/21
 */
public class ReadOptions {

    /**
     * 阻塞时间（毫秒）<p>
     * Redis 接收到流读取命令后，如果没有新消息，则阻塞指定时长，直到有新消息到达或超时
     */
    private final Long block;

    /**
     * 最大读取数量
     */
    private final Long count;

    /**
     * 私有构造函数，禁止外部实例化
     *
     * @param block 阻塞时间（毫秒）
     * @param count 最大读取数量
     */
    private ReadOptions(Long block, Long count) {
        this.block = block;
        this.count = count;
    }

    /**
     * 阻塞时间（毫秒）
     *
     * @return {@link Long} – 阻塞时间（毫秒）
     */
    public Long getBlock() {
        return block;
    }

    /**
     * 最大读取数量
     *
     * @return {@link Long} – 最大读取数量
     */
    public Long getCount() {
        return count;
    }

    /**
     * 判断参数是否有效
     *
     * @return {@code true} - 有效；{@code false} - 无效
     */
    public boolean valid() {
        return block != null || count != null;
    }

    /**
     * 创建一个只有阻塞时间的流读取参数
     *
     * @param block 阻塞时间（毫秒）
     * @return 流读取参数
     */
    public static ReadOptions block(long block) {
        return new ReadOptions(block, null);
    }

    /**
     * 创建一个只有最大读取数量的流读取参数
     *
     * @param count 最大读取数量
     * @return 流读取参数
     */
    public static ReadOptions count(long count) {
        return new ReadOptions(null, count);
    }

    /**
     * 创建流读取选项
     *
     * @param block 阻塞时间（毫秒）
     * @param count 最大读取数量
     * @return 流读取参数
     */
    public static ReadOptions of(long block, long count) {
        return new ReadOptions(block, count);
    }

}