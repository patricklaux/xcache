package com.igeeksky.xcache.extension.refresh;

import com.igeeksky.xcache.extension.lock.LockService;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 刷新配置信息
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/26
 */
public class RefreshConfig {

    private final String name;

    private final String group;

    private final boolean enableGroupPrefix;

    private final String provider;

    private final Charset charset;

    private final String refreshKey;

    private final String refreshLockKey;

    private final LockService cacheLock;

    private final long refreshPeriod;

    private final Map<String, Object> params;

    public RefreshConfig(Builder builder) {
        this.name = builder.name;
        this.group = builder.group;
        this.charset = builder.charset;
        this.provider = builder.provider;
        this.cacheLock = builder.cacheLock;
        this.refreshPeriod = builder.refreshPeriod;
        this.enableGroupPrefix = builder.enableGroupPrefix;
        this.params = builder.params;
        if (this.enableGroupPrefix) {
            this.refreshKey = "refresh:" + this.group + ":" + this.name;
            this.refreshLockKey = "refresh:lock:" + this.group + ":" + this.name;
        } else {
            this.refreshKey = "refresh:" + this.name;
            this.refreshLockKey = "refresh:lock:" + this.name;
        }
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public boolean isEnableGroupPrefix() {
        return enableGroupPrefix;
    }

    public String getProvider() {
        return provider;
    }

    public Charset getCharset() {
        return charset;
    }

    public String getRefreshKey() {
        return refreshKey;
    }

    public String getRefreshLockKey() {
        return refreshLockKey;
    }

    public LockService getCacheLock() {
        return cacheLock;
    }

    public long getRefreshPeriod() {
        return refreshPeriod;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getRefreshAfterWrite() {
        return 100000L;
    }

    public int getMaxRefreshTasks() {
        return 10000;
    }

    public static class Builder {

        private String name;

        private String group;

        private Charset charset;

        private String provider;

        private LockService cacheLock;

        private long refreshPeriod;

        private boolean enableGroupPrefix;

        private final Map<String, Object> params = new HashMap<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder group(String group) {
            this.group = group;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder refreshPeriod(long refreshPeriod) {
            this.refreshPeriod = refreshPeriod;
            return this;
        }

        public Builder cacheLock(LockService cacheLock) {
            this.cacheLock = cacheLock;
            return this;
        }

        public Builder enableGroupPrefix(boolean enableGroupPrefix) {
            this.enableGroupPrefix = enableGroupPrefix;
            return this;
        }

        public Builder params(Map<String, Object> params) {
            if (params != null) {
                this.params.putAll(params);
            }
            return this;
        }

        public RefreshConfig build() {
            return new RefreshConfig(this);
        }

    }

}