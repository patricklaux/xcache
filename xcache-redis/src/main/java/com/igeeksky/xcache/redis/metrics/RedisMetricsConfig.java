package com.igeeksky.xcache.redis.metrics;

import com.igeeksky.xredis.common.stream.StreamOperator;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/19
 */
public class RedisMetricsConfig {

    private final RedisCacheMetricsCodec codec;

    private final long maxLen;

    private final long period;

    private final String group;

    private final boolean enableGroupPrefix;

    private final StreamOperator<byte[], byte[]> operator;

    private final ScheduledExecutorService scheduler;

    private RedisMetricsConfig(Builder builder) {
        this.codec = builder.codec;
        this.maxLen = builder.maxLen;
        this.period = builder.period;
        this.group = builder.group;
        this.enableGroupPrefix = builder.enableGroupPrefix;
        this.operator = builder.operator;
        this.scheduler = builder.scheduler;
    }

    public RedisCacheMetricsCodec getCodec() {
        return codec;
    }

    public long getPeriod() {
        return period;
    }

    public long getMaxLen() {
        return maxLen;
    }

    public String getGroup() {
        return group;
    }

    public boolean isEnableGroupPrefix() {
        return enableGroupPrefix;
    }

    public StreamOperator<byte[], byte[]> getOperator() {
        return operator;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long maxLen = 1000;
        private long period = 60000;
        private String group;
        private StreamOperator<byte[], byte[]> operator;
        private RedisCacheMetricsCodec codec;
        private ScheduledExecutorService scheduler;
        private boolean enableGroupPrefix;

        public Builder maxLen(Long maxLen) {
            if (maxLen != null) {
                Assert.isTrue(maxLen > 0L, "maxLen must be greater than 0");
                this.maxLen = maxLen;
            }
            return this;
        }

        public Builder period(Long period) {
            if (period != null) {
                Assert.isTrue(period > 0L, "period must be greater than 0");
                this.period = period;
            }
            return this;
        }

        public Builder group(String group) {
            Assert.notNull(group, "group must not be null");
            this.group = group;
            return this;
        }

        public Builder operator(StreamOperator<byte[], byte[]> operator) {
            Assert.notNull(operator, "RedisOperator must not be null");
            this.operator = operator;
            return this;
        }

        public Builder codec(RedisCacheMetricsCodec codec) {
            Assert.notNull(codec, "Codec must not be null");
            this.codec = codec;
            return this;
        }

        public Builder scheduler(ScheduledExecutorService scheduler) {
            Assert.notNull(scheduler, "ScheduledExecutorService must not be null");
            this.scheduler = scheduler;
            return this;
        }

        public Builder enableGroupPrefix(Boolean enableGroupPrefix) {
            if (enableGroupPrefix != null) {
                this.enableGroupPrefix = enableGroupPrefix;
            }
            return this;
        }

        public RedisMetricsConfig build() {
            return new RedisMetricsConfig(this);
        }

    }

}