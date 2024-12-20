package com.igeeksky.xcache.props;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存刷新配置
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/6
 */
public class RefreshProps {

    /**
     * 默认构造函数
     */
    public RefreshProps() {
    }

    private String provider;

    private Long refreshAfterWrite;

    private Integer refreshTasksSize;

    private Long refreshThreadPeriod;

    private Integer refreshSequenceSize;

    private Boolean enableGroupPrefix;

    private final Map<String, Object> params = new HashMap<>();

    /**
     * CacheRefreshProviderId
     * <p>
     * 默认值：none
     * <p>
     * {@link CacheConstants#DEFAULT_REFRESH_PROVIDER}
     * <p>
     * 注意：<br>
     * CacheRefresh 依赖于 CacheLoader，如未配置 CacheLoader，请保持默认值 none，否则会抛出配置异常。
     *
     * @return {@link String} - CacheRefreshProviderId
     */
    public String getProvider() {
        return provider;
    }

    /**
     * 设置 CacheRefreshProviderId
     *
     * @param provider CacheRefreshProviderId
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * 设置数据刷新周期
     * <p>
     * 默认值：3600000 单位：毫秒
     * <p>
     * {@link CacheConstants#DEFAULT_REFRESH_AFTER_WRITE}
     * <p>
     * 数据写入缓存后，每隔此配置的时间刷新一次（近似值，非精确值）。
     * <p>
     * <b>注意：</b><p>
     * 为了能在过期之前预刷新，此配置值应小于最后一级缓存的最小存活时间，
     * 即 {@code refresh-after-write} 应小于 {@code expire-after-write}。<p>
     * 另，如最后一级缓存的随机存活时间为 true，即 {@code enable-random-ttl: true}，
     * 那么 {@code refresh-after-write} 应小于 {@code expire-after-write × 0.8}。
     * <p>
     * 另，考虑到刷新任务执行过程中，可能出现服务器断电、网络异常等意外因素，数据刷新并不能保证一定能在配置时间内成功。因此建议：<br>
     * {@code  expire-after-write : refresh-after-write >= 4:1 }
     * <p>
     * 另，当使用 {@code RedisCacheRefresh} 进行缓存刷新，刷新周期计算依赖于 RedisServer 时间。
     * 如 RedisServer 时间异常，则可能会导致刷新任务不执行。
     *
     * @return {@link Long} - 数据刷新周期
     */
    public Long getRefreshAfterWrite() {
        return refreshAfterWrite;
    }

    /**
     * 设置数据刷新周期
     *
     * @param refreshAfterWrite 数据刷新周期
     */
    public void setRefreshAfterWrite(Long refreshAfterWrite) {
        this.refreshAfterWrite = refreshAfterWrite;
    }

    /**
     * 刷新线程一个周期发起运行的最大任务数
     * <p>
     * 默认值：16384 <br>
     * {@link CacheConstants#DEFAULT_REFRESH_TASKS_SIZE}
     * <p>
     * 增加此限制的主要目的是为了限制单个服务器同时运行的任务数，以避免服务器资源耗尽。<br>
     * 当某个时间周期内需要刷新的数据量超过此值，那么剩余未刷新数据将会顺延至下一次线程启动，起到平峰削谷的作用。<br>
     * 另，刷新线程每次启动时，会先检查上一周期发起的全部任务是否已全部完成，未完成则终止当次运行。
     * <p>
     * <b>示例：</b><p>
     * 假设配置为 {@code {refresh-tasks-size: 16384, refresh-thread-period: 10000}}，
     * 那么刷新线程每隔 10 秒启动一次，每次最多会刷新 16384 条数据。
     * <p>
     * <b>注意：</b><p>
     * 如果是 Redis 集群，为了能确保每一个 SortedSet 都至少能刷新一条数据，
     * 此值应大于等于 {@code sequence-size}，且最好为 {@code sequence-size} 的整数倍。
     * <p>
     * <b>建议值：</b><p>
     * {@code refresh-tasks-size >= 需刷新数据总量 ÷ (refresh-after-write ÷ refresh-thread-period) × 4}<br>
     * 譬如刷新数据总量为 1000000，数据刷新周期为 3600000 毫秒（3600秒），刷新线程启动周期为 10000 毫秒（10秒）<br>
     * 计算： {@code 1000000 ÷ (3600000 ÷ 10000) × 4 ≈  11000 } <br>
     * 那么， {@code refresh-tasks-size} 保持默认值 16384 是比较合适的。 <br>
     * 另外，如果刷新数据总量很小，即使 {@code refresh-tasks-size} 配置得很大，但一个运行周期内发起的刷新任务也是很少的。<br>
     * 总之，在服务器资源、网络带宽……等许可范围内，宁大勿小，避免无法及时刷新。
     *
     * @return {@link Integer} - 刷新线程一个周期发起运行的最大任务数
     */
    public Integer getRefreshTasksSize() {
        return refreshTasksSize;
    }

    /**
     * 设置刷新线程一个周期发起运行的最大任务数
     *
     * @param refreshTasksSize 刷新线程一个周期发起运行的最大任务数
     */
    public void setRefreshTasksSize(Integer refreshTasksSize) {
        this.refreshTasksSize = refreshTasksSize;
    }

