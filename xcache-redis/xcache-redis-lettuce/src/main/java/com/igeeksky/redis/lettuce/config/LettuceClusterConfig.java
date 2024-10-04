package com.igeeksky.redis.lettuce.config;

import com.igeeksky.redis.RedisNode;
import com.igeeksky.redis.lettuce.props.Lettuce;
import io.lettuce.core.ReadFrom;

import java.util.List;

/**
 * Lettuce 集群配置项
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
public final class LettuceClusterConfig extends LettuceGenericConfig {

    private ReadFrom readFrom = ReadFrom.UPSTREAM;

    private List<RedisNode> nodes;

    private Lettuce.ClusterClientOptions clientOptions;

    /**
     * 默认构造函数
     */
    public LettuceClusterConfig() {
    }

    /**
     * 获取：读节点选择策略
     * <p>
     * 默认：UPSTREAM
     *
     * @return {@link ReadFrom} – 读节点选择策略
     */
    public ReadFrom getReadFrom() {
        return readFrom;
    }

    /**
     * 设置：读节点选择策略
     *
     * @param readFrom 读节点选择策略
     */
    public void setReadFrom(ReadFrom readFrom) {
        this.readFrom = readFrom;
    }

    /**
     * 获取：集群节点列表
     *
     * @return {@link List<RedisNode>} – 集群节点列表
     */
    public List<RedisNode> getNodes() {
        return nodes;
    }

    /**
     * 设置：集群节点列表
     *
     * @param nodes 集群节点列表
     */
    public void setNodes(List<RedisNode> nodes) {
        this.nodes = nodes;
    }

    /**
     * 获取：集群客户端配置项
     *
     * @return {@link Lettuce.ClusterClientOptions} – 集群客户端配置项
     */
    public Lettuce.ClusterClientOptions getClientOptions() {
        return clientOptions;
    }

    /**
     * 设置：集群客户端配置项
     *
     * @param clientOptions 集群客户端配置项
     */
    public void setClientOptions(Lettuce.ClusterClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

}