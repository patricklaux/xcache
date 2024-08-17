package com.igeeksky.redis.lettuce.config.props;

import java.util.List;

/**
 * 单机模式 或 副本集模式
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-05
 */
public class LettuceStandalone extends LettuceGeneric {

    /**
     * 配置示例：127.0.0.1:6379 或 socket:/tmp/redis.sock
     * <p>
     * Lettuce 底层采用 Netty 实现，支持 UnixSocket 方式与本机 Redis 进程通信。
     * <p>
     * 如需使用 UnixSocket，需满足以下条件：<p>
     * 1. 应用进程与 Redis 进程处于同一主机；<p>
     * 2. 主机操作系统需支持 UnixSocket（POSIX compliant system），如 Linux；<p>
     * 3. Maven 需配置 Netty 平台相关的本地传输组件；<p>
     * 4. Redis 需配置为支持 UnixSocket。<p>
     * <a href="https://lettuce.io/core/release/reference/#native-transports">See: lettuce-Native Transports</a><p>
     * <p>----------------</p>
     * <b>注意</b>：　<p>
     * 1. 如果 nodes 未配置，采用动态拓扑的方式，通过 node 的配置，发现并连接所有 Redis；<p>
     * 2. 如果 nodes 有配置，采用静态拓扑的方式，即仅连接已配置的 Redis（包括 node 和 nodes 配置的连接）。
     */
    private String node;

    /**
     * 读数据连接选用方式
     * 可选（不区分大小写）：<p>
     * upstream：仅主连接 <p>
     * upstreamPreferred：尽可能采用主连接 <p>
     * replica：仅副本集 <p>
     * replicaPreferred：尽可能采用副本集 <p>
     * lowestLatency：低延迟节点，因为延迟指标是快速变化的，因此需配置为动态拓扑发现的方式以动态测量所有节点的延迟 <p>
     * any：任意节点 <p>
     * anyReplica：任意副本节点 <p>
     *
     * @see io.lettuce.core.ReadFrom
     */
    private String readFrom;

    /**
     * 包括主连接和副本集，多个连接采用英文逗号分隔　<p>
     * 配置示例：socket:/tmp/redis.sock, 192.168.0.100:6379, 192.168.0.100:6380　<p>
     * <b>注意</b>：　<p>
     * 1. 如果 nodes 未配置，采用动态拓扑的方式，通过 node 的配置，发现并连接所有 Redis；<p>
     * 2. 如果 nodes 有配置，采用静态拓扑的方式，即仅连接已配置的 Redis（包括 node 和 nodes 配置的连接）。
     */
    private List<String> nodes;

    /**
     * 客户端选项
     *
     * @see io.lettuce.core.ClientOptions
     */
    private Lettuce.ClientOptions clientOptions;

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getReadFrom() {
        return readFrom;
    }

    public void setReadFrom(String readFrom) {
        this.readFrom = readFrom;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public Lettuce.ClientOptions getClientOptions() {
        return clientOptions;
    }

    public void setClientOptions(Lettuce.ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

}