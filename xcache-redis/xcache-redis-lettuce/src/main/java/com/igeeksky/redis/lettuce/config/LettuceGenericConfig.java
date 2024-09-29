package com.igeeksky.redis.lettuce.config;

import io.lettuce.core.SslVerifyMode;

/**
 * Lettuce 通用配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public sealed class LettuceGenericConfig permits LettuceStandaloneConfig, LettuceSentinelConfig, LettuceClusterConfig {

    private String id;

    private int database = 0;

    private String username;

    private String password;

    private String clientName;

    private long timeout = 60000;

    private boolean ssl = false;

    private boolean startTls = false;

    private SslVerifyMode sslVerifyMode = SslVerifyMode.NONE;

    /**
     * 默认构造函数
     */
    public LettuceGenericConfig() {
    }


    /**
     * 获取：RedisOperator 唯一标识
     *
     * @return {@link String} – 唯一标识
     */
    public String getId() {
        return id;
    }

    /**
     * 设置：RedisOperator 唯一标识
     *
     * @param id RedisOperator 唯一标识
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取：数据库索引
     * <p>
     * 默认为 0
     *
     * @return {@code int} – 数据库索引
     */
    public int getDatabase() {
        return database;
    }

    /**
     * 设置：数据库索引
     * <p>
     * 默认为 0
     *
     * @param database 数据库索引
     */
    public void setDatabase(int database) {
        this.database = database;
    }

    /**
     * 获取：用户名
     * <p>
     * 默认为 null
     *
     * @return {@link String} – 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置：用户名
     * <p>
     * 默认为 null
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取：密码
     * <p>
     * 默认为 null
     *
     * @return {@link String} – 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置：密码
     * <p>
     * 默认为 null
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取：客户端名称
     * <p>
     * 默认为 null
     *
     * @return {@link String} – 客户端名称
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * 设置：客户端名称
     * <p>
     * 默认为 null
     *
     * @param clientName 客户端名称
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * 获取：超时时间，单位：毫秒
     * <p>
     * 默认为 60000
     *
     * @return {@code long} – 超时时间，单位：毫秒
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * 设置：超时时间，单位：毫秒
     * <p>
     * 默认为 60000
     *
     * @param timeout 超时时间，单位：毫秒
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * 获取：是否启用 SSL
     * <p>
     * 默认为 false
     *
     * @return {@code boolean} – 是否启用 SSL
     */
    public boolean isSsl() {
        return ssl;
    }

    /**
     * 设置：是否启用 SSL
     * <p>
     * 默认为 false
     *
     * @param ssl 是否启用 SSL
     */
    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    /**
     * 获取：是否启用 StartTLS
     * <p>
     * 默认为 false
     *
     * @return {@code boolean} – 是否启用 StartTLS
     */
    public boolean isStartTls() {
        return startTls;
    }

    /**
     * 设置：是否启用 StartTLS
     * <p>
     * 默认为 false
     *
     * @param startTls 是否启用 StartTLS
     */
    public void setStartTls(boolean startTls) {
        this.startTls = startTls;
    }

    /**
     * 获取：SSL 验证模式
     * <p>
     * 默认为 NONE
     *
     * @return {@link SslVerifyMode} – SSL 验证模式
     */
    public SslVerifyMode getSslVerifyMode() {
        return sslVerifyMode;
    }

    /**
     * 设置：SSL 验证模式
     * <p>
     * 默认为 NONE
     *
     * @param sslVerifyMode SSL 验证模式
     */
    public void setSslVerifyMode(SslVerifyMode sslVerifyMode) {
        this.sslVerifyMode = sslVerifyMode;
    }

}
