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

    private final String group;

    /**
     * 缓存名称
     */
    private final String name;

    private final boolean enableGroupPrefix;

    private final String provider;

    /**
     * 字符集
     */
    private final Charset charset;

    private final String refreshKey;

    private final String refreshLockKey;

    private final String refreshPeriodKey;

    private final LockService cacheLock;

    /**
     * 刷新间隔
     */
    private final long period;

    /**
     * 停止刷新时间
     */
    private final long stopAfterAccess;

    private final Map<String, Object> params;

    public RefreshConfig(Builder builder) {
        this.group = builder.group;
        this.name = builder.name;
        this.provider = builder.provider;
        this.charset = builder.charset;
        this.cacheLock = builder.cacheLock;
        this.period = builder.period;
        this.stopAfterAccess = builder.stopAfterAccess;
        this.enableGroupPrefix = builder.enableGroupPrefix;
        this.params = builder.params;
        if (this.enableGroupPrefix) {
            this.refreshKey = "refresh:" + this.group + ":" + this.name;
            this.refreshLockKey = "refresh:lock:" + this.group + ":" + this.name;
            this.refreshPeriodKey = "refresh:period:" + this.group + ":" + this.name;
        } else {
            this.refreshKey = "refresh:" + this.name;
            this.refreshLockKey = "refresh:lock:" + this.name;
            this.refreshPeriodKey = "refresh:period:" + this.name;
        }
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public boolean getEnableGroupPrefix() {
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

    public String getRefreshPeriodKey() {
        return refreshPeriodKey;
    }

    public LockService getCacheLock() {
        return cacheLock;
    }

    public long getPeriod() {
        return period;
    }

    public long getStopAfterAccess() {
        return stopAfterAccess;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String group;

        /**
         * 缓存名称
         */
        private String name;

        private String provider;

        private Charset charset;

        private LockService cacheLock;

        /**
         * 刷新间隔
         */
        private long period;

        /**
         * 停止刷新时间
         */
        private long stopAfterAccess;

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

        public Builder period(long period) {
            this.period = period;
            return this;
        }

        public Builder stopAfterAccess(long stopAfterAccess) {
            this.stopAfterAccess = stopAfterAccess;
            return this;
        }

        public Builder cacheLock(LockService cacheLock) {
            this.cacheLock = cacheLock;
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

        public Builder enableGroupPrefix(Boolean enableGroupPrefix) {
            if (enableGroupPrefix != null) {
                this.enableGroupPrefix = enableGroupPrefix;
            }
            return this;
        }
    }

}