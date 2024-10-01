package com.igeeksky.redis.stream;

import com.igeeksky.xtool.core.lang.Assert;

/**
 * Stream：消费者组创建选项
 * <p>
 * 命令格式：{@code XGROUP CREATE key group <id | $> [MKSTREAM] [ENTRIESREAD entries-read]}
 * <p>
 * 命令参考：{@code XGROUP CREATE mystream mygroup 0 MKSTREAM ENTRIESREAD 0}
 *
 * @author Patrick.Lau
 * @see <a href="https://redis.io/docs/latest/commands/xgroup-create/">XGROUP CREATE</a>
 * @since 1.0.0 2024/7/21
 */
public class GroupCreateOptions {

    /**
     * 是否创建流
     */
    private final boolean mkstream;

    /**
     * 已读消息初始数量（用于消费者组滞后跟踪）
     */
    private final Long entriesRead;

    /**
     * 私有构造函数
     *
     * @param mkstream    是否创建流
     * @param entriesRead 消费者组已读消息初始数量
     */
    private GroupCreateOptions(boolean mkstream, Long entriesRead) {
        if (entriesRead != null) {
            Assert.isTrue(entriesRead >= -1, "entriesRead must be greater than or equal to -1");
        }

        this.mkstream = mkstream;
        this.entriesRead = entriesRead;
    }

    /**
     * 是否包含有效参数
     *
     * @return {@code boolean} – {@code false}，无需附加可选参数；{@code true}，需要附加可选参数
     */
    public boolean valid() {
        return mkstream || entriesRead != null;
    }

    /**
     * 获取：是否附加 {@code MKSTREAM} 参数
     *
     * @return {@code boolean} – 是否附加 {@code MKSTREAM} 参数
     * @see <a href="https://redis.io/docs/latest/commands/xgroup-create/">XGROUP CREATE</a>
     */
    public boolean isMkstream() {
        return mkstream;
    }

    /**
     * 获取：消费者组已读消息初始数量
     *
     * @return {@code Long} – 消费者组已读消息初始数量
     * @see <a href="https://redis.io/docs/latest/commands/xgroup-create/">XGROUP CREATE</a>
     */
    public Long getEntriesRead() {
        return entriesRead;
    }

    /**
     * 附加 {@code MKSTREAM} 参数
     * <p>
     * {@code true}  – 附加 {@code MKSTREAM} 参数，如流不存在则创建流；<br>
     * {@code false} – 附加 {@code MKSTREAM} 参数，如流不存在返回错误。
     * <p>
     * 默认值：{@code false}，调用此方法后设为 {@code true}。
     * <p>
     * 执行 {@code XGROUP CREATE} 命令时，期望 stream 已存在，通常保持默认即可。
     *
     * @return {@code GroupCreateOptions} – 附加 {@code MKSTREAM} 参数
     * @see <a href="https://redis.io/docs/latest/commands/xgroup-create/">XGROUP CREATE</a>
     */
    public static GroupCreateOptions mkstream() {
        return new GroupCreateOptions(true, null);
    }

    /**
     * 设置：消费者组已读消息初始数量
     * <p>
     * 默认值：{@code null}
     * <p>
     * 用于消费者组滞后跟踪，计算消费者组剩余多少条消息未读，取值范围为 [-1, Long.MAX_VALUE]。
     * <p>
     * 每读取 1 条消息，entries-read 加 1；每添加 1 条消息，entries-added 加 1。<br>
     * {@code lag（滞后量） = (entries-added) - (entries-read)}
     * <p>
     * <b>参数说明</b><br>
     * 假设：当前 stream 已添加消息数为 10，即 {@code entries-added = 10}。
     * <p>
     * <b>设定为 1</b><br>
     * 创建 group 时，如设定 {@code entriesRead} 初始为 1，则表示该消费者组已读 1 条消息，落后进度为 10 - 1 = 9。<br>
     * 读取 2 条消息后，已读变为 1 + 2 = 3，落后进度变为 10 - 3 = 7。<br>
     * 如该 stream 又添加了 5 条消息，则已读仍为 3 条消息，落后进度为 15 - 3 = 12。
     * <p>
     * <b>设定为 0</b><br>
     * 创建 group 时，如设定 {@code entriesRead} 初始为 0，则表示该消费者组已读 0 条消息，落后进度为 10 - 0 = 10。
     * <p>
     * <b>设定为 -1</b><br>
     * 创建 group 时，如设定 {@code entriesRead} 初始为 -1，相当于未附加 {@code ENTRIESREAD} 参数，
     * 表示该消费者组已读消息数和落后进度暂时为空，后续该组开始读取第一条消息时再由 redis 自动计算已读消息数。<br>
     * 如果该消费组设定的起始 id 为第 5 条消息，那么读取 1 条消息后，entries-read 自动计算为 5 + 1 = 6，lag = 10 - 6 = 4。
     * <p>
     * <b>设定为 18</b><br>
     * 创建 group 时，如设定 {@code entriesRead} 初始为 18，则表示该消费者组已读 18 条消息，落后进度为 10 - 18 = -8，
     * 此时落后进度会变成负数。
     * <p>
     * 因此，{@code entriesRead} 只是逻辑参考值，仅用于计算滞后量，并不一定是实际已读消息数量。<br>
     * 无论 {@code entriesRead} 设定的值是多少，都不会跳过消息，读取消息的起点都是创建 group 时设定的起始 id（不包含该 id）。<br>
     * 另，{@code ENTRIESREAD} 参数从 Redis 7.0.0 才开始支持。
     *
     * @param entriesRead 消费者组已读消息初始数量
     * @return {@code GroupCreateOptions} – 附加 {@code ENTRIESREAD} 参数
     * @see <a href="https://redis.io/docs/latest/commands/xgroup-create/">XGROUP CREATE</a>
     */
    public static GroupCreateOptions entriesRead(long entriesRead) {
        return new GroupCreateOptions(false, entriesRead);
    }

    /**
     * 创建 {@code GroupCreateOptions} 实例
     *
     * @param mkstream    是否创建流
     * @param entriesRead 消费者组已读消息初始数量
     * @return {@code GroupCreateOptions}
     * @see <a href="https://redis.io/docs/latest/commands/xgroup-create/">XGROUP CREATE</a>
     */
    public static GroupCreateOptions of(boolean mkstream, long entriesRead) {
        return new GroupCreateOptions(mkstream, entriesRead);
    }

}