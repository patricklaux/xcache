package com.igeeksky.xcache.autoconfigure.redis;


import com.igeeksky.xcache.redis.stat.RedisCacheStatProvider;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xredis.common.stream.container.StreamContainer;
import com.igeeksky.xredis.lettuce.api.RedisOperatorFactory;
import com.igeeksky.xredis.lettuce.props.LettuceCluster;
import com.igeeksky.xredis.lettuce.props.LettuceSentinel;
import com.igeeksky.xredis.lettuce.props.LettuceStandalone;
import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.concurrent.CompletableFuture;

/**
 * Lettuce 配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-27
 */
public class LettuceConfig {

    private String id = "lettuce";

    private int batchSize = 10000;

    private long batchTimeout = 60000;

    private StreamOptions stream;

    private RedisStatOptions stat;

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
     * Redis 批处理同步阻塞超时时间
     * <p>
     * 默认值：60000 单位：毫秒
     * <p>
     * 如果调用同步接口，譬如 {@code cache.get(key)}、{@code cache.getAll(keys)} 等，
     * 会先调用异步接口获取 {@link CompletableFuture}，然后再调用 {@code future.get(timeout, TimeUnit.MILLISECONDS)}
     * 方法等待数据处理完成。<p>
     * <b>注意：</b><p>
     * 1、当调用同步接口时，如果异步操作未完成或线程被中断，会抛出异常。<br>
     * 2、当调用同步接口时，单次操作数据量大、网络条件差、RedisServer 数据处理能力弱，请适当调大超时时间。
     *
     * @return {@link Long} – Redis 批处理同步阻塞超时时间
     */
    public long getBatchTimeout() {
        return batchTimeout;
    }

    /**
     * Redis 批处理同步阻塞超时时间
     * <p>
     * 默认值：60000 单位：毫秒
     * <p>
     * 如果调用同步接口，譬如 {@code cache.get(key)}、{@code cache.getAll(keys)} 等，
     * 会先调用异步接口获取 {@link CompletableFuture}，然后再调用 {@code future.get(timeout, TimeUnit.MILLISECONDS)}
     * 方法等待数据处理完成。<p>
     * <b>注意：</b><p>
     * 1、当调用同步接口时，如果异步操作未完成或线程被中断，会抛出异常。<br>
     * 2、当调用同步接口时，单次操作数据量大、网络条件差、RedisServer 数据处理能力弱，请适当调大超时时间。
     *
     * @param batchTimeout Redis 批处理同步阻塞超时时间
     */
    public void setBatchTimeout(long batchTimeout) {
        this.batchTimeout = batchTimeout;
    }

    /**
     * Redis 缓存指标统计配置
     * <p>
     * <b>注意：</b>
     * {@link RedisCacheStatProvider} 仅负责采集并发送缓存指标统计信息到指定 Stream，统计信息消费端需自行实现。
     *
     * @return {@link RedisStatOptions} – Redis 缓存指标统计配置
     */
    public RedisStatOptions getStat() {
        return stat;
    }

    /**
     * Redis 缓存指标统计配置
     *
     * @param stat Redis 缓存指标统计配置
     */
    public void setStat(RedisStatOptions stat) {
        this.stat = stat;
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
