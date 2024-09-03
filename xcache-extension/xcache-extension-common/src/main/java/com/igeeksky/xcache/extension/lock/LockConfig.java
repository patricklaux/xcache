package com.igeeksky.xcache.extension.lock;

import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 锁配置信息
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/22
 */
public class LockConfig {

    private static final String PREFIX = "lock:";

    private final String sid;

    /**
     * 缓存名称
     */
    private final String name;

    private final Charset charset;

    private final String infix;

    private final String prefix;

    private final String provider;

    private final long leaseTime;

    private final int initialCapacity;

    private final Map<String, Object> params;

    private LockConfig(Builder builder) {
        this.sid = builder.sid;
        this.name = builder.name;
        this.charset = builder.charset;
        this.provider = builder.provider;
        this.initialCapacity = builder.initialCapacity;
        this.leaseTime = builder.leaseTime;
        this.params = builder.params;
        if (Objects.equals("none", StringUtils.toLowerCase(builder.infix))) {
            this.infix = null;
            this.prefix = PREFIX + this.name + ":";
        } else {
            this.infix = builder.infix;
            this.prefix = PREFIX + this.infix + ":" + this.name + ":";
        }
    }

    public String getSid() {
        return sid;
    }

    public String getName() {
        return name;
    }

    public Charset getCharset() {
        return charset;
    }

    public String getInfix() {
        return infix;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getProvider() {
        return provider;
    }

    public int getInitialCapacity() {
        return initialCapacity;
    }

    public long getLeaseTime() {
        return leaseTime;
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
        private Charset charset;
        private String infix;
        private String provider;
        private int initialCapacity;
        private long leaseTime;
        private final Map<String, Object> params = new HashMap<>();

        private Builder() {
        }

        public Builder sid(String sid) {
            Assert.notNull(sid, "sid must not be null");
            this.sid = sid;
            return this;
        }

        public Builder name(String name) {
            Assert.notNull(name, "name must not be null");
            this.name = name;
            return this;
        }

        public Builder charset(Charset charset) {
            Assert.notNull(charset, "charset must not be null");
            this.charset = charset;
            return this;
        }

        /**
         * RedisLock 的 key 是四段结构：<p>
         * {@code "lock:" + infix + ":" + name + ":" + key}
         *
         * @param infix RedisLockKey 的中缀
         * @return {@code this}
         */
        public Builder infix(String infix) {
            Assert.notNull(infix, "infix must not be null");
            this.infix = infix;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder initialCapacity(int initialCapacity) {
            this.initialCapacity = initialCapacity;
            return this;
        }

        public Builder leaseTime(long leaseTime) {
            Assert.isTrue(leaseTime > 0, () -> "leaseTime:[" + leaseTime + "] must be greater than 0");
            this.leaseTime = leaseTime;
            return this;
        }

        public Builder params(Map<String, Object> params) {
            if (params != null) {
                this.params.putAll(params);
            }
            return this;
        }

        public LockConfig build() {
            return new LockConfig(this);
        }

    }

}