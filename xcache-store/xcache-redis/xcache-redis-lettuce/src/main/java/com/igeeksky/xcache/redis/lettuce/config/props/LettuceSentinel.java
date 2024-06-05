package com.igeeksky.xcache.redis.lettuce.config.props;

import java.util.List;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
public class LettuceSentinel extends LettuceGeneric {

    private String masterId;
    private List<String> nodes;
    private String readFrom;
    private String sentinelUsername;
    private String sentinelPassword;
    private Lettuce.ClientOptions clientOptions;

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public String getReadFrom() {
        return readFrom;
    }

    public void setReadFrom(String readFrom) {
        this.readFrom = readFrom;
    }

    public String getSentinelUsername() {
        return sentinelUsername;
    }

    public void setSentinelUsername(String sentinelUsername) {
        this.sentinelUsername = sentinelUsername;
    }

    public String getSentinelPassword() {
        return sentinelPassword;
    }

    public void setSentinelPassword(String sentinelPassword) {
        this.sentinelPassword = sentinelPassword;
    }

    public Lettuce.ClientOptions getClientOptions() {
        return clientOptions;
    }

    public void setClientOptions(Lettuce.ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

}
