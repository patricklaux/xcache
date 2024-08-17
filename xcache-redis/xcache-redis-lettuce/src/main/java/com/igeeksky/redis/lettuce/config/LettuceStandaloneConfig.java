package com.igeeksky.redis.lettuce.config;

import com.igeeksky.redis.RedisNode;
import com.igeeksky.redis.lettuce.config.props.Lettuce;
import com.igeeksky.xtool.core.json.SimpleJSON;
import io.lettuce.core.ReadFrom;

import java.util.List;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
public class LettuceStandaloneConfig extends LettuceGenericConfig {

    private RedisNode node;

    private ReadFrom readFrom = ReadFrom.UPSTREAM;

    private List<RedisNode> nodes;

    private Lettuce.ClientOptions clientOptions;

    public RedisNode getNode() {
        return node;
    }

    public void setNode(RedisNode node) {
        this.node = node;
    }

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

    public Lettuce.ClientOptions getClientOptions() {
        return clientOptions;
    }

    public void setClientOptions(Lettuce.ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }
}