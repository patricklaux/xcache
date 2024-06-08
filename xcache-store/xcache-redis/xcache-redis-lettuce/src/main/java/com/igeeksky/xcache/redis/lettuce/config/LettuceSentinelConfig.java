package com.igeeksky.xcache.redis.lettuce.config;

import com.igeeksky.xcache.core.config.HostAndPort;
import com.igeeksky.xcache.redis.lettuce.config.props.Lettuce;
import io.lettuce.core.ReadFrom;

import java.util.List;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
public class LettuceSentinelConfig extends LettuceGenericConfig {

    private ReadFrom readFrom = ReadFrom.UPSTREAM;

    private String masterId;

    private String sentinelUsername;

    private String sentinelPassword;

    private List<HostAndPort> sentinels;

    private Lettuce.ClientOptions clientOptions;

    public ReadFrom getReadFrom() {
        return readFrom;
    }

    public void setReadFrom(ReadFrom readFrom) {
        this.readFrom = readFrom;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
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

    public List<HostAndPort> getSentinels() {
        return sentinels;
    }

    public void setSentinels(List<HostAndPort> sentinels) {
        this.sentinels = sentinels;
    }

    public Lettuce.ClientOptions getClientOptions() {
        return clientOptions;
    }

    public void setClientOptions(Lettuce.ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

}
