package com.igeeksky.xcache.autoconfigure.redis;

import com.igeeksky.redis.RedisOperatorFactory;
import com.igeeksky.redis.stream.StreamListenerContainer;
import com.igeeksky.xcache.extension.stat.CacheStatMessage;
import com.igeeksky.xcache.redis.lock.RedisLockProvider;
import com.igeeksky.xcache.redis.refresh.RedisCacheRefreshProvider;
import com.igeeksky.xcache.redis.stat.RedisCacheStatProvider;
import com.igeeksky.xcache.redis.store.RedisStoreProvider;
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
     * 字符集
     * <p>
     * 默认值：UTF-8
     * <p>
     * 用于 RedisCacheSyncMessage 及 RedisCacheStatMessage 编解码。
     */
    private String charset;

    /**
     * RedisStoreProvider 配置列表
     * <p>
     * 使用指定的 RedisOperatorFactory，创建 RedisStoreProvider
     * <p>
     * 列表类型，如果有多个 RedisOperatorFactory，可对应创建多个 RedisStoreProvider。
     * <p>
     * <b>配置示例：</b>
     * <pre>{
     *     id: lettuce,
     *     factory: lettuce
     * }</pre>
     * <b>配置说明：</b><p>
     * 使用 id 为 “lettuce” 的 RedisOperatorFactory 创建 id 为 “lettuce” 的 RedisStoreProvider 。
     */
    private List<StoreOption> store;

    /**
     * StreamListenerContainer 配置列表
     * <p>
     * 使用指定的 RedisOperatorFactory，创建 StreamListenerContainer
     * <p>
     * 列表类型，如果有多个 RedisOperatorFactory，可对应创建多个 StreamListenerContainer。
     * <p>
     * 既知 RedisCacheSyncProvider 依赖此组件，用以实现缓存数据同步。
     * <p>
     * <b>配置示例：</b>
     * <pre>{
     *     id: lettuce,
     *     factory: lettuce,
     *     block: 10,
     *     delay: 10,
     *     count: 1000
     * }</pre>
     * <p>
     * <b>配置说明：</b><p>
     * 使用 id 为 “lettuce” 的 RedisOperatorFactory，创建 id 为 “lettuce” 的 StreamListenerContainer 。<br>
     * 读取 Stream 时，最多阻塞 10 毫秒等待新消息，单次最多读取 1000 条消息。<br>
     * 消息读取到本地后，缓存数据同步监听会开始消费，全部消息消费完成后休眠 10 毫秒后再执行新任务。
     */
    private List<ListenerOption> listener;

    /**
     * RedisCacheSyncProvider 配置列表
     * <p>
     * 使用指定的 StreamListenerContainer，创建 RedisCacheSyncProvider
     * <p>
     * 列表类型，如果有多个 StreamListenerContainer，可对应创建多个 RedisCacheSyncProvider。
     * <p>
     * <b>配置示例：</b>
     * <pre>{
     *     id: lettuce,
     *     container: lettuce
     * }</pre>
     * <p>
     * <b>配置说明：</b><p>
     * 使用 id 为 “lettuce” 的 StreamListenerContainer，创建 id 为 “lettuce” 的 RedisCacheSyncProvider。
     */
    private List<SyncOption> sync;

    /**
     * RedisCacheRefreshProvider 配置列表
     * <p>
     * 使用指定的 RedisOperatorFactory，创建 RedisCacheRefreshProvider
     * <p>
     * 列表类型，如果有多个 RedisOperatorFactory，可对应创建多个 RedisCacheRefreshProvider。
     * <p>
     * <b>配置示例：</b>
     * <pre>{
     *     id: lettuce,
     *     factory: lettuce
     * }</pre>
     *
     * <b>配置说明：</b><p>
     * 使用 id 为 “lettuce” 的 RedisOperatorFactory，创建 id 为 “lettuce” 的 RedisCacheRefreshProvider。
     */
    private List<RefreshOption> refresh;

    /**
     * RedisCacheLockProvider 配置列表
     * <p>
     * 使用指定的 RedisOperatorFactory，创建 RedisCacheLockProvider
     * <p>
     * 列表类型，如果有多个 RedisOperatorFactory，可对应创建多个 RedisCacheLockProvider。
     * <p>
     * <b>配置示例：</b>
     * <pre>{
     *     id: lettuce
     *     factory: lettuce
     * }</pre>
     * <p>
     * <b>配置说明：</b><p>
     * 使用 id 为 “lettuce” 的 RedisOperatorFactory，创建 id 为 “lettuce” 的 RedisCacheLockProvider。
     */
    private List<LockOption> lock;

    /**
     * RedisCacheStatProvider 配置列表
     * <p>
     * 使用指定的 RedisOperatorFactory，创建 RedisCacheStatProvider
     * <p>
     * 列表类型，如果有多个 RedisOperatorFactory，可对应创建多个 RedisCacheStatProvider。
     * <p>
     * <b>配置示例：</b>
     * <pre>{
     *     id: lettuce,
     *     factory: lettuce,
     *     period: 60000,
     *     max-len: 10000,
     *     enable-group-prefix: false
     * }</pre>
     * <p>
     * <b>配置说明：</b><p>
     * 使用 id 为 “lettuce” 的 RedisOperatorFactory，创建 id 为 “lettuce” 的 RedisCacheStatProvider，
     * 统计周期为 60 秒，Stream 队列限定存储 10000 条数据（近似值），不附加 group 名称作为 channel 后缀。
     */
    private List<StatOption> stat;

    /**
     * 字符集
     *
     * @return {@code String} – 字符集
     */
    public String getCharset() {
        return charset;
    }

    /**
     * 字符集
     *
     * @param charset 字符集
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * RedisStoreProvider 配置列表
     *
     * @return {@code List<StoreOption>} – RedisStoreProvider 配置列表
     */
    public List<StoreOption> getStore() {
        return store;
    }

    /**
     * RedisStoreProvider 配置列表
     *
     * @param store RedisStoreProvider 配置列表
     */
    public void setStore(List<StoreOption> store) {
        this.store = store;
    }

    /**
     * StreamListenerContainer 配置列表
     *
     * @return {@code List<ListenerOption>} – StreamListenerContainer 配置列表
     */
    public List<ListenerOption> getListener() {
        return listener;
    }

    /**
     * StreamListenerContainer 配置列表
     *
     * @param listener StreamListenerContainer 配置列表
     */
    public void setListener(List<ListenerOption> listener) {
        this.listener = listener;
    }

    /**
     * RedisCacheSyncProvider 配置列表
     *
     * @return {@code List<SyncOption>} – RedisCacheSyncProvider 配置列表
     */
    public List<SyncOption> getSync() {
        return sync;
    }

    /**
     * RedisCacheSyncProvider 配置列表
     *
     * @param sync RedisCacheSyncProvider 配置列表
     */
    public void setSync(List<SyncOption> sync) {
        this.sync = sync;
    }

    /**
     * RedisCacheRefreshProvider 配置列表
     *
     * @return {@code List<RefreshOption>} – RedisCacheRefreshProvider 配置列表
     */
    public List<RefreshOption> getRefresh() {
        return refresh;
    }

    /**
     * RedisCacheRefreshProvider 配置列表
     *
     * @param refresh RedisCacheRefreshProvider 配置列表
     */
    public void setRefresh(List<RefreshOption> refresh) {
        this.refresh = refresh;
    }

    /**
     * RedisCacheLockProvider 配置列表
     *
     * @return {@code List<LockOption>} – RedisCacheLockProvider 配置列表
     */
    public List<LockOption> getLock() {
        return lock;
    }

    /**
     * RedisCacheLockProvider 配置列表
     *
     * @param lock RedisCacheLockProvider 配置列表
     */
    public void setLock(List<LockOption> lock) {
        this.lock = lock;
    }

    /**
     * RedisCacheStatProvider 配置列表
     *
     * @return {@code List<StatOption>} – RedisCacheStatProvider 配置列表
     */
    public List<StatOption> getStat() {
        return stat;
    }

    /**
     * RedisCacheStatProvider 配置列表
     *
     * @param stat RedisCacheStatProvider 配置列表
     */
    public void setStat(List<StatOption> stat) {
        this.stat = stat;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        if (charset != null) {
            joiner.add("\"charset\":\"" + charset + "\"");
        }
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

    /**
     * {@link RedisCacheSyncProvider} 配置项
     */
    public static class SyncOption {

        /**
         * 需要创建的 {@link RedisCacheSyncProvider} 唯一标识
         */
        private String id;

        /**
         * 指定使用的 {@link StreamListenerContainer}
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

    /**
     * {@link StreamListenerContainer} 配置项
     */
    public static class ListenerOption {

        /**
         * 需要创建的 {@link StreamListenerContainer} 唯一标识
         */
        private String id;

        /**
         * 指定使用的 {@link RedisOperatorFactory}
         */
        private String factory;

        /**
         * 读取 Stream 时的阻塞毫秒数
         * <p>
         * 默认值： 10 单位：毫秒
         * <p>
         * 如果 block 大于 0，则 {@link StreamListenerContainer} 会阻塞 block 毫秒，直到有新消息到达；<br>
         * 如果 block 等于 0，则 {@link StreamListenerContainer} 会无限期阻塞，直到有新消息到达；<br>
         * 如果 block 小于 0，不阻塞。
         * <p>
         * <b>注意：</b><p>
         * {@link StreamListenerContainer} 的每次任务是批量读取所有已注册的 channel，
         * 且只有当次任务执行完毕后才会重新开始新的任务。<br>
         * 即：当 {@link StreamListenerContainer} 已经开始阻塞读取消息时，阻塞期间新注册的 channel 都不会被读取，
         * 因此建议配置 block 为一个较小的值，且不为 0。
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

    /**
     * {@link RedisStoreProvider} 缓存存储配置项
     */
    public static class StoreOption {

        /**
         * 需要创建的 {@link RedisStoreProvider} 的唯一标识
         */
        private String id;

        /**
         * 指定使用的 {@link RedisOperatorFactory}
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

    /**
     * {@link RedisCacheRefreshProvider} 配置项
     */
    public static class RefreshOption {

        /**
         * 需要创建的 {@link RedisCacheRefreshProvider} 的唯一标识
         */
        private String id;

        /**
         * 指定使用的 {@link RedisOperatorFactory}
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

    /**
     * {@link RedisLockProvider} 配置项
     */
    public static class LockOption {

        /**
         * 需要创建的 {@link RedisLockProvider} 的唯一标识
         */
        private String id;

        /**
         * 指定使用的 {@link RedisOperatorFactory}
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

    /**
     * {@link RedisCacheStatProvider} 配置项
     */
    public static class StatOption {

        /**
         * 需要创建的 {@link RedisCacheStatProvider} 的唯一标识
         */
        private String id;

        /**
         * 指定使用的 {@link RedisOperatorFactory}，用于发布统计指标信息（Stream）
         */
        private String factory;

        /**
         * 统计周期
         * <p>
         * 默认值：60000，单位：毫秒
         */
        private long period = 60000;

        /**
         * 统计消息的 stream channel，是否附加 group 作为后缀
         * <p>
         * 默认值：false
         * <p>
         * 如果为 true，完整的 channel 为：{@code String channel = "stat:" + group} <br>
         * 如果为 false，完整的 channel 为：{@code String channel = "stat:" } <p>
         * {@link CacheStatMessage} 内部已包含 group 名称，
         * 如希望多个应用的缓存统计指标共用一套消费者进行统计汇总，且新增应用后无需再手动添加 channel，则建议保持默认，不附加 group。
         */
        private Boolean enableGroupPrefix;

        /**
         * Redis stream 最大长度
         * <p>
         * 默认值：10000
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

        public Boolean getEnableGroupPrefix() {
            return enableGroupPrefix;
        }

        public void setEnableGroupPrefix(Boolean enableGroupPrefix) {
            this.enableGroupPrefix = enableGroupPrefix;
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