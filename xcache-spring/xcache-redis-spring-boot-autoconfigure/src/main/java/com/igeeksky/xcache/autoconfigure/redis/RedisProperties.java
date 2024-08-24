package com.igeeksky.xcache.autoconfigure.redis;

import com.igeeksky.redis.RedisOperatorFactory;
import com.igeeksky.redis.stream.StreamListenerContainer;
import com.igeeksky.xcache.redis.sync.RedisCacheSyncProvider;
import com.igeeksky.xtool.core.json.SimpleJSON;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.StringJoiner;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
@Configuration
@ConfigurationProperties(prefix = "xcache.redis")
public class RedisProperties {

    /**
     * 使用配置的 RedisConnectionFactory，创建 RedisStoreProvider <p>
     * 配置示例：
     * <pre>{@code
     *     id: lettuce
     *     factory: lettuce
     * }</pre>
     */
    private List<StoreOption> store;

    /**
     * 创建 {@link StreamListenerContainer} 的相关配置 <p>
     * 既知 {@link RedisCacheSyncProvider} 依赖此组件，用以实现缓存数据同步。<p>
     * <b>配置示例：</b>
     * <pre>{@code
     *     id: lettuce
     *     factory: lettuce
     *     block: 10
     *     delay: 10
     *     count: 1000
     * }</pre>
     * <p>
     * <b>配置说明：</b><p>
     * 使用 id 为 “lettuce” 的 {@link RedisOperatorFactory} 创建 id 为 “lettuce” 的 {@link StreamListenerContainer}。<p>
     * 读取 Stream 时，最多阻塞 10 毫秒等待新消息，单次最多读取 1000 条消息。
     * 消息读取到本地后，缓存数据同步监听会开始消费，全部消息消费完成后休眠 10 毫秒后再执行新任务。
     */
    private List<ListenerOption> listener;

    /**
     * 创建 {@link RedisCacheSyncProvider} 的相关配置 <p>
     * <b>配置示例：</b>
     * <pre>{@code
     *     id: lettuce
     *     container: lettuce
     * }</pre>
     * <p>
     * <b>配置说明：</b><p>
     * 使用 id 为 “lettuce” 的 {@link StreamListenerContainer}，
     * 创建 {@link RedisCacheSyncProvider}。
     */
    private List<SyncOption> sync;

    /**
     * 使用配置的 RedisConnectionFactory，创建 RedisCacheRefreshProvider <p>
     * 配置示例：
     * <pre>{@code
     *     id: lettuceCacheRefreshProvider
     *     factory: lettuceConnectionFactory
     *     core-pool-size: 1
     * }</pre>
     */
    private List<RefreshOption> refresh;

    /**
     * 使用配置的 RedisConnectionFactory，创建 RedisCacheLockProvider <p>
     * 配置示例：
     * <pre>{@code
     *     id: lettuce
     *     factory: lettuce
     * }</pre>
     */
    private List<LockOption> lock;

    /**
     * 使用配置的 RedisOperatorFactory，创建 RedisCacheStatProvider <p>
     * 配置示例：
     * <pre>{@code
     *     id: lettuce
     *     factory: lettuce
     *     period: 60000
     *     suffix: _stat
     *     max-len: 100
     * }</pre>
     */
    private List<StatOption> stat;

    public List<StoreOption> getStore() {
        return store;
    }

    public void setStore(List<StoreOption> store) {
        this.store = store;
    }

    public List<ListenerOption> getListener() {
        return listener;
    }

    public void setListener(List<ListenerOption> listener) {
        this.listener = listener;
    }

    public List<SyncOption> getSync() {
        return sync;
    }

    public void setSync(List<SyncOption> sync) {
        this.sync = sync;
    }

    public List<RefreshOption> getRefresh() {
        return refresh;
    }

    public void setRefresh(List<RefreshOption> refresh) {
        this.refresh = refresh;
    }

    public List<LockOption> getLock() {
        return lock;
    }

    public void setLock(List<LockOption> lock) {
        this.lock = lock;
    }

    public List<StatOption> getStat() {
        return stat;
    }

    public void setStat(List<StatOption> stat) {
        this.stat = stat;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        if (store != null) {
            joiner.add("\"store\":" + store);
        }
        if (listener != null) {
            joiner.add("\"listener\":" + listener);
        }
        if (sync != null) {
            joiner.add("\"sync\":" + sync);
        }
        if (refresh != null) {
            joiner.add("\"refresh\":" + refresh);
        }
        if (lock != null) {
            joiner.add("\"lock\":" + lock);
        }
        if (stat != null) {
            joiner.add("\"stat\":" + stat);
        }
        return joiner.toString();
    }

