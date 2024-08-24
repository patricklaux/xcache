package com.igeeksky.redis.lettuce.config.props;

/**
 * Lettuce 通用配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-06
 */
public class LettuceGeneric {

    private String username;
    private String password;
    private int database = 0;
    private String clientName;
    private Boolean ssl;
    private Boolean startTls;
    private String sslVerifyMode;

    /**
     * 用于命令执行完成的超时配置
     * <p>
     * 默认值：60000<p>
     * 单位：毫秒<p>
     * 如果需要执行类似于 mset、mget、hmget、hmset……等批处理命令，
     * 而且单次操作的数据量大，则需要结合网络情况，配置更大的超时。
     */
    private Long timeout;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

    public Boolean getStartTls() {
        return startTls;
    }

    public void setStartTls(Boolean startTls) {
        this.startTls = startTls;
    }

    public String getSslVerifyMode() {
        return sslVerifyMode;
    }

    public void setSslVerifyMode(String sslVerifyMode) {
        this.sslVerifyMode = sslVerifyMode;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
}
