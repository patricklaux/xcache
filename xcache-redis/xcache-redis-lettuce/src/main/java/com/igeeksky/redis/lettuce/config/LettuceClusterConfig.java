package com.igeeksky.redis.lettuce.config;

import com.igeeksky.redis.RedisNode;
import com.igeeksky.redis.lettuce.config.props.Lettuce;
import io.lettuce.core.ReadFrom;

import java.util.List;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
public class LettuceClusterConfig extends LettuceGenericConfig {

    private ReadFrom readFrom;

    private List<RedisNode> nodes;

    private Lettuce.ClusterClientOptions clientOptions;

    public ReadFrom getReadFrom() {
        return readFrom;
    }

    public void setReadFrom(ReadFrom readFrom) {
        this.readFrom = readFrom;
    }

    public List<RedisNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<RedisNode> nodes) {
        this.nodes = nodes;
    }

    public Lettuce.ClusterClientOptions getClientOptions() {
        return clientOptions;
    }

    public void setClientOptions(Lettuce.ClusterClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

}