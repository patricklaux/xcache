package com.igeeksky.redis.stream;

/**
 * 消费者组读取消息选项
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/20
 */
public class GroupReadOptions {

    private final Long block;

    private final Long count;

    private final boolean noack;

    /**
     * 私有构造方法
     *
     * @param block 阻塞时长（毫秒）
     * @param count 读取数量
     * @param noack 是否无需确认
     */
    private GroupReadOptions(Long block, Long count, boolean noack) {
        this.block = block;
        this.count = count;
        this.noack = noack;
    }

    /**
     * 是否包含有效参数
     *
     * @return {@code boolean} – {@code false}，无需附加可选参数；{@code true}，需要附加可选参数
     */
    public boolean valid() {
        return block != null || count != null || noack;
    }

    /**
     * 获取：阻塞时长（毫秒）
     *
     * @return {@code Long} – 阻塞时长（毫秒）
     */
    public Long getBlock() {
        return block;
    }

    /**
     * 获取：读取数量
     *
     * @return {@code Long} – 读取数量
     */
    public Long getCount() {
        return count;
    }

    /**
     * 获取：是否无需确认
     *
     * @return {@code boolean} – {@code true}，无需确认；{@code false}，需要确认
     */
    public boolean isNoack() {
        return noack;
    }

    /**
     * 设置：阻塞时长（毫秒）
     *
     * @param block 阻塞时长（毫秒）
     * @return {@link GroupReadOptions}
     */
    public static GroupReadOptions block(Long block) {
        return new GroupReadOptions(block, null, false);
    }

    /**
     * 设置：读取数量
     *
     * @param count 读取数量
     * @return {@link GroupReadOptions}
     */
    public static GroupReadOptions count(Long count) {
        return new GroupReadOptions(null, count, false);
    }

    /**
     * 设置为无需确认
     *
     * @return {@link GroupReadOptions}
     */
    public static GroupReadOptions noack() {
        return new GroupReadOptions(null, null, true);
    }

    /**
     * 设置：阻塞时长（毫秒） 和 读取数量
     *
     * @param block 阻塞时长（毫秒）
     * @param count 读取数量
     * @return {@link GroupReadOptions}
     */
    public static GroupReadOptions of(Long block, Long count) {
        return new GroupReadOptions(block, count, false);
    }

    /**
     * 设置：阻塞时长（毫秒）、读取数量、是否无需确认
     *
     * @param block 阻塞时长（毫秒）
     * @param count 读取数量
     * @param noack 是否无需确认
     * @return {@link GroupReadOptions}
     */
    public static GroupReadOptions of(Long block, Long count, boolean noack) {
        return new GroupReadOptions(block, count, noack);
    }

}