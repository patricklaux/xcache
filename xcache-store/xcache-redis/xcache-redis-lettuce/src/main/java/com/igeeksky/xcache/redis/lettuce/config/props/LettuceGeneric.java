package com.igeeksky.xcache.redis.lettuce.config.props;

/**
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
