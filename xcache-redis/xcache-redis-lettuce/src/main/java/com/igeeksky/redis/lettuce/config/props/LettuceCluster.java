package com.igeeksky.redis.lettuce.config.props;

import java.util.List;
import java.util.StringJoiner;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
public class LettuceCluster extends LettuceGeneric {

    private String readFrom;
    private List<String> nodes;
    private Lettuce.ClusterClientOptions clientOptions;

    public String getReadFrom() {
        return readFrom;
    }

    public void setReadFrom(String readFrom) {
        this.readFrom = readFrom;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public Lettuce.ClusterClientOptions getClientOptions() {
        return clientOptions;
    }

    public void setClientOptions(Lettuce.ClusterClientOptions clientOptions) {
        this.clientOptions = clientOptions;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "{", "}")
                .add("readFrom='" + readFrom + "'")
                .add("nodes=" + nodes)
                .add("clientOptions=" + clientOptions)
                .add("username='" + getUsername() + "'")
                .add("password='" + getPassword() + "'")
                .add("database=" + getDatabase())
                .add("clientName='" + getClientName() + "'")
                .add("ssl=" + getSsl())
                .add("startTls=" + getStartTls())
                .add("sslVerifyMode='" + getSslVerifyMode() + "'")
                .add("timeout=" + getTimeout())
                .toString();
    }

}