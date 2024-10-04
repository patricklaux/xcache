package com.igeeksky.redis.lettuce.props;

/**
 * Lettuce 通用配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-06
 */
public sealed class LettuceGeneric permits LettuceStandalone, LettuceSentinel, LettuceCluster {

    private String username;
    private String password;
    private int database = 0;
    private String clientName;
    private Boolean ssl;
    private Boolean startTls;
    private String sslVerifyMode;
    private Long timeout;

    /**
     * 默认构造函数
     */
    public LettuceGeneric() {
    }

    /**
     * Redis 用户名
     *
     * @return {@link String} – 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置：Redis 用户名
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Redis 密码
     *
     * @return {@link String} – 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置：Redis 密码
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Redis 数据库索引
     * <p>
     * 默认值：0
     *
     * @return {@code int} – 数据库索引
     */
    public int getDatabase() {
        return database;
    }

    /**
     * 设置：Redis 数据库索引
     *
     * @param database 数据库索引
     */
    public void setDatabase(int database) {
        this.database = database;
    }

    /**
     * Redis 客户端名称
     *
     * @return {@link String} – 客户端名称
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * 设置：Redis 客户端名称
     *
     * @param clientName 客户端名称
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * 是否启用 SSL
     * <p>
     * 默认值：false
     *
     * @return {@link Boolean} – 是否启用 SSL
     */
    public Boolean getSsl() {
        return ssl;
    }

    /**
     * 设置：是否启用 SSL
     *
     * @param ssl 是否启用 SSL
     */
    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

    /**
     * 是否启用 TLS
     * <p>
     * 默认值：false
     *
     * @return {@link Boolean} – 是否启用 TLS
     */
    public Boolean getStartTls() {
        return startTls;
    }

    /**
     * 设置：是否启用 TLS
     *
     * @param startTls 是否启用 TLS
     */
    public void setStartTls(Boolean startTls) {
        this.startTls = startTls;
    }

    /**
     * SSL 验证模式
     * <p>
     * 默认值：FULL
     * <p>
     * 可选值：FULL、NONE、CA
     *
     * @return {@link String} – SSL 验证模式
     * @see io.lettuce.core.SslVerifyMode
     */
    public String getSslVerifyMode() {
        return sslVerifyMode;
    }

    /**
     * 设置：SSL 验证模式
     *
     * @param sslVerifyMode SSL 验证模式
     * @see io.lettuce.core.SslVerifyMode
     */
    public void setSslVerifyMode(String sslVerifyMode) {
        this.sslVerifyMode = sslVerifyMode;
    }

    /**
     * 同步执行命令等待完成的最大时长
     * <p>
     * 默认值：60000  单位：毫秒
     * <p>
     * 如需执行类似于 mset、mget、hmget、hmset……等批处理命令，
     * 且单次操作的数据量大，则需结合网络情况，配置更大的值。
     *
     * @return {@link Long} – 同步执行命令等待完成的最大时长
     */
    public Long getTimeout() {
        return timeout;
    }

    /**
     * 设置：同步执行命令等待完成的最大时长
     *
     * @param timeout 同步执行命令等待完成的最大时长
     */
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

}
