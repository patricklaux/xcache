package com.igeeksky.xcache.extension.refresh;

import com.igeeksky.xcache.extension.lock.LockService;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/26
 */
public class RefreshConfig {

    /**
     * 缓存名称
     */
    private final String name;

    private final String app;

    private final String infix;

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

    public RefreshConfig(Builder builder) {
        this.name = builder.name;
        this.app = builder.app;
        this.provider = builder.provider;
        this.charset = builder.charset;
        this.cacheLock = builder.cacheLock;
        this.period = builder.period;
        this.stopAfterAccess = builder.stopAfterAccess;
        if (builder.infix != null) {
            if (Objects.equals("none", StringUtils.toLowerCase(builder.infix))) {
                this.infix = null;
                this.refreshKey = "refresh:" + this.name;
                this.refreshLockKey = "refresh:lock:" + this.name;
                this.refreshPeriodKey = "refresh:period:" + this.name;
                return;
            }
            this.infix = builder.infix;
        } else {
            this.infix = this.app;
        }
        this.refreshKey = "refresh:" + this.infix + ":" + this.name;
        this.refreshLockKey = "refresh:lock:" + this.infix + ":" + this.name;
        this.refreshPeriodKey = "refresh:period:" + this.infix + ":" + this.name;
    }

    public String getName() {
        return name;
    }

    public String getApp() {
        return app;
    }

    public String getInfix() {
        return infix;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        /**
         * 缓存名称
         */
        private String name;

        private String app;

        private String infix;

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

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder app(String app) {
            this.app = app;
            return this;
        }

        public Builder infix(String infix) {
            this.infix = infix;
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

        public RefreshConfig build() {
            return new RefreshConfig(this);
        }

    }

}