package com.igeeksky.redis.lettuce.props;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.List;

/**
 * 单机模式 或 副本集模式
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-05
 */
public final class LettuceStandalone extends LettuceGeneric {

    private String node;

    private String readFrom;

    private List<String> nodes;

    private Lettuce.ClientOptions clientOptions;

    /**
     * 默认构造函数
     */
    public LettuceStandalone() {
    }

    /**
     * Redis 连接地址，支持 UnixSocket 方式。
     * <p>
     * <b>配置示例：</b> <p>
     * {@code 127.0.0.1:6379} 或 {@code socket:/tmp/redis.sock}
     * <p>
     * <b>配置说明：</b>
     * <p>
     * Lettuce 底层采用 Netty 实现，支持 UnixSocket 方式与本机 Redis 进程通信。
     * <p>
     * 如需使用 UnixSocket，需满足以下条件：<br>
     * 1. 应用进程与 Redis Server 进程处于同一主机；<br>
     * 2. 主机系统支持 UnixSocket（POSIX compliant system），如 Linux；<br>
     * 3. Maven 需配置平台相关的 Netty 本地传输组件；<br>
     * 4. Redis Server 需配置为支持 UnixSocket。
     * <p>
     * See Also: <a href="https://redis.github.io/lettuce/advanced-usage/#native-transports">lettuce-Native Transports</a>
     * <p>
     * <b>注意事项：</b>
     * <p>
     * 1. 如果 nodes 未配置，采用动态拓扑的方式，通过 node 配置的节点，连接所有动态发现的节点；<br>
     * 2. 如果 nodes 有配置，采用静态拓扑的方式，即仅连接已配置的节点（包括 node 和 nodes 配置的连接）。
     *
     * @return {@code String} – Redis 连接地址
     */
    public String getNode() {
        return node;
    }

    /**
     * 设置 Redis 连接地址，支持 UnixSocket 方式。
     *
     * @param node Redis 连接地址
     */
    public void setNode(String node) {
        this.node = node;
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
     * 节点列表
     * <p>
     * 包括主连接和副本集，多个连接采用英文逗号分隔
     * <p>
     * <b>配置示例：</b><p>
     * {@code socket:/tmp/redis.sock, 192.168.0.100:6379, 192.168.0.100:6380}
     * <p>
     * <b>注意事项：</b><p>
     * 1. 如果 nodes 未配置，采用动态拓扑的方式，通过 node 配置的节点，连接所有动态发现的节点；<br>
     * 2. 如果 nodes 有配置，采用静态拓扑的方式，即仅连接已配置的节点（包括 node 和 nodes 配置的连接）。
     *
     * @return {@code List<String>} – 节点列表
     */
    public List<String> getNodes() {
        return nodes;
    }

    /**
     * 设置 节点列表
     *
     * @param nodes 节点列表
     */
    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    /**
     * 客户端选项
     * <p>
     * 用以控制某些特定的客户端行为，如：是否自动重连、连接超时等
     *
     * @return {@code Lettuce.ClientOptions} 客户端选项
     * @see io.lettuce.core.ClientOptions
     */
    public Lettuce.ClientOptions getClientOptions() {
        return clientOptions;
    }

    /**
     * 设定 客户端选项
     *
     * @param clientOptions 客户端选项
     * @see io.lettuce.core.ClientOptions
     */
    public void setClientOptions(Lettuce.ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}