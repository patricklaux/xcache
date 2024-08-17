package com.igeeksky.xcache.redis.stat;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/19
 */
public class RedisStatConfig {

    private final long maxLen;

    private final long period;

    private final String suffix;

    private final RedisOperator operator;

    private final ScheduledExecutorService scheduler;

    private RedisStatConfig(Builder builder) {
        this.maxLen = builder.maxLen;
        this.period = builder.period;
        this.suffix = builder.suffix;
        this.operator = builder.operator;
        this.scheduler = builder.scheduler;
    }

    public long getPeriod() {
        return period;
    }

    public long getMaxLen() {
        return maxLen;
    }

    public String getSuffix() {
        return suffix;
    }

    public RedisOperator getOperator() {
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
        private String suffix;
        private RedisOperator operator;
        private ScheduledExecutorService scheduler;

        public Builder maxLen(Long maxLen) {
            if (maxLen != null) {
                this.maxLen = maxLen;
            }
            return this;
        }

        public Builder period(Long period) {
            if (period != null) {
                this.period = period;
            }
            return this;
        }

        public Builder suffix(String suffix) {
            Assert.notNull(suffix, "suffix must not be null");
            this.suffix = suffix;
            return this;
        }

        public Builder operator(RedisOperator operator) {
            Assert.notNull(operator, "RedisOperator must not be null");
            this.operator = operator;
            return this;
        }

        public Builder scheduler(ScheduledExecutorService scheduler) {
            Assert.notNull(scheduler, "ScheduledExecutorService must not be null");
            this.scheduler = scheduler;
            return this;
        }

        public RedisStatConfig build() {
            return new RedisStatConfig(this);
        }
    }

}