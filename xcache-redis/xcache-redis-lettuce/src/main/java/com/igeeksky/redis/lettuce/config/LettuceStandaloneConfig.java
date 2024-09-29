package com.igeeksky.redis.lettuce.config;

import com.igeeksky.redis.RedisNode;
import com.igeeksky.redis.lettuce.props.Lettuce;
import com.igeeksky.xtool.core.json.SimpleJSON;
import io.lettuce.core.ReadFrom;

import java.util.List;

/**
 * Lettuce Standalone 配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
public final class LettuceStandaloneConfig extends LettuceGenericConfig {

    private RedisNode node;

    private ReadFrom readFrom = ReadFrom.UPSTREAM;

    private List<RedisNode> nodes;

    private Lettuce.ClientOptions clientOptions;

    /**
     * 默认构造函数
     */
    public LettuceStandaloneConfig() {
    }

    /**
     * 获取： Redis 节点信息
     *
     * @return {@link RedisNode} – Redis 节点信息
     */
    public RedisNode getNode() {
        return node;
    }

    /**
     * 设置： Redis 节点信息
     *
     * @param node Redis 节点信息
     */
    public void setNode(RedisNode node) {
        this.node = node;
    }

    /**
     * 获取：读写分离策略
     *
     * @return {@link ReadFrom} – 读写分离策略
     */
    public ReadFrom getReadFrom() {
        return readFrom;
    }

    /**
     * 设置：读写分离策略
     *
     * @param readFrom 读写分离策略
     */
    public void setReadFrom(ReadFrom readFrom) {
        this.readFrom = readFrom;
    }

    /**
     * 获取： Redis 节点列表
     *
     * @return {@link List<RedisNode>} – Redis 节点列表
     */
    public List<RedisNode> getNodes() {
        return nodes;
    }

    /**
     * 设置： Redis 节点列表
     *
     * @param nodes Redis 节点列表
     */
    public void setNodes(List<RedisNode> nodes) {
        this.nodes = nodes;
    }

    /**
     * 获取：客户端配置
     *
     * @return {@link Lettuce.ClientOptions} – 客户端配置
     */
    public Lettuce.ClientOptions getClientOptions() {
        return clientOptions;
    }

    /**
     * 设置：客户端配置
     *
     * @param clientOptions 客户端配置
     */
    public void setClientOptions(Lettuce.ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

    /**
     * 重写：toString
     *
     * @return {@link String} – JSON 格式的字符串
     */
    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}