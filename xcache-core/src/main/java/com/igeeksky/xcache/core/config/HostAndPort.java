package com.igeeksky.xcache.core.config;

import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
public class HostAndPort {

    private final String host;

    private final int port;

    public HostAndPort(String node) {
        String[] hp = node.split(":");
        Assert.isTrue(hp.length == 2, () -> "node:[" + node + "] can't convert to HostAndPort.");
        this.host = StringUtils.trimToNull(hp[0]);
        Assert.notNull(this.host, () -> "node:[" + node + "] host must not be null or empty.");
        this.port = Integer.parseInt(hp[1]);
    }

    public HostAndPort(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HostAndPort that)) return false;

        return getPort() == that.getPort() && Objects.equals(getHost(), that.getHost());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getHost());
        result = 31 * result + getPort();
        return result;
    }
}