    /**
     * 刷新线程运行周期
     * <p>
     * 默认值：10000 单位：毫秒
     * <p>
     * {@link CacheConstants#DEFAULT_REFRESH_THREAD_PERIOD}
     * <p>
     * 如配置为 10000，那么刷新线程每隔 10000 毫秒运行一次，检查是否有数据已到刷新周期，有则执行刷新操作。
     *
     * @return {@link Long} - 刷新线程运行周期
     */
    public Long getRefreshThreadPeriod() {
        return refreshThreadPeriod;
    }

    /**
     * 设置刷新线程运行周期
     *
     * @param refreshThreadPeriod 刷新线程运行周期
     */
    public void setRefreshThreadPeriod(Long refreshThreadPeriod) {
        this.refreshThreadPeriod = refreshThreadPeriod;
    }

    /**
     * 刷新序列数量
     * <p>
     * 适用于 Redis 集群模式，其它模式下此配置无效。
     * <p>
     * 默认值：32 <br>
     * {@link CacheConstants#DEFAULT_REFRESH_SEQUENCE_SIZE}
     * <p>
     * 如使用 {@code RedisCacheRefresh} 作为缓存刷新，将使用 Redis 的 SortedSet 记录需要刷新的数据和时间。<br>
     * 当 Redis 为集群模式时，为了让数据尽可能均匀分布于各个 Redis 节点，会创建多个 SortedSet。<br>
     * 读取或保存刷新数据时，使用 crc16 算法计算 key 的哈希值，然后取余 {@code refresh-sequence-size} 以选择使用哪个 SortedSet。
     * <p>
     * <b>示例：</b><p>
     * 设 {@code {group: shop, name: user, sequence-size: 32, enable-group-prefix: true}}，那么 Redis 中会创建
     * {@code ["refresh:shop:user:0"、"refresh:shop:user:1", "refresh:shop:user:2", ……, "refresh:shop:user:30", ""refresh:shop:user:31"]}
     * 共 32个 SortedSet。
     * <p>
     * <b>注意：</b><p>
     * 1、Redis 集群节点数越多，此配置值应越大。<br>
     * 2、最小值为 32，最大值为 16384。<br>
     * 3、此值应为 2 的 n 次方，如 32、64、128、256 等。<br>
     * 4、配置值不宜过小：过小会导致数据倾斜。<br>
     * 5、配置值不宜过大：因为刷新线程运行时会遍历所有的 SortedSet，更多的 SortedSet，意味着更多的网络请求。<br>
     * 建议 {@code sequence-size ≈ (节点数量 × 4)}
     *
     * @return {@link Integer} - 序列数量
     */
    public Integer getRefreshSequenceSize() {
        return refreshSequenceSize;
    }

    /**
     * 设置刷新序列数量
     *
     * @param refreshSequenceSize 刷新序列数量
     */
    public void setRefreshSequenceSize(Integer refreshSequenceSize) {
        this.refreshSequenceSize = refreshSequenceSize;
    }

    /**
     * 是否附加 group 作为键前缀
     * <p>
     * 此选项仅用于外部缓存刷新实现，如 {@code RedisCacheRefresh}，当仅使用 cacheName 作为前缀时会导致键冲突，
     * 则需再附加 group 作为前缀。<br>
     * 内嵌刷新实现无需此配置，如 {@code EmbedCacheRefresh}。
     * <p>
     * 默认值：true <br>
     * {@link CacheConstants#DEFAULT_ENABLE_GROUP_PREFIX}
     * <p>
     * 如果 enableGroupPrefix 为 true，生成的刷新相关的 key 如下：
     * <p>
     * 用于保存所有访问记录：{@code String refreshKey = "refresh:" + group + ":" + cacheName} <br>
     * 用于刷新任务执行的锁：{@code String refreshLockKey = "refresh:lock:" + group + ":" + cacheName} <br>
     * 用于刷新任务时间记录：{@code String refreshPeriodKey = "refresh:period:" + group + ":" + cacheName}
     * <p>
     * 如果 enableGroupPrefix 为 false，生成的刷新相关的 key 如下：
     * <p>
     * 用于保存所有访问记录：{@code String refreshKey = "refresh:" + cacheName} <br>
     * 用于刷新任务执行的锁：{@code String refreshLockKey = "refresh:lock:" + cacheName} <br>
     * 用于刷新任务时间记录：{@code String refreshPeriodKey = "refresh:period:" + cacheName}
     *
     * @return {@link Boolean} – 是否附加 group 作为键前缀
     */
    public Boolean getEnableGroupPrefix() {
        return enableGroupPrefix;
    }

    /**
     * 设置 是否附加 group 作为键前缀
     *
     * @param enableGroupPrefix 是否附加 group 作为键前缀
     */
    public void setEnableGroupPrefix(Boolean enableGroupPrefix) {
        this.enableGroupPrefix = enableGroupPrefix;
    }

    /**
     * 扩展参数
     * <p>
     * 自定义扩展实现时，如需用到额外的未定义参数，可在此配置。
     * <p>
     * 如使用 xcache 内置实现，则无需此配置。<br>
     * 如不使用，请删除，否则会导致 SpringBoot 读取配置错误而启动失败。
     *
     * @return {@link Map} - 扩展参数
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * 设置扩展参数
     *
     * @param params 扩展参数
     */
    public void setParams(Map<String, Object> params) {
        if (params != null) {
            this.params.putAll(params);
        }
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}