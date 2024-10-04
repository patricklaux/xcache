package com.igeeksky.redis;

import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.util.Objects;

/**
 * Redis节点
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
public class RedisNode {

    private final String host;

    private final int port;

    private final String socket;

    /**
     * 构造 Redis节点
     *
     * @param node 格式：{@code host:port} 或 {@code socket:/path/to/socket}
     */
    public RedisNode(String node) {
        String[] array = node.split(":");
        Assert.isTrue(array.length == 2, () -> "node:[" + node + "] is not valid.");

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

    /**
     * 构造 Redis节点
     *
     * @param host   主机名
     * @param port   端口号
     * @param socket Unix域套接字文件路径
     */
    public RedisNode(String host, int port, String socket) {
        this.host = host;
        this.port = port;
        this.socket = socket;
    }

    /**
     * 获取主机名
     *
     * @return {@link String} – 主机名
     */
    public String getHost() {
        return host;
    }

    /**
     * 获取端口号
     *
     * @return {@link Integer} – 端口号
     */
    public int getPort() {
        return port;
    }

    /**
     * 获取 Unix域套接字文件路径
     *
     * @return {@link String} – Unix域套接字文件路径
     */
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