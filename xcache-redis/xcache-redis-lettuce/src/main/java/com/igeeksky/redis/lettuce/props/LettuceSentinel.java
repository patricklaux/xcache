package com.igeeksky.redis.lettuce.props;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.List;

/**
 * Lettuce 哨兵配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
public final class LettuceSentinel extends LettuceGeneric {

    private String readFrom;
    private String masterId;
    private List<String> nodes;
    private String sentinelUsername;
    private String sentinelPassword;
    private Lettuce.ClientOptions clientOptions;

    /**
     * 默认构造函数
     */
    public LettuceSentinel() {
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
     * 哨兵主节点名称
     *
     * @return {@code String} – 哨兵主节点名称
     */
    public String getMasterId() {
        return masterId;
    }

    /**
     * 设置 哨兵主节点名称
     *
     * @param masterId 哨兵主节点名称
     */
    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    /**
     * 哨兵节点列表
     *
     * @return {@link List<String>} – 哨兵节点列表
     */
    public List<String> getNodes() {
        return nodes;
    }

    /**
     * 设置 哨兵节点列表
     *
     * @param nodes 哨兵节点列表
     */
    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    /**
     * 哨兵用户名
     *
     * @return {@link String} – 哨兵用户名
     */
    public String getSentinelUsername() {
        return sentinelUsername;
    }

    /**
     * 设置 哨兵用户名
     *
     * @param sentinelUsername 哨兵用户名
     */
    public void setSentinelUsername(String sentinelUsername) {
        this.sentinelUsername = sentinelUsername;
    }

    /**
     * 哨兵密码
     *
     * @return {@link String} – 哨兵密码
     */
    public String getSentinelPassword() {
        return sentinelPassword;
    }

    /**
     * 设置 哨兵密码
     *
     * @param sentinelPassword 哨兵密码
     */
    public void setSentinelPassword(String sentinelPassword) {
        this.sentinelPassword = sentinelPassword;
    }

    /**
     * 客户端选项
     * <p>
     * 用以控制某些特定的客户端行为，如：是否自动重连、连接超时等
     *
     * @return {@link Lettuce.ClientOptions} – 客户端选项
     * @see io.lettuce.core.ClientOptions
     */
    public Lettuce.ClientOptions getClientOptions() {
        return clientOptions;
    }

    /**
     * 设置 客户端选项
     *
     * @param clientOptions 客户端选项
     */
    public void setClientOptions(Lettuce.ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}
