package com.igeeksky.xcache.autoconfigure.redis;


import com.igeeksky.xcache.redis.metrics.RedisCacheMetricsProvider;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xredis.common.stream.container.StreamContainer;
import com.igeeksky.xredis.lettuce.api.RedisOperatorFactory;
import com.igeeksky.xredis.lettuce.props.LettuceCluster;
import com.igeeksky.xredis.lettuce.props.LettuceSentinel;
import com.igeeksky.xredis.lettuce.props.LettuceStandalone;
import com.igeeksky.xtool.core.json.SimpleJSON;

/**
 * Lettuce 配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-27
 */
public class LettuceConfig {

    private String id = "lettuce";

    private int batchSize = 10000;

    private boolean compatible = false;

    private StreamOptions stream;

    private RedisMetricsOptions metrics;

    private RedisSyncOptions sync;

    private LettuceCluster cluster;

    private LettuceSentinel sentinel;

    private LettuceStandalone standalone;

    /**
     * 默认构造函数
     */
    public LettuceConfig() {
    }

    /**
     * {@link RedisOperatorFactory} 唯一标识
     * <p>
     * 默认值：lettuce
     * <p>
     * 如果仅一套 Redis 配置，保持默认即可。<br>
     * 如果有多套 Redis 配置，可依次配置为 lettuce1, lettuce2 …… 或你所喜欢的任意名称，保持唯一即可。
     *
     * @return {@code String} – {@link RedisOperatorFactory} 唯一标识
     */
    public String getId() {
        return id;
    }

    /**
     * {@link RedisOperatorFactory} 唯一标识
     *
     * @param id {@link RedisOperatorFactory} 唯一标识
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 单批次命令提交数量阈值，默认值：10000
     * <p>
     * 如 {@code batchSize} 设为 10000，当 {@link RedisOperatorProxy} 接收到单次操作 100 万条数据的请求时，
     * 会将数据切分为 100 份，每份 10000 条数据，然后分 100 批次提交到 RedisServer。
     *
     * @return {@code Integer} – 单批次发送 Redis 命令的最大数量
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * 单批次命令提交数量阈值，默认值：10000
     * <p>
     * 如 {@code batchSize} 设为 10000，当 {@link RedisOperatorProxy} 接收到单次操作 100 万条数据的请求时，
     * 会将数据切分为 100 份，每份 10000 条数据，然后分 100 批次提交到 RedisServer。
     *
     * @param batchSize 单批次发送 Redis 命令的最大数量
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * 兼容模式，默认值：false
     * <p>
     * true：兼容模式，不使用脚本执行数据操作，仅使用 Redis String、Hash……等命令执行数据操作。
     * <p>
     * false：非兼容模式，部分批量数据操作使用脚本执行，以提高性能。
     *
     * @return {@code Boolean} – 兼容模式，默认值：false
     */
    public boolean isCompatible() {
        return compatible;
    }

    /**
     * 兼容模式，默认值：false
     * <p>
     * true：兼容模式，不使用脚本执行数据操作，仅使用 Redis String、Hash……等命令执行数据操作。
     * <p>
     * false：非兼容模式，部分批量数据操作使用脚本执行，以提高性能。
     *
     * @param compatible 是否为兼容模式
     */
    public void setCompatible(boolean compatible) {
        this.compatible = compatible;
    }

    /**
     * Redis 缓存指标统计配置
     * <p>
     * <b>注意：</b>
     * {@link RedisCacheMetricsProvider} 仅负责采集并发送缓存指标统计信息到指定 Stream，统计信息消费端需自行实现。
     *
     * @return {@link RedisMetricsOptions} – Redis 缓存指标统计配置
     */
    public RedisMetricsOptions getMetrics() {
        return metrics;
    }

    /**
     * Redis 缓存指标统计配置
     *
     * @param metrics Redis 缓存指标统计配置
     */
    public void setMetrics(RedisMetricsOptions metrics) {
        this.metrics = metrics;
    }

