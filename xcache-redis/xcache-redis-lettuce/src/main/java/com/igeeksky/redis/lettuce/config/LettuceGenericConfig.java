package com.igeeksky.redis.lettuce.config;

import io.lettuce.core.SslVerifyMode;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class LettuceGenericConfig {

    private String id;

    private Charset charset = StandardCharsets.UTF_8;

    private int database = 0;

    private String username;

    private String password;

    private String clientName;

    private long timeout = 60000;

    private boolean ssl = false;

    private boolean startTls = false;

    private SslVerifyMode sslVerifyMode = SslVerifyMode.NONE;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

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

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public boolean isStartTls() {
        return startTls;
    }

    public void setStartTls(boolean startTls) {
        this.startTls = startTls;
    }

    public SslVerifyMode getSslVerifyMode() {
        return sslVerifyMode;
    }

    public void setSslVerifyMode(SslVerifyMode sslVerifyMode) {
        this.sslVerifyMode = sslVerifyMode;
    }

}
