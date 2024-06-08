package com.igeeksky.xcache.redis.lettuce.config;

import com.igeeksky.xcache.core.config.HostAndPort;
import com.igeeksky.xcache.redis.lettuce.config.props.Lettuce;
import io.lettuce.core.ReadFrom;

import java.util.List;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
public class LettuceStandaloneConfig extends LettuceGenericConfig {

    // TODO 添加 toString()方法

    private HostAndPort master = new HostAndPort("localhost", 6379);

    private ReadFrom readFrom = ReadFrom.UPSTREAM;

    private List<HostAndPort> replicas;

    private Lettuce.ClientOptions clientOptions;

    public HostAndPort getMaster() {
        return master;
    }

    public void setMaster(HostAndPort master) {
        this.master = master;
    }

    public ReadFrom getReadFrom() {
        return readFrom;
    }

    public void setReadFrom(ReadFrom readFrom) {
        this.readFrom = readFrom;
    }

    public List<HostAndPort> getReplicas() {
        return replicas;
    }

    public void setReplicas(List<HostAndPort> replicas) {
        this.replicas = replicas;
    }

    public Lettuce.ClientOptions getClientOptions() {
        return clientOptions;
    }

    public void setClientOptions(Lettuce.ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

}