    public static class SyncOption {

        /**
         * {@link RedisCacheSyncProvider} 唯一标识
         */
        private String id;

        /**
         * {@link StreamListenerContainer} 唯一标识
         */
        private String listener;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getListener() {
            return listener;
        }

        public void setListener(String listener) {
            this.listener = listener;
        }

        @Override
        public String toString() {
            return SimpleJSON.toJSONString(this);
        }

    }

    public static class ListenerOption {

        /**
         * {@link StreamListenerContainer} 唯一标识
         */
        private String id;

        /**
         * {@link RedisOperatorFactory} 唯一标识
         */
        private String factory;

        /**
         * 读取 Stream 时的阻塞毫秒数
         * <p>
         * 默认值： 10 单位：毫秒
         * <p>
         * 如果大于 0，阻塞配置的时长；<p>
         * 如果等于 0，无限期阻塞，直到有新消息到达；<p>
         * 如果小于 0，不阻塞。
         * <p>
         * <b>注意：</b><p>
         * {@link StreamListenerContainer} 的每次任务是批量读取所有已注册的 channel，
         * 且只有当次任务执行完毕后才会重新开始新的任务。<p>
         * 如果设置的时间过长或无限期阻塞，而原来所有的 channel 都没有新消息到达，则在该段时间内新注册的 channel 都不会被读取。
         */
        private long block = 10;

        /**
         * 当次同步任务结束后，下次任务开始前的延迟时长
         * <p>
         * 默认值： 10 单位：毫秒
         * <p>
         * <b>注意：</b> 不能为负数。
         */
        private long delay = 10;

        /**
         * 同步任务每次从 Stream 读取的最大消息数量
         * <p>
         * 默认值： 1000
         * <p>
         * <b>注意：</b> 不能为负数。
         */
        private long count = 1000;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFactory() {
            return factory;
        }

        public void setFactory(String factory) {
            this.factory = factory;
        }

        public long getBlock() {
            return block;
        }

        public void setBlock(long block) {
            this.block = block;
        }

        public long getDelay() {
            return delay;
        }

        public void setDelay(long delay) {
            this.delay = delay;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return SimpleJSON.toJSONString(this);
        }

    }

    public static class StoreOption {

        /**
         * CacheStoreProvider 唯一标识
         */
        private String id;

        /**
         * 底层使用的 RedisOperatorFactory 唯一标识
         */
        private String factory;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFactory() {
            return factory;
        }

        public void setFactory(String factory) {
            this.factory = factory;
        }

        @Override
        public String toString() {
            return SimpleJSON.toJSONString(this);
        }

    }

    public static class RefreshOption {

        /**
         * CacheRefreshProvider 唯一标识
         */
        private String id;

        /**
         * 底层使用的 RedisOperatorFactory 唯一标识
         */
        private String factory;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFactory() {
            return factory;
        }

        public void setFactory(String factory) {
            this.factory = factory;
        }

        @Override
        public String toString() {
            return SimpleJSON.toJSONString(this);
        }

    }

    public static class LockOption {

        /**
         * CacheLockProvider 唯一标识
         */
        private String id;

        /**
         * 底层使用的 RedisOperatorFactory 唯一标识
         */
        private String factory;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFactory() {
            return factory;
        }

        public void setFactory(String factory) {
            this.factory = factory;
        }

        @Override
        public String toString() {
            return SimpleJSON.toJSONString(this);
        }
    }


    public static class StatOption {
        /**
         * CacheStatProvider 唯一标识
         */
        private String id;

        /**
         * 底层使用的 RedisOperatorFactory 唯一标识
         */
        private String factory;

        /**
         * 统计周期 <p>
         * 默认值：60000，单位：毫秒
         */
        private long period = 60000;

        /**
         * Redis stream key 后缀 <p>
         * 使用 Redis stream 来发送缓存统计数据，完整的 key 为：<p>
         * {@code String key = "stat:" + suffix }
         */
        private String suffix;

        /**
         * Redis stream 最大长度
         */
        private long maxLen = 10000;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFactory() {
            return factory;
        }

        public void setFactory(String factory) {
            this.factory = factory;
        }

        public long getPeriod() {
            return period;
        }

        public void setPeriod(long period) {
            this.period = period;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }

        public long getMaxLen() {
            return maxLen;
        }

        public void setMaxLen(long maxLen) {
            this.maxLen = maxLen;
        }

        @Override
        public String toString() {
            return SimpleJSON.toJSONString(this);
        }

    }

}