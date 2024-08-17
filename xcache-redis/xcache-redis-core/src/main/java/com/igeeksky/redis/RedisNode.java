package com.igeeksky.redis;

import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
public class RedisNode {

    private final String host;

    private final int port;

    private final String socket;

    public RedisNode(String node) {
        String[] array = node.split(":");
        Assert.isTrue(array.length == 2, () -> "node:[" + node + "] is not validate.");

        String first = StringUtils.trimToNull(array[0]);
        if (Objects.equals("socket", first)) {
            this.socket = array[1];
            Assert.notNull(this.socket, () -> "node:[" + node + "] socket must not be null or empty.");
            this.host = null;
            this.port = -1;
        } else {
            this.host = first;
            Assert.notNull(this.host, () -> "node:[" + node + "] host must not be null or empty.");
            this.port = Integer.parseInt(array[1]);
            this.socket = null;
        }
    }

    public RedisNode(String host, int port, String socket) {
        this.host = host;
        this.port = port;
        this.socket = socket;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getSocket() {
        return socket;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RedisNode node)) return false;

        return getPort() == node.getPort() && Objects.equals(getHost(), node.getHost()) && Objects.equals(socket, node.socket);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getHost());
        result = 31 * result + getPort();
        result = 31 * result + Objects.hashCode(socket);
        return result;
    }
}