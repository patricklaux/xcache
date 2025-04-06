package com.igeeksky.xcache.extension.refresh;

import com.igeeksky.xcache.common.ShutdownBehavior;
import com.igeeksky.xcache.props.CacheConstants;
import com.igeeksky.xtool.core.lang.Assert;

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

    private final int refreshThreadPeriod;

    private final int refreshSlotSize;

    private final int refreshTaskSize;

    private final int refreshAfterWrite;

    private final long shutdownTimeout;

    private final long shutdownQuietPeriod;

    private final ShutdownBehavior shutdownBehavior;

    private final boolean enableGroupPrefix;

    private final Map<String, Object> params;

    public RefreshConfig(Builder builder) {
        this.name = builder.name;
        this.group = builder.group;
        this.charset = builder.charset;
        this.provider = builder.provider;
        this.sid = builder.sid;
        this.refreshThreadPeriod = builder.refreshThreadPeriod;
        this.refreshTaskSize = builder.refreshTaskSize;
        this.refreshAfterWrite = builder.refreshAfterWrite;
        this.refreshSlotSize = builder.refreshSlotSize;
        this.shutdownTimeout = builder.shutdownTimeout;
        this.shutdownQuietPeriod = builder.shutdownQuietPeriod;
        this.shutdownBehavior = builder.shutdownBehavior;
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

    public long getRefreshThreadPeriod() {
        return refreshThreadPeriod;
    }

    public int getRefreshTaskSize() {
        return refreshTaskSize;
    }

    public int getRefreshAfterWrite() {
        return refreshAfterWrite;
    }

    public int getRefreshSlotSize() {
        return refreshSlotSize;
    }

    public long getShutdownTimeout() {
        return shutdownTimeout;
    }

    public long getShutdownQuietPeriod() {
        return shutdownQuietPeriod;
    }

    public ShutdownBehavior getShutdownBehavior() {
        return shutdownBehavior;
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

        private Charset charset = CacheConstants.DEFAULT_CHARSET;

        private int refreshTaskSize = CacheConstants.DEFAULT_REFRESH_TASK_SIZE;

        private int refreshSlotSize = CacheConstants.DEFAULT_REFRESH_SLOT_SIZE;

        private int refreshAfterWrite = CacheConstants.DEFAULT_REFRESH_AFTER_WRITE;

        private int refreshThreadPeriod = CacheConstants.DEFAULT_REFRESH_THREAD_PERIOD;

        private long shutdownTimeout = CacheConstants.DEFAULT_SHUTDOWN_TIMEOUT;

        private long shutdownQuietPeriod = CacheConstants.DEFAULT_SHUTDOWN_QUIET_PERIOD;

        private ShutdownBehavior shutdownBehavior = CacheConstants.DEFAULT_SHUTDOWN_BEHAVIOR;

        private boolean enableGroupPrefix = CacheConstants.DEFAULT_ENABLE_GROUP_PREFIX;

        private final Map<String, Object> params = new HashMap<>();

        /**
         * 应用实例 ID（不能为空）
         *
         * @param sid 应用实例 ID
         * @return {@code this} – Builder
         */
        public Builder sid(String sid) {
            this.sid = sid;
            return this;
        }

        /**
         * 缓存名称（不能为空）
         *
         * @param name 缓存名称
         * @return {@code this} – Builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * 组名称（不能为空）
         *
         * @param group 组名称
         * @return {@code this} – Builder
         */
        public Builder group(String group) {
            this.group = group;
            return this;
        }

        /**
         * 缓存数据刷新器工厂 ID（不能为空）
         *
         * @param provider 缓存数据刷新器工厂 ID
         * @return {@code this} – Builder
         */
        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        /**
         * 字符集（不能为空）
         *
         * @param charset 字符集
         * @return {@code this} – Builder
         */
        public Builder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        /**
         * 数据刷新周期（大于 0）
         *
         * @param refreshAfterWrite 数据刷新周期
         * @return {@code this} – Builder
         */
        public Builder refreshAfterWrite(int refreshAfterWrite) {
            this.refreshAfterWrite = refreshAfterWrite;
            return this;
        }

        /**
         * 刷新任务队列大小（大于等于 refreshSlotSize）
         *
         * @param refreshTaskSize 刷新任务队列大小
         * @return {@code this} – Builder
         */
        public Builder refreshTaskSize(int refreshTaskSize) {
            this.refreshTaskSize = refreshTaskSize;
            return this;
        }

        /**
         * 刷新线程执行周期（大于 0）
         *
         * @param refreshThreadPeriod 刷新线程执行周期
         * @return {@code this} – Builder
         */
        public Builder refreshThreadPeriod(int refreshThreadPeriod) {
            this.refreshThreadPeriod = refreshThreadPeriod;
            return this;
        }

        /**
         * 刷新数据槽数量（大于等于 1）
         *
         * @param refreshSlotSize 刷新数据槽数量
         * @return {@code this} – Builder
         */
        public Builder refreshSlotSize(int refreshSlotSize) {
            this.refreshSlotSize = refreshSlotSize;
            return this;
        }

        /**
         * 停止任务队列最大等待时长（大于 0）
         *
         * @param shutdownTimeout 停止任务队列最大等待时长
         * @return {@code this} – Builder
         */
        public Builder shutdownTimeout(long shutdownTimeout) {
            this.shutdownTimeout = shutdownTimeout;
            return this;
        }

        /**
         * 静默期：停止任务队列最小等待时长（小于 shutdownTimeout）
         *
         * @param shutdownQuietPeriod 停止任务队列最小等待时长
         * @return {@code this} – Builder
         */
        public Builder shutdownQuietPeriod(long shutdownQuietPeriod) {
            this.shutdownQuietPeriod = shutdownQuietPeriod;
            return this;
        }

        /**
         * 关闭行为（不能为空）
         *
         * @param shutdownBehavior 关闭行为
         * @return {@code this} – Builder
         */
        public Builder shutdownBehavior(ShutdownBehavior shutdownBehavior) {
            this.shutdownBehavior = shutdownBehavior;
            return this;
        }

        /**
         * 是否启用组前缀（默认为 false）
         *
         * @param enableGroupPrefix 是否启用组前缀
         * @return {@code this} – Builder
         */
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
            Assert.notNull(sid, "sid must not be null");
            Assert.notNull(name, "name must not be null");
            Assert.notNull(group, "group must not be null");
            Assert.notNull(provider, "provider must not be null");
            Assert.notNull(charset, "charset must not be null");
            Assert.notNull(provider, "provider must not be null");
            Assert.isTrue(refreshThreadPeriod > 0, "refreshThreadPeriod must be greater than 0");
            Assert.isTrue(refreshTaskSize >= refreshSlotSize, "refreshTasksSize must be greater than or equal to refreshSlotSize");
            Assert.isTrue(refreshAfterWrite > 0, "refreshAfterWrite must be greater than 0");
            Assert.isTrue(shutdownTimeout > 0, "shutdownTimeout must be greater than 0");
            Assert.isTrue(shutdownQuietPeriod < shutdownTimeout, "shutdownQuietPeriod must be less than shutdownTimeout");
            Assert.notNull(shutdownBehavior, "shutdownBehavior must not be null");
            return new RefreshConfig(this);
        }

    }

}