package com.igeeksky.xcache.extension.sync;


import com.igeeksky.xcache.common.Store;
import com.igeeksky.xcache.props.SyncType;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/7
 */
public class SyncConfig<V> {

    private static final String PREFIX = "sync:";

    private final String sid;

    private final String name;

    private final String application;

    private final String infix;

    private final String channel;

    private final long maxLen;

    private final String provider;

    private final Charset charset;

    private final SyncType first;

    private final SyncType second;

    private final Store<V> firstStore;

    private final Store<V> secondStore;

    private final Map<String, Object> params;

    public SyncConfig(Builder<V> builder) {
        this.sid = builder.sid;
        this.name = builder.name;
        this.application = builder.application;
        this.maxLen = builder.maxLen;
        this.provider = builder.provider;
        this.charset = builder.charset;
        this.first = builder.first;
        this.second = builder.second;
        this.firstStore = builder.firstStore;
        this.secondStore = builder.secondStore;
        this.params = builder.params;
        if (builder.infix != null) {
            if (Objects.equals("none", StringUtils.toLowerCase(builder.infix))) {
                this.infix = null;
                this.channel = PREFIX + this.name;
            } else {
                this.infix = builder.infix;
                this.channel = PREFIX + this.infix + ":" + this.name;
            }
        } else {
            this.infix = this.application;
            this.channel = PREFIX + this.infix + ":" + this.name;
        }
    }

    public String getSid() {
        return sid;
    }

    public String getName() {
        return name;
    }

    public String getApplication() {
        return application;
    }

    public String getInfix() {
        return infix;
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
        private String application;
        private String infix;
        private long maxLen;
        private String provider;
        private Charset charset;
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

        public Builder<V> application(String application) {
            this.application = application;
            return this;
        }

        public Builder<V> infix(String infix) {
            this.infix = infix;
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

        public SyncConfig<V> build() {
            return new SyncConfig<>(this);
        }
    }

}