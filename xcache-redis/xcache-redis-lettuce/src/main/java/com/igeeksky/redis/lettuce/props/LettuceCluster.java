package com.igeeksky.redis.lettuce.props;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.List;

/**
 * Lettuce 集群配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
public final class LettuceCluster extends LettuceGeneric {

    private String readFrom;
    private List<String> nodes;
    private Lettuce.ClusterClientOptions clientOptions;

    /**
     * 默认构造函数
     */
    public LettuceCluster() {
    }

    /**
     * 读节点选择策略（不区分大小写）
     * <p>
     * upstream：仅主连接 <br>
     * upstreamPreferred：尽可能采用主连接 <br>
     * replica：仅副本集 <br>
     * replicaPreferred：尽可能采用副本集 <br>
     * lowestLatency：低延迟节点，因为延迟指标是快速变化的，因此需配置为动态拓扑发现的方式以动态测量所有节点的延迟 <br>
     * any：任意节点 <br>
     * anyReplica：任意副本节点
     *
     * @return {@link String} – 读节点选择策略
     * @see io.lettuce.core.ReadFrom
     */
    public String getReadFrom() {
        return readFrom;
    }

    /**
     * 设置 读节点选择策略（不区分大小写）
     *
     * @param readFrom 读节点选择策略
     * @see io.lettuce.core.ReadFrom
     */
    public void setReadFrom(String readFrom) {
        this.readFrom = readFrom;
    }

    /**
     * 集群节点列表
     *
     * @return {@link List<String>} – 集群节点列表
     */
    public List<String> getNodes() {
        return nodes;
    }

    /**
     * 设置 集群节点列表
     *
     * @param nodes 集群节点列表
     */
    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    /**
     * 集群客户端选项
     * <p>
     * 用以控制某些特定的客户端行为，如：是否自动重连、连接超时、最大跳转次数等。
     *
     * @return {@link Lettuce.ClusterClientOptions} – 集群客户端配置
     * @see io.lettuce.core.cluster.ClusterClientOptions
     */
    public Lettuce.ClusterClientOptions getClientOptions() {
        return clientOptions;
    }

    /**
     * 设置 集群客户端选项
     *
     * @param clientOptions 集群客户端选项
     */
    public void setClientOptions(Lettuce.ClusterClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}