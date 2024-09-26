package com.igeeksky.xcache.extension.sync;


import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.props.SyncType;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/7
 */
public class SyncConfig<V> {

    private static final String PREFIX = "sync:";

    private final String sid;

    private final String name;

    private final String group;

    private final String channel;

    private final long maxLen;

    private final String provider;

    private final Charset charset;

    private final boolean enableGroupPrefix;

    private final SyncType first;

    private final SyncType second;

    private final Store<V> firstStore;

    private final Store<V> secondStore;

    private final Map<String, Object> params;

    public SyncConfig(Builder<V> builder) {
        this.sid = builder.sid;
        this.name = builder.name;
        this.group = builder.group;
        this.maxLen = builder.maxLen;
        this.charset = builder.charset;
        this.provider = builder.provider;
        this.enableGroupPrefix = builder.enableGroupPrefix;
        this.first = builder.first;
        this.second = builder.second;
        this.firstStore = builder.firstStore;
        this.secondStore = builder.secondStore;
        this.params = builder.params;
        if (this.enableGroupPrefix) {
            this.channel = PREFIX + this.group + ":" + this.name;
        } else {
            this.channel = PREFIX + this.name;
        }
    }

    public String getSid() {
        return sid;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getChannel() {
        return channel;
    }

    public long getMaxLen() {
        return maxLen;
    }

    public String getProvider() {
        return provider;
    }

    public Charset getCharset() {
        return charset;
    }

    public boolean isEnableGroupPrefix() {
        return enableGroupPrefix;
    }

    public SyncType getFirst() {
        return first;
    }

    public SyncType getSecond() {
        return second;
    }

    public Store<V> getFirstStore() {
        return firstStore;
    }

    public Store<V> getSecondStore() {
        return secondStore;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public static <V> Builder<V> builder(Store<V> firstStore, Store<V> secondStore) {
        return new Builder<>(firstStore, secondStore);
    }

    public static class Builder<V> {
        private String sid;
        private String name;
        private String group;
        private long maxLen;
        private String provider;
        private Charset charset;
        private boolean enableGroupPrefix;
        private SyncType first;
        private SyncType second;
        private final Store<V> firstStore;
        private final Store<V> secondStore;
        private final Map<String, Object> params = new HashMap<>();

        private Builder(Store<V> firstStore, Store<V> secondStore) {
            this.firstStore = firstStore;
            this.secondStore = secondStore;
        }

        public Builder<V> sid(String sid) {
            this.sid = sid;
            return this;
        }

        public Builder<V> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<V> group(String group) {
            this.group = group;
            return this;
        }

        public Builder<V> maxLen(long maxLen) {
            this.maxLen = maxLen;
            return this;
        }

        public Builder<V> provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder<V> charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder<V> first(SyncType first) {
            this.first = first;
            return this;
        }

        public Builder<V> second(SyncType second) {
            this.second = second;
            return this;
        }

        public Builder<V> params(Map<String, Object> params) {
            if (params != null) {
                this.params.putAll(params);
            }
            return this;
        }

        public Builder<V> enableGroupPrefix(Boolean enableGroupPrefix) {
            if (enableGroupPrefix != null) {
                this.enableGroupPrefix = enableGroupPrefix;
            }
            return this;
        }

        public SyncConfig<V> build() {
            return new SyncConfig<>(this);
        }

    }

}