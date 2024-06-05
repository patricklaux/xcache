package com.igeeksky.xcache.redis.lettuce.config;

import com.igeeksky.xcache.config.HostAndPort;
import com.igeeksky.xcache.redis.lettuce.config.props.Lettuce;
import io.lettuce.core.ReadFrom;

import java.util.List;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
public class LettuceClusterConfig extends LettuceGenericConfig {

    private ReadFrom readFrom;

    private List<HostAndPort> nodes;

    private Lettuce.ClusterClientOptions clientOptions;

    public ReadFrom getReadFrom() {
        return readFrom;
    }

    public void setReadFrom(ReadFrom readFrom) {
        this.readFrom = readFrom;
    }

    public List<HostAndPort> getNodes() {
        return nodes;
    }

    public void setNodes(List<HostAndPort> nodes) {
        this.nodes = nodes;
    }

    public Lettuce.ClusterClientOptions getClientOptions() {
        return clientOptions;
    }

    public void setClientOptions(Lettuce.ClusterClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

}