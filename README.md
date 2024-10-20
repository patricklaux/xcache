## Xcache

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html) [![Release](https://img.shields.io/github/v/release/patricklaux/xcache)](https://github.com/patricklaux/xcache/releases) [![Maven](https://img.shields.io/maven-central/v/com.igeeksky.xcache/xcache-parent.svg)](https://central.sonatype.com/namespace/com.igeeksky.xcache) [![Last commit](https://img.shields.io/github/last-commit/patricklaux/xcache)](https://github.com/patricklaux/xcache/commits)

## 1. 简介

Xcache 是易于扩展、功能强大且配置灵活的 Java 多级缓存框架。

## 2. 架构

![Architecture](docs/images/architecture.png)

**说明**：

1. Cache：缓存实例。
2. CacheStore：缓存数据存储，每个缓存实例最多可支持三级缓存数据存储。
3. CacheStat：缓存指标计数，用于记录缓存方法调用次数及结果。
4. StatCollector：缓存指标统计信息的采集与发布（可选择发布到日志或 Redis）。
5. CacheSync：缓存数据同步，用于维护各个缓存实例的数据一致性。
6. MQ：消息队列，用于中转数据同步消息或缓存指标统计消息（已有实现采用 Redis Stream）。
7. CacheWriter：缓存数据回写，当缓存数据发生变化时，将数据写入到数据源。
8. CacheLoader：回源加载数据，当缓存无数据或需定期刷新时，从数据源加载新数据。
9. dataSource：数据源。



## 3. 特性

- 支持多种缓存模式：Cache-Aside，Read-Through，Write-Through，Write-Behind。
- 支持缓存数据同步：通过缓存事件广播，多个应用实例的缓存数据保持一致。
- 支持缓存指标统计：通过日志方式输出和 Redis Stream 方式输出，便于统计各种缓存指标。
- 支持随机存活时间：避免大量的 key 集中过期，导致数据源压力过大。
- 支持缓存自动刷新：自动刷新缓存数据，避免慢查询导致应用响应缓慢。
- 支持数据回源加锁：加锁确保相同的键同时仅有一个线程回源查询，降低回源次数，减轻数据源压力。
- 支持缓存数据压缩：可选择压缩数据，降低内存消耗。
- 支持多级缓存实现：一级缓存默认采用 Caffeine，二级缓存默认采用 Redis，最多可支持三级缓存。
- 支持数据存在断言：通过实现数据存在断言接口，譬如 Bloom Filter，避免回源查询。
- 支持缓存空值：当数据源确定无数据时，可缓存空值，避免缓存穿透。
- 虚拟线程特别优化：需要加锁执行或 IO 等待的定时任务，采用虚拟线程执行，降低平台线程资源占用。
- 更丰富的缓存注解：Cacheable，CacheableAll，CachePut，CachePutAll，…… ，CacheClear
- 适配 SpringCache：无需修改现有代码，添加 Xcache 依赖，即可支持更多缓存功能。

## 4. 运行环境

SpringBoot：3.3.0+

JDK：21+

## 5. 开始使用

以下代码片段来自于 [xcache-samples](https://github.com/patricklaux/xcache-samples)，如需获取更详细信息，您可以克隆示例项目到本地进行调试。

```bash
git clone https://github.com/patricklaux/xcache-samples.git
```

### 5.1. 第一步：引入缓存依赖

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

### 5.2. 第二步：编写缓存配置

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
  cache: # 缓存实例个性配置，列表类型，可配置零至多个
    - name: user # 缓存名称，用于区分不同的缓存对象
      template-id: t0 # 指定使用的模板为 t0（对应属性：xcache.template[i].id）
```

**说明**：

1. 同一应用中，一般会有多个不同名称的缓存实例，它们的配置通常大部分相同。

   为避免重复填写配置，可创建一个公共配置模板，缓存实例个性配置则只需填写与该模板的差异部分。

2. Xcache 提供了丰富的配置项，绝大多数都有默认值，因此可以省略而无需填写。

3. 每一个配置项都有详细介绍，可借助 ide 的自动提示功能快速查看相关描述信息。

### 5.3. 第三步：调用缓存方法

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

### 5.4. 更多使用信息

详细介绍：[Xcache 使用手册](docs/Reference.md)

## 6. 项目构建

如希望尝试新特性，可以将项目克隆到本地进行编译（需要 JDK21）。

```bash
# 1. git clone项目到本地
git clone https://github.com/patricklaux/xcache.git

# 2. 进入项目目录
cd xcache

# 3. 执行 maven 命令编译
mvn clean install

# xcache 测试用例依赖本地 redis，如构建时不希望运行测试用例，可执行：
# mvn clean install -DskipTests
```

## 7. 项目参与

### 7.1. 分支开发

| 分支     | 说明                  |
| -------- | --------------------- |
| **main** | 主分支，用于版本发布  |
| **dev**  | 开发分支，用于接受 PR |

如您希望参与开发，请 fork 项目到您的仓库，修改 dev 分支并提交 pr。

### 7.2. 寻求帮助

https://github.com/patricklaux/xcache/discussions

如您希望了解如何使用 xcache，或在使用中遇到问题无法解决，欢迎在此提问。

### 7.3. 建议反馈

https://github.com/patricklaux/xcache/issues

如您发现功能缺陷，或有任何开发建议，欢迎在此提交。

如您发现安全漏洞，请私信与我联系。

## 8. 许可证

Xcache 采用 Apache License Version 2.0 进行许可。有关详细信息，请参阅 [LICENSE](LICENSE) 文件。