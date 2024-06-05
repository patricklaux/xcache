package com.igeeksky.xcache.config;

import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.StringUtils;

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
        this.host = StringUtils.trim(hp[0]);
        Assert.hasLength(this.host, () -> "node:[" + node + "] host must not be null or empty.");
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
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof HostAndPort)) return false;

        HostAndPort that = (HostAndPort) object;

        if (getPort() != that.getPort()) return false;
        return getHost().equals(that.getHost());
    }

    @Override
    public int hashCode() {
        int result = getHost().hashCode();
        result = 31 * result + getPort();
        return result;
    }
}