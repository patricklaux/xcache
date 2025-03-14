package com.igeeksky.xcache.autoconfigure.redis;

import com.igeeksky.xredis.common.stream.container.StreamContainer;
import com.igeeksky.xtool.core.json.SimpleJSON;

/**
 * {@link StreamContainer} 配置项
 */
public class StreamOptions {

    /**
     * 读取 Stream 时的阻塞时长
     * <p>
     * 默认值： 10 单位：毫秒（{@code 必须大于 0}）
     * <p>
     * {@code block > 0 }： {@link StreamContainer} 会最多阻塞 block 毫秒，直到有新消息到达；<br>
     * {@code block == 0}，则 {@link StreamContainer} 会无限期阻塞，直到有新消息到达；<br>
     * {@code block < 0 }，不阻塞。
     * <p>
     * <b>注意：</b><p>
     * {@link StreamContainer} 的每次任务是使用一个 {@code xread} 命令读取所有已订阅的 stream，
     * 当次任务执行完毕后会延迟 delay 毫秒后再开始新任务。<br>
     * 即：<br>
     * 当 {@link StreamContainer} 已经开始拉取消息，新订阅的 stream 需等到下次任务开始时才会被读取，
     * 因此建议 block 配置为一个较小的值，且大于 0。
     */
    private long block = 10;

    /**
     * 每次任务每个 Stream 拉取消息的最大数量
     * <p>
     * 默认值： 1000（{@code 必须大于 0 且小于 1<<29}）
     * <p>
     * <b>注意：</b> <p>
     * 这个最大数量是指每个 Stream 拉取消息的最大数量，总数量取决于 {@link StreamContainer} 的 stream 数量。
     * <p>
     * {@link StreamContainer} 采用 {@code xread} 命令读取所有已订阅的 stream，该命令可以一次传入多个 Stream。<br>
     * 如果 {@code count} 设为 1000，那么每次任务拉取消息的 {@code 最大总数量 = 1000 * stream 数量}。
     * <p>
     * 命令示例：{@code XREAD COUNT 1000 BLOCK 10 STREAMS mystream1 mystream2 mystream3 0-0 0-0 0-0} <br>
     * 上面这个命令，拉取消息的最大总数量为 3000。
     *
     * @see <a href="https://redis.io/docs/latest/commands/xread/">XREAD</a>
     */
    private int count = 1000;

    /**
     * 两次拉取消息任务的间隔时长
     * <p>
     * 默认值： 10 单位：毫秒
     */
    private long period = 10;

    /**
     * 读取 Stream 时的阻塞时长
     * <p>
     * 默认值： 10 单位：毫秒
     * <p>
     * {@code block > 0 }： {@link StreamContainer} 会最多阻塞 block 毫秒，直到有新消息到达；<br>
     * {@code block == 0}，则 {@link StreamContainer} 会无限期阻塞，直到有新消息到达；<br>
     * {@code block < 0 }，不阻塞。
     * <p>
     * <b>注意：</b><p>
     * {@link StreamContainer} 的每次任务是使用一个 {@code xread} 命令读取所有已订阅的 stream，
     * 当次任务执行完毕后会延迟 delay 毫秒后再开始新任务。<br>
     * 即：<br>
     * 当 {@link StreamContainer} 已经开始拉取消息，新订阅的 stream 需等到下次任务开始时才会被读取，
     * 因此建议 block 配置为一个较小的值，且大于 0。
     *
     * @return {@code long} – 读取 Stream 时的阻塞时长
     */
    public long getBlock() {
        return block;
    }

    /**
     * 读取 Stream 时的阻塞时长
     * <p>
     * 默认值： 10 单位：毫秒
     * <p>
     * {@code block > 0 }： {@link StreamContainer} 会最多阻塞 block 毫秒，直到有新消息到达；<br>
     * {@code block == 0}，则 {@link StreamContainer} 会无限期阻塞，直到有新消息到达；<br>
     * {@code block < 0 }，不阻塞。
     * <p>
     * <b>注意：</b><p>
     * {@link StreamContainer} 的每次任务是使用一个 {@code xread} 命令读取所有已订阅的 stream，
     * 当次任务执行完毕后会延迟 delay 毫秒后再开始新任务。<br>
     * 即：<br>
     * 当 {@link StreamContainer} 已经开始拉取消息，新订阅的 stream 需等到下次任务开始时才会被读取，
     * 因此建议 block 配置为一个较小的值，且大于 0。
     *
     * @param block 读取 Stream 时的阻塞时长
     */
    public void setBlock(long block) {
        this.block = block;
    }

    /**
     * 每次任务每个 Stream 拉取消息的最大数量
     * <p>
     * 默认值： 1000（{@code 必须大于 0 且小于 1<<29}）
     * <p>
     * <b>注意：</b> <p>
     * 这个最大数量是指每个 Stream 拉取消息的最大数量，总数量取决于 {@link StreamContainer} 的 stream 数量。
     * <p>
     * {@link StreamContainer} 采用 {@code xread} 命令读取所有已订阅的 stream，该命令可以一次传入多个 Stream。<br>
     * 如果 {@code count} 设为 1000，那么每次任务拉取消息的 {@code 最大总数量 = 1000 * stream 数量}。
     * <p>
     * 命令示例：{@code XREAD COUNT 1000 BLOCK 10 STREAMS mystream1 mystream2 mystream3 0-0 0-0 0-0} <br>
     * 上面这个命令，拉取消息的最大总数量为 3000。
     *
     * @return {@code int} – 每次任务每个 Stream 拉取消息的最大数量
     * @see <a href="https://redis.io/docs/latest/commands/xread/">XREAD</a>
     */
    public int getCount() {
        return count;
    }

    /**
     * 每次任务每个 Stream 拉取消息的最大数量
     * <p>
     * 默认值： 1000（{@code 必须大于 0 且小于 1<<29}）
     * <p>
     * <b>注意：</b> <p>
     * 这个最大数量是指每个 Stream 拉取消息的最大数量，总数量取决于 {@link StreamContainer} 的 stream 数量。
     * <p>
     * {@link StreamContainer} 采用 {@code xread} 命令读取所有已订阅的 stream，该命令可以一次传入多个 Stream。<br>
     * 如果 {@code count} 设为 1000，那么每次任务拉取消息的 {@code 最大总数量 = 1000 * stream 数量}。
     * <p>
     * 命令示例：{@code XREAD COUNT 1000 BLOCK 10 STREAMS mystream1 mystream2 mystream3 0-0 0-0 0-0} <br>
     * 上面这个命令，拉取消息的最大总数量为 3000。
     *
     * @param count 每次任务每个 Stream 拉取消息的最大数量
     * @see <a href="https://redis.io/docs/latest/commands/xread/">XREAD</a>
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * 两次拉取消息任务的间隔时长
     * <p>
     * 默认值： 10 单位：毫秒（{@code 必须大于 0}）
     *
     * @return {@code long} – 两次拉取消息任务的间隔时长
     */
    public long getPeriod() {
        return period;
    }

    /**
     * 两次拉取消息任务的间隔时长
     * <p>
     * 默认值： 10 单位：毫秒（{@code 必须大于 0}）
     *
     * @param period 两次拉取消息任务的间隔时长
     */
    public void setPeriod(long period) {
        this.period = period;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}