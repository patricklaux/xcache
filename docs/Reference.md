## Xcache  Reference

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html) [![Release](https://img.shields.io/github/v/release/patricklaux/xcache)](https://github.com/patricklaux/xcache/releases) [![Maven](https://img.shields.io/maven-central/v/com.igeeksky.xcache/xcache-parent.svg)](https://central.sonatype.com/namespace/com.igeeksky.xcache) [![Last commit](https://img.shields.io/github/last-commit/patricklaux/xcache)](https://github.com/patricklaux/xcache/commits)

## 简介

Xcache 是易于扩展、功能强大且配置灵活的 Java 多级缓存框架。

## 架构

![Architecture](images/architecture.png)

**说明**：



## 特性

- 支持多种缓存模式：Cache-Aside，Read-Through，Write-Through，Write-Behind。
- 支持缓存数据同步：通过缓存事件广播，多个应用实例的缓存数据保持一致。
- 支持缓存指标统计：通过日志方式输出和 Redis Stream 方式输出，便于统计各种缓存指标。
- 支持随机存活时间：避免大量的 key 集中过期，导致数据源压力过大。
- 支持缓存自动刷新：自动刷新缓存数据，避免慢查询导致应用响应缓慢。
- 支持数据回源加锁：加锁确保相同的键同时仅有一个线程回源查询，降低回源次数，减轻数据源压力。
- 支持缓存数据压缩：通过压缩数据，降低内存消耗。
- 支持多级缓存实现：内嵌缓存采用 Caffeine，外部缓存采用 Redis，并可通过实现 Store 接口扩展缓存能力，最多可支持三级缓存。
- 支持数据存在断言：通过实现数据存在断言接口，譬如 Bloom Filter，避免回源查询。
- 支持缓存空值：当数据源确定无数据时，可缓存空值，避免缓存穿透。
- 虚拟线程特别优化：需要加锁执行或 IO 等待的定时任务，采用虚拟线程执行，降低平台线程资源占用。
- 更丰富的缓存注解：Cacheable，CacheableAll，CachePut，CachePutAll，CacheEvict，CacheEvictAll，CacheClear
- 适配 SpringCache：无需修改现有代码，添加 Xcache 依赖，即可支持更多缓存功能。

## 运行环境

SpringBoot：3.3.0+

JDK：21+

## 开始使用

**参考项目**：[xcache-samples-base](https://github.com/patricklaux/xcache-samples/tree/main/xcache-samples-base)

### 第一步：引入缓存依赖

如直接通过调用方法操作缓存，不使用缓存注解，仅需引入 ``xcache-spring-boot-starter`` 。

主要依赖：Caffeine（内嵌缓存），Lettuce（Redis 客户端），Jackson（序列化）

```xml
<dependencies>
    <dependency>
        <groupId>com.igeeksky.xcache</groupId>
        <artifactId>xcache-spring-boot-starter</artifactId>
        <version>${xcache.version}</version>
    </dependency>
    <!-- ... other ... -->
</dependencies>
```

### 第二步：编写缓存配置

```yaml
xcache:
  group: shop # 分组名称（必填），主要用于区分不同的应用
  template: # 公共模板配置（必填），列表类型，可配置一至多个
    - id: t0 # 模板ID（必填）
      first: # 一级缓存配置
        provider: caffeine # 缓存存储提供者实例 id
        store-type: EMBED # 缓存存储类型，根据类型自动填充默认配置
      second: # 二级缓存配置
        provider: none # 缓存存储提供者实例 id（如果配置为 none，则表示不使用二级缓存）
        store-type: EXTRA # 缓存存储类型，根据类型自动填充默认配置
  cache: # 缓存个性配置，列表类型，可配置零至多个
    - name: user # 缓存名称，用于区分不同的缓存对象
      template-id: t0 # 指定使用的模板为 t0（对应属性：xcache.template[i].id）
```

**说明**：

1. 公共模板配置

   同一应用中，可能会有多个不同名称的缓存对象，它们的配置通常大部分相同。

   为了避免大量的重复配置，可创建一个公共配置模板，然后在缓存个性配置中只需填写与该公共模板配置的差异部分即可。

2. Xcache 提供了丰富的配置项，大多数都有默认值，因此可以省略而无需填写。

### 第三步：调用缓存方法

```java
/**
 * 用户缓存服务
 */
@Service
public class UserCacheService {

    private final UserDao userDao;
    private final Cache<Long, User> cache;
    private final CacheLoader<Long, User> cacheLoader;

    public UserCacheService(UserDao userDao, CacheManager cacheManager) {
        this.userDao = userDao;
        this.cache = cacheManager.getOrCreateCache("user", Long.class, User.class);
        this.cacheLoader = new UserCacheLoader(this.userDao);
    }

    /**
     * 根据用户ID获取单个用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    public User getUser(Long id) {
        // 1. 首先查询缓存，如果缓存命中，则直接返回缓存数据；
        // 2. 如果缓存未命中，则调用 cacheLoader 从数据源加载数据。
        return cache.get(id, cacheLoader);
    }

    /**
     * 根据用户ID批量获取用户信息
     *
     * @param ids 用户ID集合
     * @return 用户信息集合
     */
    public Map<Long, User> getUsers(Set<Long> ids) {
        // 1. 首先查询缓存，如果缓存全部命中，则直接返回缓存数据；
        // 2. 如果缓存全部未命中或部分命中，则调用 cacheLoader 从数据源加载未命中数据。
        return cache.getAll(ids, this.cacheLoader);
    }

    /**
     * 新增用户
     *
     * @param user 新用户信息
     * @return 保存到数据库后返回的用户信息
     */
    public User saveUser(User user) {
        User created = userDao.save(user);
        // 将新增用户信息写入缓存
        cache.put(user.getId(), created);
        return created;
    }

    /**
     * 更新用户信息
     *
     * @param user 待更新的用户信息
     * @return 保存到数据库后返回的用户信息
     */
    public User updateUser(User user) {
        User updated = userDao.update(user);
        // 将更新后的用户信息写入缓存
        cache.put(user.getId(), updated);
        // 如果为了更好地保持数据一致性，这里可选择直接删除缓存数据，后续查询时再从数据源加载
        // cache.evict(user.getId());
        return updated;
    }

    /**
     * 批量更新用户信息
     *
     * @param users 待更新的用户信息集合
     * @return 保存到数据库后返回的用户信息集合
     */
    public Map<Long, User> updateUsers(List<User> users) {
        Map<Long, User> updates = userDao.batchUpdate(users);
        // 将更新后的用户信息写入缓存
        cache.putAll(updates);
        // 如果为了更好地保持数据一致性，这里可选择直接删除缓存数据，后续查询时再从数据源加载
        // cache.evictAll(updates.keySet());
        return updates;
    }

    /**
     * 删除用户信息
     *
     * @param id 用户ID
     */
    public void deleteUser(Long id) {
        userDao.delete(id);
        // 删除缓存数据
        cache.evict(id);
    }

    /**
     * 批量删除用户信息
     *
     * @param ids 用户ID集合
     */
    public void deleteUsers(Set<Long> ids) {
        userDao.batchDelete(ids);
        // 批量删除缓存数据
        cache.evictAll(ids);
    }

    /**
     * 清空数据
     */
    public void clear() {
        userDao.clear();
        // 清空缓存数据
        cache.clear();
    }

    /**
     * CacheLoader 实现类
     * <p>
     * 用于数据回源操作，当缓存中不存在指定数据时，会调用此方法从数据源加载数据。
     *
     * @param userDao
     */
    private record UserCacheLoader(UserDao userDao) implements CacheLoader<Long, User> {

        @Override
        public User load(Long id) {
            return this.userDao.findUser(id);
        }

        @Override
        public Map<Long, User> loadAll(Set<? extends Long> ids) {
            return this.userDao.findUserList(ids);
        }

    }

}
```

详细介绍：[Xcache 参考文档](docs/Reference.md)

示例项目：[xcache-samples](https://github.com/patricklaux/xcache-samples)

## 缓存配置

作为开源框架项目，关于配置项，我设计时遵循的是这三个基本原则：

1、尽可能多配置项：可以全面控制缓存的各种逻辑，对缓存进行全面调优；

2、尽可能少写配置：通过提供默认值和公共配置模板，避免显式书写配置；

3、尽可能不改代码：可通过调整配置项和增减依赖包，灵活适配业务逻辑。

#### 总体介绍

所有配置项可以划分为两部分，一部分是关于缓存的基础配置，一部分是关于 Redis 的扩展配置。

为了让大家有一个整体认识，先通过表格介绍大的配置类别，具体子项后面再通过 yaml 来详细介绍。

##### 基础配置

基础配置部分，除了少数几个必填项，大部分配置项都有默认值，如未配置，则将使用默认值。

| 类别             | 名称             | 详细描述                                                     |
| ---------------- | ---------------- | ------------------------------------------------------------ |
| xcache.group     | 组名             | 主要用于区分不同的应用。<br />譬如当有多个应用共用一套 Redis 作为缓存数据存储，则可以附加 group 作为前缀，从而避免键冲突。 |
| xcache.template  | 公共模板配置     | 除了缓存名称，同一应用的各个缓存实例的大部分配置应该都是相似的。<br />因此可以在此建立一个或多个公共模板，从而避免每一个缓存实例重复配置。 |
| xcache.cache     | 缓存个性配置     | 具体到某个缓存实例：<br />如配置项与公共模板配置完全相同，可以省略全部配置。<br />如配置项与公共模板配置部分不同，只需配置差异部分。 |
| xcache.stat      | 缓存指标统计配置 | 用于配置日志方式的统计信息输出的时间间隔。<br />如所有缓存实例的统计信息都选择输出到 Redis，则此配置项无效。 |
| xcache.scheduler | 调度器配置       | 缓存定时刷新、缓存指标定时采集均依赖于此调度器。             |

##### Redis 配置

Redis 配置部分，如果不配置，就不会生成对象实例。

| 类别                  | 说明       |
| --------------------- | ---------- |
| xcache.redis          | Redis 配置 |
| xcache.redis.charset  | 字符集     |
| xcache.redis.store    |            |
| xcache.redis.listener |            |
| xcache.redis.sync     |            |
| xcache.redis.lock     |            |
| xcache.redis.stat     |            |
| xcache.redis.refresh  |            |
| xcache.redis.lettuce  |            |



#### 细项说明

1. 除了标注必填的选项，其他选项均为选填，删除或留空表示使用默认配置；
2. 每个配置项均有详细说明，可用 ide 自动提示功能快速查看相关描述信息。

```yaml
xcache:
  group: shop # 分组名称 (必填)
  template: # 公共模板配置 (必填，仅需配置与默认配置不同的部分)，列表类型，可配置多个模板。
    - id: t0 # 模板ID (必填)，建议将其中一个模板的 id 配置为 t0。
      charset: UTF-8 # 字符集 (默认 UTF-8)
      cache-lock: # 缓存锁配置
        initial-capacity: 128 # HashMap 初始容量（）
        lease-time: 1000 # 锁租期 （默认值：1000 单位：毫秒）
        provider: lettuce # LockProviderId（默认值：embed）
        enable-group-prefix: true # 是否添加 group 作为前缀（默认值：true）
        params: # 用于扩展实现的自定义参数，map 类型 （如不使用，请删除，否则会提示参数读取异常）
          test: test
      cache-sync: # 缓存同步配置
        first: ALL # 一级缓存数据同步 （默认值：ALL，如仅有一级缓存，且为本地缓存，请改为 NONE 或 CLEAR）
        second: NONE # 二级缓存数据同步（默认值：NONE）
        enable-group-prefix: true # 是否添加 group 作为前缀（默认值：true）
        max-len: 1000 # 缓存同步队列最大长度 （默认值：10000）
        provider: lettuce # CacheSyncProviderId （默认值：lettuce）
        params: # 用于扩展实现的自定义参数，map 类型 （如不使用，请删除，否则会提示参数读取异常）
          test: test
      cache-stat: lettuce # CacheStatProviderId，用于缓存指标信息采集和输出（默认值：log，输出到日志）
      cache-refresh: # 缓存刷新配置
        period: 1000 # 刷新间隔周期（默认值：1800000 单位：毫秒）
        stop-after-access: 10000 # 某个键最后一次查询后，超过此时限则不再刷新 （默认值：7200000 毫秒）
        enable-group-prefix: true # 是否添加 group 作为前缀（默认值：true，适用于外部刷新实现）
        provider: none # CacheRefreshProviderId（默认值：none，不启用缓存刷新）
        params: # 用于扩展实现的自定义参数，map 类型 （如不使用，请删除，否则会提示参数读取异常）
          test: test
      key-codec: jackson # 用于将键转换成 String（默认值：jackson）
      contains-predicate: # 用于判断数据源是否存在相应数据（默认值：none，需自行实现并注入到 spring 容器）
      first: # 一级缓存配置
        store-type: EMBED # 根据是内嵌缓存还是外部缓存，自动填充不同的默认配置（一级缓存默认设定为：EMBED）
        provider: caffeine # StoreProviderId（内嵌缓存默认值：caffeine）
        initial-capacity: 65536 # 初始容量（默认值：65536）
        maximum-size: 65536 # 最大容量（默认值：65536）
        maximum-weight: 0 # 最大权重 （默认值：0，如小于等于 0，表示不采用基于权重的驱逐策略）
        key-strength: STRONG # 基于键的引用类型执行驱逐策略（默认值：STRONG）
        value-strength: STRONG # 基于值的引用类型执行驱逐策略（默认值：STRONG）
        expire-after-write: 3600000 # 数据写入后的存活时间（内嵌缓存默认值：3600000 单位：毫秒）
        expire-after-access: 300000 # 数据访问后的存活时间（默认值：300000 单位：毫秒，适用于 caffeine）
        enable-random-ttl: true # 是否使用随机存活时间（默认值：true，避免大量的 key 集中过期）
        enable-null-value: true # 是否允许保存空值（默认值：true）
        value-codec: none # 用于值对象的序列化（内嵌缓存默认值：none，不启用序列化）
        value-compressor: # 值压缩配置，如需启用压缩，必须启用序列化（先序列化后压缩）
          provider: none # CompressorProviderId（默认值：none，不启用数据压缩）
        params: # 用于扩展实现的自定义参数，map 类型 （如不使用，请删除，否则会提示参数读取异常）
          test: test
      second: # 二级缓存配置
        store-type: EXTRA # 根据是内嵌缓存还是外部缓存，自动填充不同的默认配置（二级缓存默认设定为：EXTRA）
        provider: lettuce # StoreProviderId（外部缓存默认值：lettuce）
        redis-type: STRING # Redis 命令类型（默认：STRING，如无需过期，可设为 HASH）
        expire-after-write: 7200000 # 数据写入后的存活时间（外部缓存默认值：7200000 单位：毫秒）
        enable-group-prefix: true # 是否添加 group 作为前缀（默认值：true，仅适用于外部缓存）
        enable-random-ttl: true # 是否使用随机存活时间（默认值：true，避免大量的 key 集中过期）
        enable-null-value: true # 是否允许保存空值（默认值：true）
        value-codec: jackson # 用于值对象的序列化（外部缓存默认值：jackson）
        value-compressor: # 值压缩配置，如需启用压缩，必须启用序列化（先序列化后压缩）
          provider: none # CompressorProviderId（默认值：none，不启用数据压缩）
          nowrap: false # 是否不携带封装信息（默认值：false，此配置对于 DeflaterCompressor 有效）
          level: 5 # 压缩级别（默认值：-1，此配置对于 DeflaterCompressor 有效）
        params: # 用于扩展实现的自定义参数，map 类型 （如不使用，请删除，否则 spring boot 会提示参数读取异常）
          test: test
      third: # 三级缓存配置
        store-type: EXTRA # 根据是内嵌缓存还是外部缓存，自动填充不同的默认配置（三级缓存默认设定为：EXTRA）
        provider: none # StoreProviderId（三级缓存默认值：none）
        redis-type: STRING # Redis 命令类型（默认：STRING，如无需过期，可设为 HASH）
        expire-after-write: 7200000 # 数据写入后的存活时间（外部缓存默认值：7200000 单位：毫秒）
        enable-group-prefix: true # 是否添加 group 作为前缀（默认值：true，仅适用于外部缓存）
        enable-random-ttl: true # 是否使用随机存活时间（默认值：true，避免大量的 key 集中过期）
        enable-null-value: true # 是否允许保存空值（默认值：true）
        value-codec: jackson # 用于值的序列化（外部缓存默认值：jackson）
        value-compressor: # 值压缩配置
          provider: none # CompressorProviderId（默认值：none，不启用数据压缩）
          nowrap: false # 是否不携带封装信息（默认值：false，此配置对于 DeflaterCompressor 有效）
          level: 5 # 压缩级别（默认值：-1，此配置对于 DeflaterCompressor 有效）
        params: # 用于扩展实现的自定义参数，map 类型 （如不使用，请删除，否则会提示参数读取异常）
          test: test
  cache: # 缓存配置（template 是公共配置，cache 是具体缓存个性配置，仅需配置与对应 template 不同的部分）
    # 另，如缓存配置与 id 为 t0 的模板配置完全相同，那么这里可以彻底删除，包括 name 与 template-id。
    - name: user # 缓存名称（必填）
      template-id: t0 # 模板id（默认值：t0，如未配置，默认从 id 为 t0 的模板中复制配置项）
      # …… 其余配置项与模板配置相同，所以直接省略
    - name: order # 缓存名称（必填）
      template-id: t0 # 模板id（默认值：t0，如未配置，默认从 id 为 t0 的模板中复制配置项）
      # …… 其余配置项与模板配置相同，所以直接省略
  stat: # 日志方式缓存指标统计配置
    period: 60000 # 缓存指标采集的时间间隔（默认值：60000 单位：毫秒）
  scheduler: # 调度器配置
    core-pool-size: 1 # 定时任务调度器核心线程数，如未配置，则使用 (核心数 / 8)，最小为 1。
  redis: # Redis 配置（如不使用 Redis，可直接删除此配置项；如未配置，则不会生成相应的实例对象）
    charset: UTF-8 # 字符集，用于数据同步消息和缓存指标消息的编解码（默认值：UTF-8）
    store: # RedisStoreProvider 配置，列表类型，可配置零至多个
      - id: lettuce #  要创建的 RedisStoreProvider 的 id（必填）
        factory: lettuce # 指定使用的 RedisOperatorFactory 的 id（必填）
    listener: # StreamListenerContainer 配置，列表类型，可配置零至多个
      - id: lettuce # 要创建的 StreamListenerContainer 的 id（必填）
        factory: lettuce # 指定使用的 RedisOperatorFactory 的 id（必填）
        block: 10 # 读取 Stream 时的阻塞时长（默认值： 10 单位：毫秒）
        delay: 1000 # 当次同步任务结束后，下次任务开始前的延迟时长（默认值： 10 单位：毫秒）
        count: 1000 # 同步任务每次从 Stream 读取的最大消息数量（默认值： 1000）
    sync: # RedisCacheSyncProvider 配置，列表类型，可配置零至多个
      - id: lettuce # 需要创建的 RedisCacheSyncProvider 的 id
        listener: lettuce # （）
    lock: # RedisLockProvider 配置，列表类型，可配置零至多个
      - id: lettuce # 要创建的 RedisLockProvider  的 id（必填）
        factory: lettuce # 指定使用的 RedisOperatorFactory 的 id（必填）
    stat: # RedisCacheStatProvider 配置，列表类型，可配置零至多个
      # 另，RedisCacheStat 只负责发送统计指标信息到指定的 channel，具体的统计汇总需用户自行实现
      - id: lettuce # 要创建的 RedisCacheStatProvider 的 id（必填）
        factory: lettuce # 指定使用的 RedisOperatorFactory 的 id（必填）
        period: 60000 # 缓存指标统计的时间间隔（默认值：60000，单位：毫秒）
        max-len: 10000 # Redis stream 最大长度（默认值：10000，采用近似裁剪，实际长度可能略大于配置值）
        enable-group-prefix: false # 是否附加 group 作为后缀（默认值：false）
    refresh: # RedisCacheRefreshProvider 配置，，列表类型，可配置多个
      - id: lettuce # 要创建的 RedisCacheRefreshProvider 的 id（必填）
        factory: lettuce # 指定使用的 RedisOperatorFactory 的 id（必填）
    lettuce: # Lettuce 客户端配置
      factories: # 考虑到一个应用可能会使用多套 Redis，因此采用列表类型，可配置多个
        - id: lettuce # RedisOperatorFactory 唯一标识（默认值：lettuce）
          standalone: # 单机模式 或 副本集模式（优先 sentinel 配置，其次 cluster，最后 standalone）
            node: 127.0.0.1:6379 # Redis 节点，支持 UnixSocket 方式
            nodes: socket:/tmp/redis.sock, 127.0.0.1:6380 # Redis 节点列表
            read-from: # 读节点选择策略（默认值：UPSTREAM，仅从主节点读取数据）
            username: # Redis 用户名
            password: # Redis 密码
            database: 0 # Redis 数据库索引（默认值：0）
            client-name: # Redis 客户端名称
            ssl: false # 是否启用 SSL（默认值：false）
            start-tls: false # 是否启用 TLS（默认值：false）
            ssl-verify-mode: FULL # SSL 验证模式，只有 ssl 配置为 true 时才有意义（默认值：FULL）
            timeout: 60000 # 同步执行命令等待完成的最大时长（默认值：60000 单位：毫秒）
            client-options: # 客户端选项，一般保持默认即可
              auto-reconnect: true # 是否自动重连（默认值：true）
              disconnected-behavior: DEFAULT # 连接断开后是否接受命令（默认值：DEFAULT）
              publish-on-scheduler: false # 是否使用专用的 scheduler 处理 publish 事件（默认值：false）
              ping-before-activate-connection: true # 连接激活前是否发送 PING 消息（默认值：true）
              protocol-version: RESP2 # Redis 协议版本（默认值：RESP3，Redis 6.0 以下请配置为 RESP2）
              suspend-reconnect-on-protocol-failure: # 是否在协议失败时暂停重连（默认值：false）
              request-queue-size: # 请求队列大小 （默认值：Integer.MAX_VALUE）
              socketOptions: # socket 配置选项
                connect-timeout: 10000 # Socket 连接超时（默认值：10000， 单位：毫秒）
                tcp-no-delay: true  # 是否启用 TCP_NODELAY（默认值：true）
                keep-alive: # KeepAlive 配置选项，仅适用于 epoll、 io_uring、Java 11 及之后版本的 NIO
                  enabled: false # 是否启用 KeepAlive（默认值：false）
                  count: 9 # KeepAlive 重试次数（默认值：9）
                  idle: 7200000 # KeepAlive 空闲时间（默认值：7200000， 单位：毫秒）
                  interval: 75000 # KeepAlive 发送间隔（默认值：75000， 单位：毫秒）
                tcp-user-timeout: # TCP_USER_TIMEOUT 配置选项，仅适用于 epoll 和 io_uring
                  enabled: false # 是否启用 TCP_USER_TIMEOUT（默认值：false）
                  tcp-user-timeout: 60000 # TCP 超时配置（默认值：7875000 单位：毫秒）
              ssl-options: # SSL 配置选项，只有 ssl 配置为 true 时才有意义
                ssl-provider: JDK # （默认值：JDK）
                key-store-type: # 密钥库格式（默认值：jks）
                keystore: # 密钥库路径
                keystore-password: # 密钥库密码
                truststore: # 信任库路径
                truststore-password: # 信任库密码
                protocols: # 支持的安全协议
                cipher-suites: # 支持的加密套件
                handshake-timeout: # 握手超时（默认值：10000 单位：毫秒）
              timeout-options: # 命令超时配置选项
                fixedTimeout: # 固定超时时间（默认值：-1，单位：毫秒，无超时配置）
          sentinel: # 哨兵模式配置（优先 sentinel 配置，其次 cluster 配置，最后 standalone 配置）
            master-id: mymaster # 哨兵主节点名称
            nodes: 127.0.0.1:26379, 127.0.0.1:26380, 127.0.0.1:26381 # 哨兵节点列表
            read-from: # 读节点选择策略（默认值：UPSTREAM，仅从主节点读取数据）
            sentinel-username:  # 哨兵用户名
            sentinel-password:  # 哨兵密码
            username:  # Redis 用户名
            password:  # Redis 密码
            database: 0 # Redis 数据库索引（默认值：0）
            client-name: # Redis 客户端名称
            ssl: false # 是否启用 SSL（默认值：false）
            start-tls: false # 是否启用 TLS（默认值：false）
            ssl-verify-mode: FULL # SSL 验证模式，只有 ssl 配置为 true 时才有意义（默认值：FULL）
            timeout:  # 同步执行命令等待完成的最大时长（默认值：60000 单位：毫秒）
            client-options: # 客户端选项，一般保持默认即可，可参考 Lettuce 官方文档
              auto-reconnect: true # 是否自动重连（默认值：true）
              # …… 其余配置省略，可参考 standalone 的相关配置
          cluster: # 集群模式配置 （优先 sentinel 配置，其次 cluster 配置，最后 standalone 配置）
            nodes: 127.0.0.1:7001, 127.0.0.1:7002, 127.0.0.1:7003, 127.0.0.1:7004, 127.0.0.1:7005, 127.0.0.1:7006 # 集群节点列表
            read-from: # 读节点选择策略（默认值：UPSTREAM，仅从主节点读取数据）
            username: redis-admin # Redis 用户名
            password: 123456 # Redis 密码
            database: 0 # Redis 数据库索引（默认值：0）
            client-name: # Redis 客户端名称
            ssl: false # 是否启用 SSL（默认值：false）
            start-tls: false # 是否启用 TLS（默认值：false）
            ssl-verify-mode: FULL # SSL 验证模式，只有 ssl 配置为 true 时才有意义（默认值：FULL）
            timeout:  # 同步执行命令等待完成的最大时长（默认值：60000 单位：毫秒）
            client-options: # 集群客户端选项，比一般客户端选项多了重定向、拓扑刷新等配置
              auto-reconnect: true # 是否自动重连（默认值：true）
              # …… 部分配置项省略，可参考 standalone 的相关配置
              max-redirects: # 集群重定向最大重试次数（默认值：5）
              validate-cluster-node-membership: # 是否验证集群节点成员关系（默认值：true）
              node-filter:    # 建立连接的节点[白名单] （如未配置，连接所有节点；如有配置，只连接配置节点）
              topology-refresh-options: # 拓扑刷新配置选项
                adaptive-refresh-triggers: # 动态刷新触发器，列表类型（默认为空集）
                adaptive-refresh-timeout: # 动态刷新超时（默认值：30000 单位：毫秒）
                close-stale-connections: # 是否关闭旧连接（默认值：true）
                dynamic-refresh-sources: # 是否动态刷新节点源（默认值：true）
                periodic-refresh-enabled: # 是否启用周期刷新（默认值：true）
                refresh-period: # 刷新周期 （默认值：30000 单位：毫秒）
                refresh-triggers-reconnect-attempts: # 刷新触发器重连尝试次数（默认值：3）
```

#### 简化配置示例

配置项很多，但大部分都有默认值，可以省略，以下是一个简化配置的示例。

```yaml
xcache:
  group: shop # 分组名称 (必填)
  template: # 公共模板配置 (必填)
    - id: t0 # 模板ID (必填)
      cache-lock: # 缓存锁配置
        provider: lettuce # LockProviderId（默认值：embed，这里选用 lettuce）
      cache-sync: # 缓存同步配置
        provider: lettuce # CacheSyncProviderId （默认值：none）
      cache-stat: lettuce # CacheStatProviderId（默认值：log，这里选用 lettuce）
  redis: # Redis 配置
    store: # RedisStoreProvider 配置
      - id: lettuce #  要创建的 RedisStoreProvider 的 id（必填）
        factory: lettuce # 指定使用的 RedisOperatorFactory 的 id（必填）
    listener: # StreamListenerContainer 配置，列表类型，可配置多个
      - id: lettuce # 要创建的 StreamListenerContainer 的 id（必填）
        factory: lettuce # 指定使用的 RedisOperatorFactory 的 id（必填）
    sync: # RedisCacheSyncProvider 配置，列表类型，可配置多个
      - id: lettuce # 需要创建的 RedisCacheSyncProvider 的 id（必填）
        listener: lettuce # 指定使用的 StreamListenerContainer 的 id（必填）
    lock: # RedisLockProvider 配置
      - id: lettuce # 要创建的 RedisLockProvider 的 id（必填）
        factory: lettuce # 指定使用的 RedisOperatorFactory 的 id（必填）
    stat: # RedisCacheStatProvider 配置
      - id: lettuce # 要创建的 RedisCacheStatProvider 的 id（必填）
        factory: lettuce # 指定使用的 RedisOperatorFactory 的 id（必填）
    refresh: # RedisCacheRefreshProvider 配置
      - id: lettuce # 要创建的 RedisCacheRefreshProvider 的 id（必填）
        factory: lettuce # 指定使用的 RedisOperatorFactory 的 id（必填）
    lettuce: # Lettuce 客户端配置
      factories: 
        - id: lettuce # RedisOperatorFactory 唯一标识
          sentinel: # 哨兵模式配置
            master-id: mymaster # 哨兵主节点名称
            nodes: 127.0.0.1:26379, 127.0.0.1:26380, 127.0.0.1:26381 # 哨兵节点列表
```

此配置使用 Lettuce 连接 Redis 哨兵节点，并使用 redis 实现缓存数据存储、数据同步、缓存锁、缓存指标采集等。

#### 极简配置示例

如果不使用 redis，可以进一步简化配置：

```yaml
xcache:
  group: shop # 分组名称 (必填)
  template: # 公共模板配置 (必填)
    - id: t0 # 模板ID (必填)
      second: # 二级缓存配置
        provider: none # 二级缓存默认是 lettuce，因为不使用，所以这里需设为 none。
```



## 模块说明

Xcache 拆分为很多子模块，一是为了避免引入不必要的依赖，二是便于自定义扩展实现。

| 项目名称                                  | 类型 | 项目说明                                                     |
| :---------------------------------------- | :--: | :----------------------------------------------------------- |
| xcache-parent                             | pom  | 所有子项目的最顶层父项目，主要用于统一的项目构建             |
| xcache-common                             | jar  | 基础公共模块，主要用于定义基础接口、数据对象和配置项         |
| xcache-core                               | jar  | 核心公共模块，主要用于实现具体的缓存逻辑                     |
| xcache-annotation                         | jar  | 缓存注解                                                     |
| xcache-caffeine                           | jar  | 使用 caffeine 实现内嵌缓存                                   |
| xcache-caffeine-spring-boot-autoconfigure | jar  | xcache-caffeine 模块的 Spring boot 自动配置                  |
| xcache-dependencies                       | pom  | 所有子项目的父项目，主要用于统一的依赖包管理                 |
| xcache-extension                          | pom  | 扩展模块的父项目                                             |
| xcache-extension-codec                    | jar  | 编解码模块的基础接口和基本实现，如希望开发自定义的编解码实现，可依赖此项目 |
| xcache-extension-common                   | jar  | 可扩展模块的基础接口和基本实现                               |
| xcache-extension-jackson                  | jar  | 使用 jackson 实现的编解码                                    |
| xcache-jackson-spring-boot-autoconfigure  | jar  | xcache-extension-jackson 模块的 Spring boot 自动配置         |
| xcache-lettuce-spring-boot-autoconfigure  | jar  |                                                              |
| xcache-redis                              | pom  |                                                              |
| xcache-redis-common                       | jar  | 如希望开发自定义的 Redis 客户端，可依赖此项目                |
| xcache-redis-core                         | jar  |                                                              |
| xcache-redis-jedis                        | jar  |                                                              |
| xcache-redis-lettuce                      | jar  |                                                              |
| xcache-redis-spring-boot-autoconfigure    | jar  |                                                              |
| xcache-spring                             | pom  | spring 相关项目的父项目                                      |
| xcache-spring-adapter                     | jar  |                                                              |
| xcache-spring-adapter-autoconfigure       | jar  |                                                              |
| xcache-spring-aop                         | jar  |                                                              |
| xcache-spring-boot-autoconfigure          | jar  |                                                              |
| xcache-spring-boot-starter                | jar  |                                                              |
| xcache-spring-boot-starter-test           | jar  | 主要用于 Xcache 注解的测试                                   |
| xcache-spring-adapter-test                | jar  | 主要用于 Spring cache 适配的测试                             |
| xcache-test                               | pom  | 所有测试项目的直接父项目                                     |
| xcache-test-base                          | jar  | 主要用于缓存方法的测试，与及基础接口的公共测试用例           |
| xcache-test-domain                        | jar  | 测试项目的数据对象定义                                       |



## 缓存键



## 缓存值



## 序列化



## 缓存锁



## 缓存刷新



## 数据加载

CacheLoader



## 数据回写

CacheLoader

## 

## 缓存模式



## 概念与定义

内嵌缓存 与 外部缓存

私有缓存 与 共享缓存

本地缓存 与 远程缓存

分散式缓存 与 集中式缓存

单实例缓存 与 分布式缓存

