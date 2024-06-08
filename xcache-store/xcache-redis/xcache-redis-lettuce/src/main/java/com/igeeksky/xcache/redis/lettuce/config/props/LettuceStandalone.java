package com.igeeksky.xcache.redis.lettuce.config.props;

import java.util.List;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-05
 */
public class LettuceStandalone extends LettuceGeneric {

    private String master;
    private String readFrom;
    private List<String> replicas;
    private Lettuce.ClientOptions clientOptions;

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getReadFrom() {
        return readFrom;
    }

    public void setReadFrom(String readFrom) {
        this.readFrom = readFrom;
    }

    public List<String> getReplicas() {
        return replicas;
    }

    public void setReplicas(List<String> replicas) {
        this.replicas = replicas;
    }

    public Lettuce.ClientOptions getClientOptions() {
        return clientOptions;
    }

    public void setClientOptions(Lettuce.ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

}