    /**
     * Redis 缓存数据同步配置
     *
     * @return {@link RedisSyncOptions} – Redis 缓存数据同步配置
     */
    public RedisSyncOptions getSync() {
        return sync;
    }

    /**
     * Redis 缓存数据同步配置
     *
     * @param sync Redis 缓存数据同步配置
     */
    public void setSync(RedisSyncOptions sync) {
        this.sync = sync;
    }

    /**
     * StreamListenerContainer 配置项
     * <p>
     * {@link StreamContainer}
     *
     * @return {@link StreamOptions} – StreamListenerContainer 配置项
     */
    public StreamOptions getStream() {
        return stream;
    }

    /**
     * StreamListenerContainer 配置项
     *
     * @param stream {@link StreamContainer} 配置项
     */
    public void setStream(StreamOptions stream) {
        this.stream = stream;
    }

    /**
     * 集群模式配置
     * <p>
     * <b>注意事项：</b>
     * <p>
     * 初始化 Lettuce 客户端时，优先读取 sentinel 配置，其次是 cluster 配置，最后才是 standalone 配置。
     * <p>
     * 1、首先，如 sentinel 配置不为空，使用 sentinel 配置，忽略 cluster 配置 和 standalone 配置。<br>
     * 2、其次，如 sentinel 配置为空，读取 cluster 配置。如 cluster 配置不为空，使用 cluster 配置，忽略 standalone 配置。<br>
     * 3、最后，如 cluster 配置为空，则读取 standalone 配置。
     *
     * @return {@link LettuceCluster} – 集群模式配置
     */
    public LettuceCluster getCluster() {
        return cluster;
    }

    /**
     * 集群模式配置
     *
     * @param cluster 集群模式配置
     */
    public void setCluster(LettuceCluster cluster) {
        this.cluster = cluster;
    }

    /**
     * 哨兵模式配置
     * <p>
     * <b>注意事项：</b>
     * <p>
     * 初始化 Lettuce 客户端时，优先读取 sentinel 配置，其次是 cluster 配置，最后才是 standalone 配置。
     * <p>
     * 1、首先，如 sentinel 配置不为空，使用 sentinel 配置，忽略 cluster 配置 和 standalone 配置。<br>
     * 2、其次，如 sentinel 配置为空，读取 cluster 配置。如 cluster 配置不为空，使用 cluster 配置，忽略 standalone 配置。<br>
     * 3、最后，如 cluster 配置为空，则读取 standalone 配置。
     *
     * @return {@link LettuceSentinel} – 哨兵模式配置
     */
    public LettuceSentinel getSentinel() {
        return sentinel;
    }

    /**
     * 哨兵模式配置
     *
     * @param sentinel 哨兵模式配置
     */
    public void setSentinel(LettuceSentinel sentinel) {
        this.sentinel = sentinel;
    }

    /**
     * 单机模式 或 副本集模式 配置
     * <p>
     * <b>注意事项：</b>
     * <p>
     * 初始化 Lettuce 客户端时，优先读取 sentinel 配置，其次是 cluster 配置，最后才是 standalone 配置。
     * <p>
     * 1、首先，如 sentinel 配置不为空，使用 sentinel 配置，忽略 cluster 配置 和 standalone 配置。<br>
     * 2、其次，如 sentinel 配置为空，读取 cluster 配置。如 cluster 配置不为空，使用 cluster 配置，忽略 standalone 配置。<br>
     * 3、最后，如 cluster 配置为空，则读取 standalone 配置。
     *
     * @return {@link LettuceStandalone} – 单机模式 或 副本集模式 配置
     */
    public LettuceStandalone getStandalone() {
        return standalone;
    }

    /**
     * 单机模式 或 副本集模式 配置
     *
     * @param standalone 单机模式 或 副本集模式 配置
     */
    public void setStandalone(LettuceStandalone standalone) {
        this.standalone = standalone;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}
