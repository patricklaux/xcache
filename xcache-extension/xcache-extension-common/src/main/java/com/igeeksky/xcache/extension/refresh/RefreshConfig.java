package com.igeeksky.xcache.extension.refresh;

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

    private final String sid;

    private final String name;

    private final String group;

    private final String provider;

    private final Charset charset;

    private final String refreshKey;

    private final String refreshLockKey;

    private final String refreshPeriodKey;

    private final int refreshThreadPeriod;

    private final int refreshSequenceSize;

    private final int refreshTasksSize;

    private final int refreshAfterWrite;

    private final boolean enableGroupPrefix;

    private final Map<String, Object> params;

    public RefreshConfig(Builder builder) {
        this.name = builder.name;
        this.group = builder.group;
        this.charset = builder.charset;
        this.provider = builder.provider;
        this.sid = builder.sid;
        this.refreshThreadPeriod = builder.refreshThreadPeriod;
        this.refreshTasksSize = builder.refreshTasksSize;
        this.refreshAfterWrite = builder.refreshAfterWrite;
        this.refreshSequenceSize = builder.refreshSequenceSize;
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

    public long getRefreshThreadPeriod() {
        return refreshThreadPeriod;
    }

    public int getRefreshTasksSize() {
        return refreshTasksSize;
    }

    public int getRefreshAfterWrite() {
        return refreshAfterWrite;
    }

    public int getRefreshSequenceSize() {
        return refreshSequenceSize;
    }

    public String getSid() {
        return sid;
    }

    public boolean isEnableGroupPrefix() {
        return enableGroupPrefix;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String sid;

        private String name;

        private String group;

        private String provider;

        private Charset charset;

        private int refreshTasksSize;

        private int refreshAfterWrite;

        private int refreshSequenceSize;

        private int refreshThreadPeriod;

        private boolean enableGroupPrefix;

        private final Map<String, Object> params = new HashMap<>();

        public Builder sid(String sid) {
            this.sid = sid;
            return this;
        }

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

        public Builder refreshAfterWrite(int refreshAfterWrite) {
            this.refreshAfterWrite = refreshAfterWrite;
            return this;
        }

        public Builder refreshTasksSize(int refreshTasksSize) {
            this.refreshTasksSize = refreshTasksSize;
            return this;
        }

        public Builder refreshThreadPeriod(int refreshThreadPeriod) {
            this.refreshThreadPeriod = refreshThreadPeriod;
            return this;
        }

        public Builder refreshSequenceSize(int refreshSequenceSize) {
            this.refreshSequenceSize = refreshSequenceSize;
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