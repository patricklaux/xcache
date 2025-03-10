## Xcache  Reference Guide

Author: Patrick.Lau Version: 1.0.0

## 1. 基本说明

### 1.1. 文档版本

此文档最新版本位于 https://github.com/patricklaux/xcache/blob/main/docs/Reference.md，如您有任何改进，非常欢迎您提交 pr。

### 1.2. 获取帮助

https://github.com/patricklaux/xcache/discussions

如您希望了解如何使用 xcache，或在使用中遇到问题无法解决，欢迎在此提问。

### 1.3. 建议反馈

https://github.com/patricklaux/xcache/issues

如您发现功能缺陷，或有任何开发建议，欢迎在此提交。

如您发现安全漏洞，请私信与我联系。

### 1.4. 项目测试

https://github.com/patricklaux/xcache/tree/main/xcache-test

如您希望扩展实现，又或者对某处代码逻辑有疑问，您可以参考此测试项目，并对相关实现进行调试。

当然，也欢迎您补充更多的测试用例。

## 2. 相关介绍

### 2.1. 简介

Xcache 是易于扩展、功能强大且配置灵活的 Java 多级缓存框架。

### 2.2. 架构

![Architecture](images/architecture.png)

**说明**：

* `Cache`：缓存实例。
* `CacheStore`：缓存数据存储，每个缓存实例最多可支持三级缓存数据存储。
* `MetricsMessage`：缓存指标信息，用于记录缓存调用次数及命中率等指标。
* `MetricsSystem`：缓存指标信息的收集、存储、计算与展示。
* `SyncMessage`：缓存数据同步信息，用于维护各个缓存实例的数据一致性。
* `MQ`：消息队列，用于转发数据同步消息（已有实现采用 `Redis Stream`）。
* `DataSource`：数据源，当缓存无数据时，从数据源加载数据并存入缓存。

### 2.3. 运行环境

`SpringBoot`：3.3.0+

`JDK`：21+

## 3. 项目示例

以下代码片段来自于 [xcache-samples](https://github.com/patricklaux/xcache-samples)，如需获取更详细信息，您可以克隆示例项目到本地进行调试。

```bash
git clone https://github.com/patricklaux/xcache-samples.git
```

### 3.0. Maven bom

Xcache 支持 bom 方式统一管理版本，可在 pom.xml 文件中添加如下配置片段，后续真正引入组件依赖时可省略版本号。

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.igeeksky.xcache</groupId>
            <artifactId>xcache-bom</artifactId>
            <version>${xcache.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 3.1. 缓存方法

详见 `xcache-samples-method` 子项目。

#### 3.1.1 第一步：引入依赖

如直接通过调用方法操作缓存，仅需引入 `xcache-spring-boot-starter` 模块。

主要组件：Caffeine（内嵌缓存），Lettuce（Redis 客户端），Jackson（序列化）

```xml

<dependencies>
    <dependency>
        <groupId>com.igeeksky.xcache</groupId>
        <artifactId>xcache-spring-boot-starter</artifactId>
    </dependency>
    <!-- ... other ... -->
</dependencies>
```

#### 3.1.2. 第二步：编写配置

```yaml
xcache: #【1】xcache 配置的根节点
  group: shop #【2】分组名称（必填），主要用于区分不同的应用
  template: #【3】缓存公共配置模板（必填），列表类型，可配置一至多个
    - id: t0 #【4】 模板ID（必填）
      first: #【5】 一级缓存配置
        provider: caffeine #【6】使用 caffeine 作为一级缓存（默认值：caffeine）
```

**说明**：

* 【1-4】仅有的 4 个必填项。`Xcache` 提供了丰富的配置项，大部分有默认值，因此可以省略。
* 【3~8】缓存公共配置模板：同一应用中，一般会有多个缓存实例，配置通常相同。为减少重复配置，可使用公共配置模板。

另，每一个配置项都有详细介绍，可借助 ide 的自动提示功能快速查看配置描述。

或直接查看 `com.igeeksky.xcache.props.CacheProps`，了解详细的配置信息。

#### 3.1.3. 第三步：调用方法

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
        return cache.getOrLoad(id, cacheLoader);
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
        return cache.getAllOrLoad(ids, this.cacheLoader);
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
        // cache.remove(user.getId());
        return updated;
    }

    /**
     * 批量更新用户信息
     *
     * @param users 待更新的用户信息集合
     * @return 保存到数据库后返回的用户信息集合
     */
    public Map<Long, User> updateUsers(List<User> users) {
        Map<Long, User> updated = userDao.batchUpdate(users);
        // 将更新后的用户信息写入缓存
        cache.putAll(updated);
        // 如果为了更好地保持数据一致性，这里可选择直接删除缓存数据，后续查询时再从数据源加载
        // cache.removeAll(updated.keySet());
        return updated;
    }

    /**
     * 删除用户信息
     *
     * @param id 用户ID
     */
    public void deleteUser(Long id) {
        userDao.delete(id);
        // 删除缓存数据
        cache.remove(id);
    }

    /**
     * 批量删除用户信息
     *
     * @param ids 用户ID集合
     */
    public void deleteUsers(Set<Long> ids) {
        userDao.batchDelete(ids);
        // 批量删除缓存数据
        cache.removeAll(ids);
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

提示：

> `CacheLoader` 有两个接口：一是 `load(key)`，用于单个回源取值；二是 `loadAll(keys)`，用于批量回源取值。
>
> 单个回源取值时加锁，批量回源取值时不加锁（批量回源加锁可能导致死锁）。

#### 3.1.4. 小结

此示例演示了如何通过直接调用缓存方法来使用缓存。

缓存方法使用其实是很简单的，类似于 `Map` 的 `API` 。

这里演示的仅仅是同步调用方式，另外还有异步 `API`，只要在方法名称后面加上 `Async`，返回结果将变成 `CompleteFuture`。

### 3.2. Xcache 注解

上一示例中，仅仅使用了 `caffeine` 作为一级缓存，这一节将使用 `caffeine` 和 `redis` 创建两级缓存，并介绍如何使用缓存注解。

详见 `xcache-samples-annotation` 子项目。

#### 3.2.1. 第一步：引入依赖

使用 Xcache 注解，除了依赖 `xcache-spring-boot-starter`，还需引入 `xcache-spring-aop`。

```xml

<dependencies>
    <dependency>
        <groupId>com.igeeksky.xcache</groupId>
        <artifactId>xcache-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.igeeksky.xcache</groupId>
        <artifactId>xcache-spring-aop</artifactId>
    </dependency>
    <!-- ... other ... -->
</dependencies>
```

#### 3.2.2. 第二步：编写配置

```yaml
xcache: #【1】 xcache 配置根节点
  group: shop #【2】分组名称（必填），主要用于区分不同的应用
  template: #【3】公共模板配置（必填），列表类型，可配置一至多个
    - id: t0 #【4】模板ID（必填）
      first: #【5】一级缓存配置
        provider: caffeine #【6】使用 id 为 caffeine 的 StoreProvider 创建一级缓存实例
      second: #【7】二级缓存配置
        provider: lettuce #【8】使用 id 为 lettuce 的 StoreProvider 创建二级缓存实例（即【11】中设定的 id）
  redis: #【9】Redis 配置
    lettuce: #【10】Lettuce 配置
      - id: lettuce #【11】RedisOperatorFactory 唯一标识
        standalone: #【12】单机模式（或副本集模式）配置
          node: 127.0.0.1:6379 #【13】节点地址
```

**说明**：

1. 首先，【9~13】增加 `xcache.redis` 配置。

   根据配置创建 `RedisOperatorFactory` 对象实例，此实例的 id 设定为 “lettuce”。

   基于此 `RedisOperatorFactory` ，创建相同 id 的 `RedisStoreProvider` 对象实例。

3. 最后，【8】二级缓存指定使用 id 为 "lettuce" 的 `StoreProvider`。

**常见问题**：

2. **Q**：为什么 `Redis` 配置要设计成列表类型？

   **A**：一个复杂应用连接多套 Redis 服务是比较常见的，设计成列表可以让用户不用再手写配置类来扩展。

3. **Q**：为什么 id 为 "caffeine" 的 `StoreProvider` 没有通过配置创建却可以直接使用？

   **A**：因为 `CaffeineStoreProvider` 不依赖外部服务，创建该对象也无需任何参数，因此在应用启动时就自动创建了 id 为
   “caffeine” 的 `CaffeineStoreProvider` 。

   而 `RedisStoreProvider` 则依赖于外部服务，需配置必要的连接信息，且可能有多个实例，因此需显式配置。

3. **Q**：id 可以自行设定吗？

   **A**：`CaffeineStoreProvider` 的 id 是系统指定无法修改；`RedisStoreProvider` 的 id 则完全由用户设定。

#### 3.2.3. 第三步：使用注解

**启用缓存注解：@EnableCache**

```java
import com.igeeksky.xcache.aop.EnableCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Xcache 注解示例
 */
// 启用缓存注解
@EnableCache(basePackages = "com.igeeksky.xcache.samples")
@SpringBootApplication(scanBasePackages = "com.igeeksky.xcache.samples")
public class AnnotationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnnotationApplication.class, args);
    }

}
```

**代码示例**

```java
// 引入 Xcache 注解

import com.igeeksky.xcache.annotation.*;
// ………… 省略其它

/**
 * 用户缓存服务
 */
@Service
// @CacheConfig 统一配置缓存注解公共参数
@CacheConfig(name = "user", keyType = Long.class, valueType = User.class)
public class UserCacheService {

    private static final Logger log = LoggerFactory.getLogger(UserCacheService.class);

    private final UserDao userDao;

    public UserCacheService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * 获取单个用户信息
     * <p>
     * {@code Cacheable} 注解，对应 {@code V value = cache.get(K key, CacheLoader<K,V> loader) } 方法。
     * <p>
     * 如未配置 key 表达式，采用方法的第一个参数作为缓存键；如已配置 key 表达式，解析该表达式提取键.
     *
     * @param id 用户ID
     * @return {@code User} – 用户信息
     */
    @Cacheable
    public User getUser(Long id) {
        return userDao.findUser(id);
    }

    /**
     * 获取单个用户信息
     *
     * @param id 用户ID
     * @return {@code Optional<User>} – 用户信息 <br>
     * 如果检测到方法返回值类型为 {@code Optional}，缓存实现会自动采用 {@code Optional.ofNullable()} 包装返回值.
     */
    @Cacheable
    public Optional<User> getOptionalUser(Long id) {
        User user = userDao.findUser(id);

        // 错误：方法返回值为 Optional 类型时，当用户不存在，直接返回 null
        // return user == null ? null : Optional.of(user);

        // 正确：使用 Optional.ofNullable(value) 包装可能为空的值
        return Optional.ofNullable(user);
    }

    /**
     * 获取单个用户信息
     *
     * @param id 用户ID
     * @return {@code CompletableFuture<User>} – 用户信息 <br>
     * 如果检测到方法返回值类型为 {@code CompletableFuture}，缓存实现会自动采用 {@code CompletableFuture.completedFuture()} 包装返回值.
     */
    @Cacheable
    public CompletableFuture<User> getFutureUser(Long id) {
        User user = userDao.findUser(id);

        // 错误：方法返回值为 CompletableFuture 类型时，当用户不存在，直接返回 null
        // return user == null ? null : CompletableFuture.completedFuture(user);

        // 正确：使用 CompletableFuture.completedFuture 包装可能为空的值
        return CompletableFuture.completedFuture(user);
    }

    /**
     * 批量获取用户信息
     * <p>
     * {@code CacheableAll} 注解，对应 {@code Map<K,V> results = cache.getAll(Set<K> keys, CacheLoader<K,V> loader) }方法.<p>
     * 缓存的键集：Set 类型。如未配置 keys 表达式，采用方法的第一个参数作为键集；如已配置 keys 表达式，解析该表达式提取键集.<p>
     * 缓存结果集：Map 类型.
     *
     * @param ids 用户ID集合
     * @return {@code Map<Long, User>} – 用户信息集合
     */
    @CacheableAll
    public Map<Long, User> getUsers(Set<Long> ids) {
        log.debug("getUsers: {}", ids);
        return userDao.findUserList(ids);
    }

    /**
     * 批量获取用户信息
     *
     * @param ids 用户ID集合
     * @return {@code Optional<Map<Long, User>>} – 用户信息集合 <br>
     * 如果检测到方法返回值类型为 {@code Optional}，缓存实现会自动采用 {@code Optional.ofNullable()} 包装返回值.
     */
    @CacheableAll
    public Optional<Map<Long, User>> getOptionalUsers(Set<Long> ids) {
        log.debug("getOptionalUsers: {}", ids);
        return Optional.ofNullable(userDao.findUserList(ids));
    }

    /**
     * 批量获取用户信息
     *
     * @param ids 用户ID集合
     * @return {@code CompletableFuture<Map<Long, User>>} – 用户信息集合 <br>
     * 如果检测到方法返回值类型为 {@code CompletableFuture}，缓存实现会自动采用 {@code CompletableFuture.completedFuture()} 包装返回值.
     */
    @CacheableAll(keys = "#ids")
    public CompletableFuture<Map<Long, User>> getFutureUsers(Set<Long> ids) {
        log.debug("getFutureUsers: {}", ids);
        return CompletableFuture.completedFuture(userDao.findUserList(ids));
    }

    /**
     * 新增用户信息
     * <p>
     * {@code CachePut} 注解，对应 {@code cache.put(K key, V value)} 方法.<p>
     * 如未配置 key 表达式，采用方法的第一个参数作为缓存键；如已配置 key 表达式，解析该表达式提取键.<p>
     * 如未配置 value 表达式，采用方法返回结果作为缓存值；如已配置 value 表达式，解析该表达式提取值.
     *
     * @param user 用户信息（无ID）
     * @return {@code User} – 用户信息（有ID）
     */
    @CachePut(key = "#result.id")
    public User saveUser(User user) {
        return userDao.save(user);
    }

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return {@code User} – 用户信息
     */
    @CachePut(key = "#user.id", value = "#user")
    public User updateUser(User user) {
        return userDao.update(user);
    }

    /**
     * 批量更新用户信息
     * <p>
     * {@code CachePutAll} 注解， 对应 {@code cache.putAll(Map<K,V> keyValues) }方法.<p>
     * 如未配置 keyValues 表达式，默认采用方法返回值；如已配置 keyValues 表达式，解析该表达式提取键值对集合.
     *
     * @param users 用户信息列表
     * @return {@code Map<Long, User>} – 用户信息集合
     */
    @CachePutAll
    public Map<Long, User> updateUsers(List<User> users) {
        return userDao.batchUpdate(users);
    }

    /**
     * 删除用户信息
     * <p>
     * {@code CacheRemove} 注解，对应 {@code cache.remove(K key)} 方法.
     *
     * @param id 用户ID
     */
    @CacheRemove
    public void deleteUser(Long id) {
        userDao.delete(id);
    }

    /**
     * 批量删除用户信息
     * <p>
     * {@code CacheRemoveAll} 注解，对应 {@code cache.removeAll(Set<K> keys) }方法.
     *
     * @param ids 用户ID集合
     */
    @CacheRemoveAll
    public void deleteUsers(Set<Long> ids) {
        userDao.batchDelete(ids);
    }

    /**
     * 清空数据
     * <p>
     * {@code CacheClear} 注解，对应 {@code cache.clear()} 方法.
     */
    @CacheClear
    public void clear() {
        userDao.clear();
    }

}
```

#### 3.2.4. 小结

此示例演示了如何使用 Xcache 缓存注解。

1. 公共参数

   `Xcache` 的方法级缓存注解一共有 7 个：`@Cacheable`，`@CacheableAll`，`@CachePut`，`@CachePutAll`，`@CacheRemove`，
   `@CacheRemoveAll`，`@CacheClear`。

   这些注解均有 3 个公共参数：`name`，`keyType`，`valueType`。

   如果一个类中有同名缓存的多个缓存方法注解，可以使用类注解 `@CacheConfig` 统一配置公共参数。

3. 被注解方法的返回值类型

   对于 `@Cacheable` 和 `@CacheableAll` 注解，被注解方法的返回值类型除了与缓存结果类型保持一致，还可以是 `Optional` 或
   `CompletableFuture` 类型。

   如果是  `Optional`  类型，缓存会自动用 `Optional.ofNullable(value)` 包装缓存中获取到的值。

   如果是 `CompletableFuture` 类型，缓存会自动用 `CompletableFuture.completedFuture(value)`  包装缓存中获取到的值。

3. 每个注解的参数配置和执行逻辑见 [5. Xcache 注解](#5. 缓存注解)

### 3.3. Spring cache 注解

详见 `xcache-samples-spring-annotation` 子项目。

如既想用 `Spring cache` 注解，又想要 `Xcache` 相对丰富的功能特性，那么，`Xcache` 提供了  `Spring cache` 适配模块。

#### 3.3.1. 第一步：引入依赖

使用 Spring cache 注解，除了需引入 `xcache-spring-boot-starter`，还需引入 `xcache-spring-adapter-autoconfigure`。

```xml

<dependencies>
    <dependency>
        <groupId>com.igeeksky.xcache</groupId>
        <artifactId>xcache-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.igeeksky.xcache</groupId>
        <artifactId>xcache-spring-adapter-autoconfigure</artifactId>
    </dependency>
    <!-- ... other ... -->
</dependencies>
```

#### 3.3.2. 第二步：编写配置

```yaml
xcache: #【1】Xcache 配置的根节点
  group: shop #【2】分组名称（必填），主要用于区分不同的应用
  template: #【3】公共模板配置（必填），列表类型，可配置一至多个
    - id: t0 #【4】模板ID（必填）
      key-codec: jackson-spring #【5】键转为 String（需设为适配 Spring cache 的 jackson-spring）
      first: #【6】一级缓存配置
        provider: caffeine #【7】使用 id 为 caffeine 的 StoreProvider 作为一级缓存
      second: #【8】二级缓存配置
        provider: lettuce #【9】使用 id 为 lettuce 的 StoreProvider 作为二级缓存（即【16】中设定的 id）
        value-codec: jackson-spring #【10】值序列化（需设为适配 Spring cache 的 jackson-spring）
  cache: #【11】缓存个性配置，列表类型，可配置零至多个
    - name: user #【12】缓存名称，用于区分不同的缓存对象
      template-id: t0 #【13】指定使用的模板为 t0（即【4】中设定的 id）
  redis: #【14】Redis 配置
    lettuce: #【15】Lettuce 配置
      - id: lettuce #【16】RedisOperatorFactory 唯一标识
        standalone: #【17】单机模式（或副本集模式）配置
          node: 127.0.0.1:6379 #【18】节点地址
```

与上一示例的配置相比，这份配置显式指定了序列化实现：

1. 【5】`key-codec`，需显式设定为适配 Spring cache 的 “jackson-spring”（默认是 “jackson”）。
2. 【10】`value-codec`，需显式设定为适配 Spring cache 的 “jackson-spring”（默认是 “jackson”）。

这是因为 Spring cache 注解无法配置对象类型，因此序列化数据需携带类型信息，这样在反序列化时才能确定对象类型。

譬如示例项目的 `User` 对象，当配置为 “jackson-spring” 时，序列化数据会多出 “@class” 属性：

```json
{
  "@class": "com.igeeksky.xcache.samples.User",
  "id": 1,
  "name": "Jack1",
  "age": 18
}
```

> 特别提示：
>
> 使用 Spring cache 注解唯一需要特别修改的就是序列化配置，其余配置完全相同。

#### 3.3.3. 第三步：使用注解

**启用缓存注解：@EnableCaching**

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Spring Cache 注解示例
 */
@EnableCaching
@SpringBootApplication(scanBasePackages = "com.igeeksky.xcache.samples")
public class SpringAnnotationApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAnnotationApplication.class, args);
    }

}
```

**代码示例**

```java
// 引入 Spring cache 注解

import org.springframework.cache.annotation.*;
// ………… 省略其它

/**
 * 用户缓存服务
 */
@Service
// 适配 Spring cache 的 CacheManager 为 springCacheManager，如 Spring 容器内无其它 CacheManager 对象，可不配置。
@CacheConfig(cacheNames = "user", cacheManager = "springCacheManager")
public class UserCacheService {

    private final UserDao userDao;

    public UserCacheService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * 获取单个用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Cacheable(key = "#id")
    public User getUser(Long id) {
        return userDao.findUser(id);
    }

    /**
     * 获取单个用户信息
     *
     * @param id 用户ID
     * @return {@code Optional<User>} – 用户信息
     */
    @Cacheable(key = "#id")
    public Optional<User> getOptionalUser(Long id) {
        return Optional.ofNullable(userDao.findUser(id));
    }

    /**
     * 获取单个用户信息
     *
     * @param id 用户ID
     * @return {@code CompletableFuture<User>} – 用户信息
     */
    @Cacheable(key = "#id")
    public CompletableFuture<User> getFutureUser(Long id) {
        return CompletableFuture.completedFuture(userDao.findUser(id));
    }

    /**
     * 获取单个用户信息
     *
     * @param id 用户ID
     * @return {@code Mono<User>} – 用户信息
     * <p>
     * Spring cache 适配 Reactor 的 Mono 类型，因此可以返回 {@code Mono<User>}。
     */
    @Cacheable(key = "#id")
    public Mono<User> getMonoUser(Long id) {
        return Mono.fromSupplier(() -> userDao.findUser(id));
    }

    /**
     * 新增用户信息
     *
     * @param user 用户信息
     * @return 用户信息
     */
    @CachePut(key = "#result.id")
    public User saveUser(User user) {
        return userDao.save(user);
    }

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 用户信息
     */
    @CachePut(key = "#result.id")
    public User updateUser(User user) {
        return userDao.update(user);
    }

    /**
     * 删除用户信息
     *
     * @param id 用户ID
     */
    @CacheEvict(key = "#id")
    public void deleteUser(Long id) {
        userDao.delete(id);
    }

    /**
     * 清空数据
     */
    @CacheEvict(allEntries = true)
    public void clear() {
        userDao.clear();
    }

}
```

#### 3.3.4. 小结

此示例演示了如何引入适配依赖将 `Xcache` 作为 `Spring cache` 的接口实现，并通过 `Spring cache` 注解操作缓存。

1. `Spring Cache` 没有 `@CacheableAll`，`@CachePutAll`，`@CacheRemoveAll` 这三个批处理注解。

2. `Spring Cache` 没有写 key 表达式时，不是使用方法的第一个参数作为键，而是使用所有参数生成 `SimpleKey` 对象作为键。

3. 对已使用 `Spring cache` 注解的项目，只需引入 `Xcache` 相关依赖，几乎不用改动代码，就可将缓存实现替换成 `Xcache`。

## 4. 缓存配置

作为开源框架项目，关于配置项，我在设计时遵循三个基本原则：

1. 尽可能多配置项：可全面控制缓存的各种功能逻辑，进行缓存性能优化；
2. 尽可能少写配置：通过提供默认值和公共配置模板，减少显式书写配置；
3. 尽可能不改代码：可通过调整配置和增减依赖组件，灵活适配业务逻辑。

### 4.1. 总体介绍

所有配置项大概可分为三部分：一是缓存核心配置，二是 Redis 配置，三是其它配置。

#### 4.1.1. 核心配置

核心配置部分，用于创建 Cache 对象。

Cache 对象可能依赖于“Redis 配置”或“其它配置”中创建的对象。

| 类别              | 名称       | 说明                                                                                |
|-----------------|----------|-----------------------------------------------------------------------------------|
| xcache.group    | 组名       | 用于区分不同的应用，主要是为了避免键冲突。<br />譬如，当一个 Redis 被多个应用共用，每个应用可设定不同的 group 作为缓存键前缀，从而避免键冲突。 |
| xcache.template | 缓存公共模板配置 | 除了缓存名称外，同一应用的各个缓存实例的其它配置是相似的。<br />因此可配置一个或多个公共模板，从而避免每一个缓存实例重复配置。                |
| xcache.cache    | 缓存实例个性配置 | 如配置项与公共模板配置完全相同，可以省略全部配置。<br />如配置项与公共模板配置部分不同，只需配置差异部分。                          |

**相关配置类**：

| 所在项目                             | 配置类                                                 |
|----------------------------------|-----------------------------------------------------|
| xcache-spring-boot-autoconfigure | `com.igeeksky.xcache.autoconfigure.CacheProperties` |

#### 4.1.2. Redis 配置

Redis 配置部分，用于创建 Redis 相关的对象。

| 类别                                 | 名称                | 说明                                                                                                                                                                                                                                       |
|------------------------------------|-------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| xcache.redis.lettuce[i].id         | redis 相关对象唯一标识    | `RedisOperatorFactory` <br /> `StreamContainer` <br /> `RedisStoreProvider` <br /> `RedisCacheSyncProvider` <br /> `RedisLockProvider` <br /> `RedisCacheRefreshProvider` <br /> `RedisCacheMetricsProvider` <br /> 这些对象都将使用此 id 作为唯一标识。 |
| xcache.redis.lettuce[i].batchSize  | Redis 单批次命令提交数量阈值 | 大批量操作 Redis 缓存数据时，<br />将会切分成多批次进行操作                                                                                                                                                                                                     |
| xcache.redis.lettuce[i].stream     | Redis Stream 配置   | 创建 `StreamContainer` 对象 <br />用于拉取缓存数据同步消息。                                                                                                                                                                                              |
| xcache.redis.lettuce[i].sync       | Redis 缓存数据同步配置    | 创建 `RedisCacheSyncProvider` 对象 <br />用于处理缓存数据同步。                                                                                                                                                                                         |
| xcache.redis.lettuce[i].metrics    | Redis 缓存指标统计配置    | 创建 `RedisCacheMetricsProvider`对象<br />用于发送缓存指标数据到 Redis。                                                                                                                                                                                 |
| xcache.redis.lettuce[i].standalone | Lettuce 客户端配置     | 创建 `RedisOperatorFactory` 对象<br />使用 lettuce 客户端连接 Redis。<br />单机模式或副本集模式                                                                                                                                                                |
| xcache.redis.lettuce[i].sentinel   | Lettuce 客户端配置     | 创建 `RedisOperatorFactory` 对象<br />使用 lettuce 客户端连接 Redis。<br />哨兵模式                                                                                                                                                                      |
| xcache.redis.lettuce[i].cluster    | Lettuce 客户端配置     | 创建 `RedisOperatorFactory` 对象<br />使用 lettuce 客户端连接 Redis。<br />集群模式                                                                                                                                                                      |

> **注意**：`standalone`，`sentinel`，`cluster` 仅需配置其中一个。
>
> 如果三者中任意两个配置存在，读取优先级为： `sentinel` > `cluster` > `standalone`。

**相关配置类**：

| 所在项目                                   | 配置类                                                            |
|----------------------------------------|----------------------------------------------------------------|
| xcache-redis-spring-boot-autoconfigure | com.igeeksky.xcache.autoconfigure.redis.LettuceCacheProperties |

#### 4.1.3. 其它配置

其它配置部分，用于创建无外部服务依赖的一些对象。

| 类别                          | 名称       | 说明                                                                                                                                                                                       |
|-----------------------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| xcache.log-metrics-interval | 缓存指标统计配置 | 用于创建 `LogCacheMetricsProvider` 对象<br />当前 Xcache 的缓存指标统计支持两种输出方式：<br />一是将缓存指标数据输出到日志，二是将缓存指标数据发送到 Redis。<br />此配置项仅用于日志输出方式；如希望使用 Redis 进行指标数据采集，需在 xcache.redis.lettuce[i].metrics 配置。 |
| xcache.scheduler            | 任务调度器配置  | 用于创建 `ScheduledExecutorService` 对象<br />缓存数据定时刷新、缓存指标定时采集和发送均依赖于此调度器。                                                                                                                    |

**相关配置类**：

| 所在项目                             | 配置类                                                   |
|----------------------------------|-------------------------------------------------------|
| xcache-spring-boot-autoconfigure | com.igeeksky.xcache.autoconfigure.CacheProperties     |
| xcache-spring-boot-autoconfigure | com.igeeksky.xcache.autoconfigure.SchedulerProperties |

#### 4.1.4. 小结

这一章节主要介绍了大的配置类别，具体的细项配置可参见 [4.3.1. 完全配置](#4.3.1. 完全配置)。

如果想继续了解 Xcache 有哪些主要的对象类型，哪些需要配置，哪些无需配置，请参见下一节：[4.2. 对象创建与使用](#4.2.
对象创建与使用)

### 4.2. 对象创建

#### 4.2.1. 对象类型

Cache 对象需要用到不同类型的对象来完成不同的功能。

譬如缓存数据用 Jackson 进行序列化操作，那么就需要先创建一个 `JacksonCodecProvider` 对象；

譬如一级缓存用 Caffeine 作为缓存数据存储，那么就需要先创建一个 `CaffeineStoreProvider` 对象；

譬如二级缓存用 Redis 作为缓存数据存储，那么就需要先创建一个 `RedisStoreProvider` 对象；而 `RedisStoreProvider` 对象，又需要用到
`RedisOperatorFactory`，那么就需要先创建一个 `RedisOperatorFactory` 对象。

…………

对象类型汇总信息可见下表：

| 功能         | 接口                      | 实现类                         |       id       |                        对象依赖                        |
|------------|-------------------------|-----------------------------|:--------------:|:--------------------------------------------------:|
| 序列化        | CodecProvider           | JacksonCodecProvider        |    jackson     |                         无                          |
| 序列化        | CodecProvider           | GenericJacksonCodecProvider | jackson-spring |                         无                          |
| 序列化        | CodecProvider           | JdkCodecProvider            |      jdk       |                         无                          |
| 数据压缩       | CompressorProvider      | DeflaterCompressorProvider  |    deflate     |                         无                          |
| 数据压缩       | CompressorProvider      | GzipCompressorProvider      |      gzip      |                         无                          |
| 缓存锁        | CacheLockProvider       | EmbedCacheLockProvider      |     embed      |                         无                          |
| 缓存锁        | CacheLockProvider       | RedisLockProvider           |      自定义       |                RedisOperatorFactory                |
| 缓存数据刷新     | CacheRefreshProvider    | EmbedCacheRefreshProvider   |     embed      |              ScheduledExecutorService              |
| 缓存数据刷新     | CacheRefreshProvider    | RedisCacheRefreshProvider   |      自定义       | RedisOperatorFactory<br />ScheduledExecutorService |
| 缓存指标统计     | CacheMetricsProvider    | LogCacheMetricsProvider     |      log       |              ScheduledExecutorService              |
| 缓存指标统计     | CacheMetricsProvider    | RedisCacheMetricsProvider   |      自定义       | RedisOperatorFactory<br />ScheduledExecutorService |
| 缓存数据同步     | CacheSyncProvider       | RedisCacheSyncProvider      |      自定义       |              StreamListenerContainer               |
| 缓存存储       | StoreProvider           | CaffeineStoreProvider       |    caffeine    |                         无                          |
| 缓存存储       | StoreProvider           | RedisStoreProvider          |      自定义       |                RedisOperatorFactory                |
| 消息监听       | StreamListenerContainer | StreamListenerContainer     |      自定义       |                RedisOperatorFactory                |
| Redis 命令操作 | RedisOperatorFactory    | LettuceOperatorFactory      |      自定义       |                         无                          |
| 执行定时任务     | ExecutorService         | ScheduledExecutorService    |       无        |                         否                          |

**常见问题**：

**Q**：id 是否能重复？

**A**：*实现同一接口的对象的 id 不能重复*。

Xcache 内部，每个接口类型使用一个 Map 作为对象容器，id 作为键，对象实例作为值。所以，不同接口的对象 id 可以重复，相同接口的对象
id 不能重复。

譬如， `CaffeineStoreProvider` 的 id 已经被预设为 "caffeine"，那么  `RedisStoreProvider` 就不能再设为 caffeine，因为这两个类都实现了
`StoreProvider` 接口。

又譬如， `RedisOperatorFactory` 的 id 设为自定义的 "lettuce"，其它如 `RedisStoreProvider` ，`RedisLockProvider` …… 等的 id
也都是 "lettuce"，因为这些类实现的是不同的接口。

**Q**：为什么有些接口名称的后缀为 “provider” 或 “factory”？

**A**：“provider” 或 “factory” 结尾的是工厂类，真正执行具体功能的是这些工厂类创建的对象。

### 4.3. 完全配置

1. 除了标注必填的选项，其他选项均为选填，删除或留空表示使用默认配置；
2. 每个配置项均有详细说明，可用 ide 自动提示功能快速查看相关描述信息；
3. 如无法通过 ide 查看描述信息，可查看相关配置类，每一项均有详细注释。

```yaml
xcache:
  group: shop # 分组名称 (必填)
  template: # 公共模板配置 (必填，仅需配置与默认配置不同的部分)，列表类型，可配置多个模板。
    - id: t0 # 模板ID (必填)，建议将其中一个模板的 id 配置为 t0。
      charset: UTF-8 # 字符集 (默认 UTF-8)
      cache-lock: # 缓存锁配置
        provider: lettuce # LockProviderId（默认值：embed）
        initial-capacity: 128 # HashMap 初始容量（默认值：256）
        lease-time: 1000 # 锁租期 （默认值：1000 毫秒）
        enable-group-prefix: true # 是否添加 group 作为前缀（默认值：true）
        params: # 用于自定义扩展实现的非标参数，map 类型 （如不使用，请删除，否则会提示参数读取异常）
          test: test
      cache-metrics: log # CacheMetricsProviderId，用于缓存指标信息采集和输出（默认值：log，输出到日志）
      cache-refresh: # 缓存刷新配置
        provider: none # CacheRefreshProviderId（默认值：none，不启用缓存刷新）
        enable-group-prefix: true # 是否添加 group 作为前缀（默认值：true，适用于外部刷新实现）
        refresh-after-write: 10000 # （默认值：3600000 毫秒）
        refresh-tasks-size: 16384
        refresh-sequence-size: 16 # 刷新键序列数量（默认值：16），适用于 Redis 集群模式，其它模式下此配置无效
        refresh-thread-period: 10000 # 刷新间隔周期（默认值：10000 毫秒）
        shutdown-timeout: 2000
        shutdown-quiet-period: 100
        shutdown-behavior: cancel # 缓存刷新关闭行为（默认值：cancel，取消刷新线程）
        params: # 用于自定义扩展实现的非标参数，map 类型 （如不使用，请删除，否则会提示参数读取异常）
          test: test
      cache-sync: # 缓存同步配置
        provider: lettuce # CacheSyncProviderId （默认值：none）
        first: true # 一级缓存数据同步（默认值：true，如仅有一级缓存，请改为 false）
        second: false # 二级缓存数据同步（默认值：false）
        enable-group-prefix: true # 是否添加 group 作为前缀（默认值：true）
        max-len: 1000 # 缓存同步队列最大长度 （默认值：10000）
        params: # 用于自定义扩展实现的非标参数，map 类型 （如不使用，请删除，否则会提示参数读取异常）
          test: test
      key-codec: jackson # 用于将键转换成 String（默认值：jackson）
      first: # 一级缓存配置
        provider: caffeine # StoreProviderId（默认值：caffeine）
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
        params: # 用于自定义扩展实现的非标参数，map 类型 （如不使用，请删除，否则会提示参数读取异常）
          test: test
      second: # 二级缓存配置
        provider: lettuce # StoreProviderId（默认值：none）
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
        params: # 用于自定义扩展实现的非标参数，map 类型 （如不使用，请删除，否则 spring boot 会提示参数读取异常）
          test: test
      third: # 三级缓存配置
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
        params: # 用于自定义扩展实现的非标参数，map 类型 （如不使用，请删除，否则会提示参数读取异常）
          test: test
  cache: # 缓存配置（template 是公共配置，cache 是具体缓存个性配置，仅需配置与对应 template 不同的部分）
    - name: user # 缓存名称
      template-id: t0 # 模板id（默认值：t0，如未配置，默认从 id 为 t0 的模板中复制配置项）
      # …… 其余配置项与模板配置相同，所以直接省略
      # 另，如此缓存配置与 id 为 t0 的模板配置完全相同，name 与 template-id 其实也可以省略。
    - name: order # 缓存名称
      template-id: t0 # 模板id（默认值：t0，如未配置，默认从 id 为 t0 的模板中复制配置项）
      # …… 其余配置项与模板配置相同，所以直接省略
      # 另，如此缓存配置与 id 为 t0 的模板配置完全相同，name 与 template-id 其实也可以省略。
  log-metrics-interval: 60000 # 缓存指标采集的间隔时长，仅用于 LogCacheMetricsProvider（默认值：60000 单位：毫秒）
  scheduler: # 调度器配置
    core-pool-size: 1 # 定时任务调度器核心线程数，如未配置，则使用 (核心数 / 8)，最小为 1。
  redis: # Redis 配置（如不使用 Redis，可直接删除此配置项；如未配置，则不会生成相应的实例对象）
    lettuce: # Lettuce 客户端配置
      - id: lettuce # RedisOperatorFactory 唯一标识（默认值：lettuce）
        batch-size: 10000 # 单批次命令提交数量阈值（默认值：10000）
        metrics: # RedisCacheMetricsProvider 配置
          # 另，Redis StreamPublisher 仅负责发送统计指标信息，统计汇总需用户自行实现
          interval: 60000 # 缓存指标统计的时间间隔（默认值：60000，单位：毫秒）
          max-len: 10000 # Redis stream 最大长度（默认值：10000，采用近似裁剪，实际长度可能略大于配置值）
          charset: UTF-8 # 字符集，用于缓存统计指标消息的编解码（默认值：UTF-8）
          codec: jackson # 统计消息编解码
          enable-group-prefix: false # 是否附加 group 作为后缀（默认值：false）
        stream: # StreamContainer 配置
          block: -1 # 读取 Stream 时的阻塞时长（默认值： 10 单位：毫秒）
          count: 1000 # 同步任务每次从 Stream 读取的最大消息数量（默认值： 1000）
          interval: 10 # 当次同步任务结束后，下次任务开始前的间隔时长（默认值： 10 单位：毫秒）
        sync:
          codec: jackson # 缓存数据同步消息编解码
        standalone: # 单机模式 或 副本集模式
          # 这里为了演示 standalone，sentinel，cluster 分别如何配置，所以三种配置都存在，实际只需保留真正使用的其中一种
          # 当三种配置都存在时，那么优先使用 sentinel 配置，其次 cluster，最后 standalone
          node: 127.0.0.1:6379 # Redis 节点，支持 UnixSocket 方式
          nodes: socket:/tmp/redis.sock, 127.0.0.1:6378 # Redis 节点列表
          read-from: # 读节点选择策略（默认值：UPSTREAM，仅从主节点读取数据）
          username: # Redis 用户名
          password: # Redis 密码
          database: 0 # Redis 数据库索引（默认值：0）
          client-name: # Redis 客户端名称
          ssl: false # 是否启用 SSL（默认值：false）
          start-tls: false # 是否启用 TLS（默认值：false）
          ssl-verify-mode: FULL # SSL 验证模式，只有 ssl 配置为 true 时才有意义（默认值：FULL）
          timeout: 60000 # 同步执行命令等待完成的最大时长（默认值：60000 单位：毫秒）
          shutdown-timeout: 2000
          shutdown-quiet-period: 100
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
              fixedTimeout: # 超时时间设为固定值（默认值：-1，单位：毫秒，即使用连接超时）
        sentinel: # 哨兵模式配置
          # 这里为了演示 standalone，sentinel，cluster 分别如何配置，所以三种配置都存在，实际只需保留真正使用的其中一种
          # 当三种配置都存在时，那么优先使用 sentinel 配置，其次 cluster，最后 standalone
          master-id: mymaster # 哨兵主节点名称
          nodes: 127.0.0.1:26377, 127.0.0.1:26378, 127.0.0.1:26379 # 哨兵节点列表
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
          shutdown-timeout: 2000
          shutdown-quiet-period: 100
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
              fixedTimeout: # 超时时间设为固定值（默认值：-1，单位：毫秒，即使用连接超时）
        cluster: # 集群模式配置
          # 这里为了演示 standalone，sentinel，cluster 分别如何配置，所以三种配置都存在，实际只需保留真正使用的其中一种
          # 当三种配置都存在时，那么优先使用 sentinel 配置，其次 cluster，最后 standalone
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
          shutdown-timeout: 2000
          shutdown-quiet-period: 100
          client-options: # 集群客户端选项，比一般客户端选项多了重定向、拓扑刷新等配置
            auto-reconnect: true # 是否自动重连（默认值：true）
            disconnected-behavior: DEFAULT # 连接断开后是否接受命令（默认值：DEFAULT）
            publish-on-scheduler: false # 是否使用专用的 scheduler 处理 publish 事件（默认值：false）
            ping-before-activate-connection: true # 连接激活前是否发送 PING 消息（默认值：true）
            protocol-version: RESP2 # Redis 协议版本（默认值：RESP3，Redis 6.0 以下请配置为 RESP2）
            suspend-reconnect-on-protocol-failure: # 是否在协议失败时暂停重连（默认值：false）
            request-queue-size: # 请求队列大小 （默认值：Integer.MAX_VALUE）
            max-redirects: # 集群：重定向最大重试次数（默认值：5）
            validate-cluster-node-membership: # 集群：是否验证集群节点成员关系（默认值：true）
            node-filter:    # 集群：建立连接的节点[白名单] （如未配置，连接所有节点；如有配置，只连接配置节点）
            topology-refresh-options: # 集群：拓扑刷新配置选项
              adaptive-refresh-triggers: # 集群：动态刷新触发器，列表类型（默认为空集）
              adaptive-refresh-timeout: # 集群：动态刷新超时（默认值：30000 单位：毫秒）
              close-stale-connections: # 集群：是否关闭旧连接（默认值：true）
              dynamic-refresh-sources: # 集群：是否动态刷新节点源（默认值：true）
              periodic-refresh-enabled: # 集群：是否启用周期刷新（默认值：true）
              refresh-period: # 集群：刷新周期 （默认值：30000 单位：毫秒）
              refresh-triggers-reconnect-attempts: # 集群：刷新触发器重连尝试次数（默认值：3）
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
              fixedTimeout: # 超时时间设为固定值（默认值：-1，单位：毫秒，即使用连接超时）
```

这份完全配置中有超多的配置项，实际使用时可以根据需求进行删减。

如果默认配置值与期望值一致，那么直接删除该配置项即可，最后应该会留下非常简洁的配置。

### 4.4. 极简配置

```yaml
xcache:
  group: shop # 分组名称 (必填)
  template: # 公共模板配置 (必填)
    - id: t0 # 模板ID (必填)
```

**说明**：

这份配置设定了 group，并定义了一个公共模板配置，且仅设定了模板 id，其余全部使用默认配置。

1. 一级缓存：caffeine，过期时间、缓存空值 …… 等均使用默认值。
2. 二级缓存：无。
3. 三级缓存：无。
4. 缓存个性配置：无，所有缓存实例都完全使用 t0 模板配置。
5. 缓存指标统计：统计数据输出到日志。
6. 缓存数据同步、缓存数据刷新 …… 等：无。

## 5. 缓存注解

> 这里的缓存注解指的是 `Xcache` 定义的注解，非 `Spring Cache` 注解。

### 5.1. @Cacheable

`@Cacheable` 是方法注解，主要用于查询和保存单个缓存元素。

#### 5.1.1. 相关属性

| 属性        | 必填 | 作用                                         |
|:----------|:--:|--------------------------------------------|
| name      | 否  | 指定缓存名称（如类中无 `@CacheConfig` 注解则必须填写）        |
| keyType   | 否  | 指定键类型（如类中无 `@CacheConfig` 注解则必须填写）         |
| valueType | 否  | 指定值类型（如类中无 `@CacheConfig` 注解则必须填写）         |
| key       | 否  | SpEL表达式，用于提取键。<br/>如果未配置，使用被注解方法的第一个参数作为键。 |
| condition | 否  | SpEL表达式，用于判断是否执行缓存逻辑。 <br/>如果未配置，默认为 true。 |

#### 5.1.2. 执行逻辑

![image-20241021093622933](images/cacheable.png)

### 5.2. @CacheableAll

`@CacheableAll` 是方法注解，用于批量查询和保存缓存元素。

#### 5.2.1. 相关属性

| 属性        | 必填 | 作用                                          |
|:----------|:--:|---------------------------------------------|
| name      | 否  | 指定缓存名称（如类中无 `@CacheConfig` 注解则必须填写）         |
| keyType   | 否  | 指定键类型（如类中无 `@CacheConfig` 注解则必须填写）          |
| valueType | 否  | 指定值类型（如类中无 `@CacheConfig` 注解则必须填写）          |
| keys      | 否  | SpEL表达式，用于提取键集。<br/>如果未配置，使用被注解方法的第一个参数作为键。 |
| condition | 否  | SpEL表达式，用于判断是否执行缓存逻辑。 <br/>如果未配置，默认为 true。  |

#### 5.2.2. 执行逻辑

![cacheableAll](images/cacheableAll.png)

> 注意：
>
> 1. `@Cacheable` 加锁执行，`@CacheableAll` 不加锁执行。
>
> 2. ⭐⭐⭐ 被 `@CacheableAll` 注解修饰的方法，其返回的 `Map` 类型必须是可修改的，因为缓存结果集需要添加到该 `Map` 。
>
> 3. ⭐⭐⭐ 当方法创建的值集与缓存的键集不匹配时，方法返回值是不确定的。
     >
     >    ```java
>    /**
>     * 如果传入的 keys 是 {1, 2}，而方法创建的 Map 是 {{1, 1}, {2, 2}, {3, 3}}。
>     * 当缓存未全部命中时，会调用方法，返回 {{1, 1}, {2, 2}, {3, 3}}；
>     * 当缓存已全部命中时，不调用方法，返回 {{1, 1}, {2, 2}}。
>     */
>    @CacheableAll
>    public Map<Integer, Integer> getList(Set<Integer> keys) {
>        Map<Integer, Integer> map = new HashMap<>();
>    	Map.put(1, 1);
>      	Map.put(2, 2);
>        Map.put(3, 3);
>        return map;
>    }
>    ```

### 5.3. @CachePut

`@CachePut` 是方法注解，用于保存或更新单个缓存元素。

#### 5.3.1. 相关属性

| 属性        | 必填 | 作用                                                  |
|:----------|:--:|-----------------------------------------------------|
| name      | 否  | 指定缓存名称（如类中无 `@CacheConfig` 注解则必须填写）                 |
| keyType   | 否  | 指定键类型（如类中无 `@CacheConfig` 注解则必须填写）                  |
| valueType | 否  | 指定值类型（如类中无 `@CacheConfig` 注解则必须填写）                  |
| key       | 否  | SpEL表达式，用于提取键。<br/>如果未配置，使用被注解方法的第一个参数作为键。          |
| value     | 否  | SpEL表达式，用于提取值。<br/>如果未配置，使用被注解方法的执行结果作为值。           |
| condition | 否  | SpEL表达式，用于调用被注解方法前判断是否执行缓存逻辑。 <br/>如果未配置，默认为 true。  |
| unless    | 否  | SpEL表达式，用于调用被注解方法后判断是否执行缓存逻辑。 <br/>如果未配置，默认为 false。 |

#### 5.3.2. 执行逻辑

![cacheableAll](images/cachePut.png)

### 5.4. @CachePutAll

`@CachePutAll` 是方法注解，用于批量保存或更新缓存元素。

#### 5.4.1. 相关属性

| 属性        | 必填 | 作用                                                  |
|:----------|:--:|-----------------------------------------------------|
| name      | 否  | 指定缓存名称（如类中无 `@CacheConfig` 注解则必须填写）                 |
| keyType   | 否  | 指定键类型（如类中无 `@CacheConfig` 注解则必须填写）                  |
| valueType | 否  | 指定值类型（如类中无 `@CacheConfig` 注解则必须填写）                  |
| keyValues | 否  | SpEL表达式，用于提取键值对集。<br/>如果未配置，使用被注解方法的第一个参数作为键值对集。    |
| condition | 否  | SpEL表达式，用于调用被注解方法前判断是否执行缓存逻辑。 <br/>如果未配置，默认为 true。  |
| unless    | 否  | SpEL表达式，用于调用被注解方法后判断是否执行缓存逻辑。 <br/>如果未配置，默认为 false。 |

#### 5.4.2. 执行逻辑

![cacheableAll](images/cachePutAll.png)

### 5.5. @CacheRemove

`@CacheRemove` 是方法注解，用于驱逐单个缓存元素。

#### 5.5.1. 相关属性

| 属性               | 必填 | 作用                                                                      |
|:-----------------|:--:|-------------------------------------------------------------------------|
| name             | 否  | 指定缓存名称（如类中无 `@CacheConfig` 注解则必须填写）                                     |
| keyType          | 否  | 指定键类型（如类中无 `@CacheConfig` 注解则必须填写）                                      |
| valueType        | 否  | 指定值类型（如类中无 `@CacheConfig` 注解则必须填写）                                      |
| key              | 否  | SpEL表达式，用于提取键。<br/>如果未配置，使用被注解方法的第一个参数作为键。                              |
| condition        | 否  | SpEL表达式，用于调用被注解方法前判断是否执行缓存逻辑。 <br/>如果未配置，默认为 true。                      |
| unless           | 否  | SpEL表达式，用于调用被注解方法后判断是否执行缓存逻辑。 <br/>如果未配置，默认为 false。                     |
| beforeInvocation | 否  | 如果为 true，调用被注解方法前驱逐缓存元素；如果为 false，调用被注解方法后驱逐缓存元素。 <br/>如果未配置，默认为 false。 |

#### 5.5.2. 执行逻辑

![cacheRemove](images/cacheRemove.png)

### 5.6. @CacheRemoveAll

`@CacheRemoveAll` 是方法注解，用于批量驱逐缓存元素。

#### 5.6.1. 相关属性

| 属性               | 必填 | 作用                                                                      |
|:-----------------|:--:|-------------------------------------------------------------------------|
| name             | 否  | 指定缓存名称（如类中无 `@CacheConfig` 注解则必须填写）                                     |
| keyType          | 否  | 指定键类型（如类中无 `@CacheConfig` 注解则必须填写）                                      |
| valueType        | 否  | 指定值类型（如类中无 `@CacheConfig` 注解则必须填写）                                      |
| keys             | 否  | SpEL表达式，用于提取键集。<br/>如果未配置，使用被注解方法的第一个参数作为键集。                            |
| condition        | 否  | SpEL表达式，用于调用被注解方法前判断是否执行缓存逻辑。 <br/>如果未配置，默认为 true。                      |
| unless           | 否  | SpEL表达式，用于调用被注解方法后判断是否执行缓存逻辑。 <br/>如果未配置，默认为 false。                     |
| beforeInvocation | 否  | 如果为 true，调用被注解方法前驱逐缓存元素；如果为 false，调用被注解方法后驱逐缓存元素。 <br/>如果未配置，默认为 false。 |

#### 5.6.2. 执行逻辑

![cacheRemoveAll](images/cacheRemoveAll.png)

### 5.7. @CacheClear

`@CacheClear` 是方法注解，用于清空所有缓存数据。

#### 5.7.1. 相关属性

| 属性               | 必填 | 作用                                                                      |
|:-----------------|:--:|-------------------------------------------------------------------------|
| name             | 否  | 指定缓存名称（如类中无 `@CacheConfig` 注解则必须填写）                                     |
| keyType          | 否  | 指定键类型（如类中无 `@CacheConfig` 注解则必须填写）                                      |
| valueType        | 否  | 指定值类型（如类中无 `@CacheConfig` 注解则必须填写）                                      |
| condition        | 否  | SpEL表达式，用于调用被注解方法前判断是否执行缓存逻辑。 <br/>如果未配置，默认为 true。                      |
| unless           | 否  | SpEL表达式，用于调用被注解方法后判断是否执行缓存逻辑。 <br/>如果未配置，默认为 false。                     |
| beforeInvocation | 否  | 如果为 true，调用被注解方法前驱逐缓存元素；如果为 false，调用被注解方法后驱逐缓存元素。 <br/>如果未配置，默认为 false。 |

#### 5.7.2. 执行逻辑

![cacheClear](images/cacheClear.png)

### 5.8. @CacheConfig

`@CacheConfig` 是类注解，用于配置公共属性。

#### 5.8.1. 相关属性

| 属性        | 必填 | 作用     |
|:----------|:--:|--------|
| name      | 是  | 指定缓存名称 |
| keyType   | 是  | 指定键类型  |
| valueType | 是  | 指定值类型  |

> `@Cacheable`，`@CacheableAll` …… 等所有缓存方法注解均有这 3 个公共属性。
>
> 如果一个类中有同名缓存的多个缓存方法注解，那么可以在类中添加 `@CacheConfig` 注解，避免重复配置公共属性。

#### 5.8.2. 执行逻辑

![cacheClear](images/cacheConfig.png)

1. 公共属性完整性

   `@CacheConfig` 的 name 、keyType、valueType 是必填属性。

   缓存方法注解中，name 、keyType、valueType 并非必填属性，但如果类中没有 `@CacheConfig` 注解，则必须填写，否则报异常。

2. 公共属性一致性

   如果缓存方法注解中有配置 keyType、valueType 任意其中一个或多个属性，但类中又有同名缓存的 `@CacheConfig`
   注解，则这些公共属性值必须一致，否则报异常。

   因此，如果类中有同名缓存的 `@CacheConfig` 注解，缓存方法注解中的公共属性建议留空。

3. 如果缓存方法注解的 name 属性值未配置或与 `@CacheConfig` 的相同，则表示两者指向的是同一个缓存实例。

### 5.9. @EnableCache

`@EnableCache` 是类注解，用于启用 Xcache 缓存注解功能。

| 属性           | 必填 |            默认值            | 作用                                                         |
|:-------------|:--:|:-------------------------:|------------------------------------------------------------|
| basePackages | 是  |             无             | 指定需要扫描缓存注解的包路径                                             |
| order        | 否  | Ordered.LOWEST_PRECEDENCE | 指定切面优先级                                                    |
| AdviceMode   | 否  |     AdviceMode.PROXY      | 指定代理模式<br />当前仅支持 AdviceMode.PROXY，不支持 AdviceMode.ASPECTJ。 |

### 5.10. 其它事项

#### 5.10.1. condition 与 unless

condition 默认为 true，该表达式是在调用被注解方法之前进行解析，只有解析结果为 true，才会执行缓存相关逻辑。

unless 默认为 false，该表达式是在调用被注解方法之后进行解析，只有解析结果为 false，才会执行缓存相关逻辑。

由于 condition 先于 unless 进行条件判断，因此如果 condition 为 false，将直接忽略 unless，一定不会执行缓存逻辑。

另：`@Cacheable` 和 `@CacheableAll` 无 unless 属性。

#### 5.10.2. 注解互斥

`@Cacheable` 和 `@CacheableAll`，当缓存命中（全部）数据时，将不执行被注解方法，因此不能与其它缓存注解共用于同一方法。

#### 5.10.3. 方法参数名

如注解的表达式有用到方法参数名，项目编译时需添加参数 `-parameters`。

如使用 Maven 进行编译，可参考如下示例：

```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>${maven.compiler.version}</version>
    <configuration>
        <source>${maven.compiler.source}</source>
        <target>${maven.compiler.target}</target>
        <encoding>${maven.compiler.encoding}</encoding>
        <!--如果注解的表达式有用到方法参数名，需添加此编译选项并设为 true -->
        <parameters>true</parameters>
    </configuration>
</plugin>
```

如不想添加编译参数，则需使用 【#a + index】 或 【#p + index】 的方式来获取方法参数。

```java
// #a0 或 #p0 表示获取方法的第 1 个参数，#a1 或 #p1 表示获取方法的第 2 个参数，如此类推。
@CachePut(key = "#a0", value = "#a1")
//@CachePut(key = "#p0", value = "#p1")
public void save(long id, User user) {
    // do something
}
```

#### 5.10.4. result

如表达式计算是在被注解方法执行之后，被注解方法的执行结果将使用 “result” 关键字保存到表达式计算的上下文环境，表达式中可使用
“#result” 来提取被注解方法执行结果。

需要注意的是，如果被注解方法中存在命名为 “result” 的参数，那么：

如表达式计算是在被注解方法执行之前，“#result” 获取到的是被注解方法的参数变量；
如表达式计算是在被注解方法执行之后，“#result” 获取到的是被注解方法的返回结果。

```java
/**
 * 此示例，"#result" 获取到的是方法返回结果，而不是参数中的 result。
 * 因为 @CachePut 的表达式计算和缓存逻辑执行是在方法执行之后。
 */
@CachePut(value = "#result")
public User save(long id, User result) {
    return new User(1, "MethodResult", 18);
}
```

如果参数命名为 result，又希望表达式中使用该参数，那么可使用 【#a+index】 或 【#p+index】 来获取参数中的值。

```java

@CachePut(value = "#a1")
//@CachePut(value = "#p1")
public User save(long id, User result) {
    return new User(1, "MethodResult", 18);
}
```

## 6. 缓存接口

缓存核心接口位于 `com.igeeksky.xcache.common.cache`：

### 6.1. 主要接口

```java
    /**
 * 根据键从缓存中读取值（返回值为原始值）
 */
V get(K key);

CompletableFuture<V> getAsync(K key);    //异步

/**
 * 根据键从缓存中读取值（返回值为包装类）
 */
CacheValue<V> getCacheValue(K key);

CompletableFuture<CacheValue<V>> getCacheValueAsync(K key);    //异步

/**
 * 1. 先从缓存取值，如果缓存有命中，返回已缓存的值；
 * 2. 如果缓存未命中，则通过方法传入的 cacheLoader 回源取值，取值结果先存入缓存，最后返回该值。
 * <p>
 * 注：回源取值时将加锁执行。
 */
V getOrLoad(K key, CacheLoader<K, V> cacheLoader);

CompletableFuture<V> getOrLoadAsync(K key, CacheLoader<K, V> cacheLoader);    //异步

/**
 * 1. 先从缓存取值，如果缓存有命中，返回已缓存的值。
 * 2. 如果缓存未命中，通过缓存内部的 cacheLoader 回源取值，取值结果存入缓存并返回；
 * <p>
 * 注1：回源取值时将加锁执行。
 * 注2：如果缓存内部无 CacheLoader，将抛出异常。
 */
V getOrLoad(K key);

CompletableFuture<V> getOrLoadAsync(K key);    //异步

/**
 * 根据键集从缓存中读取值（返回值为原始值）
 */
Map<K, V> getAll(Set<? extends K> keys);

CompletableFuture<Map<K, V>> getAllAsync(Set<? extends K> keys);    //异步

/**
 * 根据键集从缓存中读取值（返回值为包装类）
 */
Map<K, CacheValue<V>> getAllCacheValues(Set<? extends K> keys);

CompletableFuture<Map<K, CacheValue<V>>> getAllCacheValuesAsync(Set<? extends K> keys);    //异步

/**
 * 1. 先从缓存取值，如果缓存命中全部数据，返回缓存数据集。
 * 2. 如果缓存有未命中数据，通过方法传入的 cacheLoader 回源取值，取值结果先存入缓存，最后返回合并结果集：缓存数据集+回源取值结果集。
 * 注：批量回源取值不加锁。
 */
Map<K, V> getAllOrLoad(Set<? extends K> keys, CacheLoader<K, V> cacheLoader);

CompletableFuture<Map<K, V>> getAllOrLoadAsync(Set<? extends K> keys, CacheLoader<K, V> cacheLoader);    //异步

/**
 * 1. 先从缓存取值，如果缓存命中全部数据，返回缓存数据集。
 * 2. 如果有缓存未命中数据，通过缓存内部的 cacheLoader 回源取值，取值结果先存入缓存，最后返回合并结果集：缓存数据集+回源取值结果集。
 * <p>
 * 注1：批量回源取值不加锁；
 * 注2：如果缓存内部无 CacheLoader，将抛出异常。
 */
Map<K, V> getAllOrLoad(Set<? extends K> keys);

CompletableFuture<Map<K, V>> getAllOrLoadAsync(Set<? extends K> keys);    //异步

/**
 * 将单个键值对存入缓存
 */
void put(K key, V value);

CompletableFuture<Void> putAsync(K key, V value);    //异步

/**
 * 将多个键值对存入缓存
 */
void putAll(Map<? extends K, ? extends V> keyValues);

CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> keyValues);    //异步

/**
 * 根据键将数据逐出缓存
 */
void remove(K key);

CompletableFuture<Void> removeAsync(K key);    //异步

/**
 * 根据键集将数据逐出缓存
 */
void removeAll(Set<? extends K> keys);

CompletableFuture<Void> removeAllAsync(Set<? extends K> keys);    //异步

/**
 * 清空缓存中的所有数据（只有同步方法）
 */
void clear();
```

### 6.2. 关于空值

#### 空值问题

缓存查询接口 `V get(K key)`，那么当返回值为 `null` 时，会有语义模糊：

1. 可能是数据源无该数据；
2. 可能是还未缓存该数据。

如果想确定数据源是否有该数据，调用者需回源查询。

每次返回 `null` ，为了确定数据源是否有值，都回源查询，这无疑会大大增加数据源压力。

#### 解决方案

Xcache 被设计为可缓存空值，`CacheValue` 是缓存值的包装类。

当使用缓存查询接口 `CacheValue<V> getCacheValue(K key)` 时，可通过 `cacheValue` 是否为 `null` 来判断是否还未缓存该数据。

```java
public void test() {
    CacheValue<User> cacheValue = cache.get(id);
    if (cacheValue == null) {
        // 未缓存，从数据源读取数据
        User user = userDao.find(id);
        // 取值结果存入缓存，如果缓存设置成允许缓存空值，那么下次查询时 cacheValue 将不为 null
        cache.put(id, user);
        doSomething();
    } else {
        if (cacheValue.hasValue()) {
            // 已缓存，数据源有数据
            User user = cacheValue.getValue();
            doSomething();
        } else {
            // 已缓存，数据源无数据（无需再回源确认）
            doSomething();
        }
    }
}
```

只有 `cacheValue == null` 时，才需回源取值，因此可以减少回源次数。

其它接口如 `Map<K, CacheValue<V>> getAll(Set<? extends K> keys)`，关于 `cacheValue` 的语义也是如此。

> 注：
>
> 通过 `cacheValue` 是否为 `null` 来决定是否回源取值，需将该缓存实例配置为允许缓存空值。
>
> 即，缓存实例至少有一级的缓存数据存储的  `enable-null-value` 配置项为 `true`（默认为  `true`）。

## 7. 功能扩展

Xcache 提供了一些功能扩展接口，用于支持不同的缓存模式和特定功能。

### 7.1. 回源取值

#### 7.1.1. CacheLoader

`CacheLoader` 主要用于实现 read-through 模式，其接口定义如下：

```java
/**
 * 回源取值，用于从数据源读取数据
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
@FunctionalInterface
public interface CacheLoader<K, V> {

    /**
     * 单个回源取值
     *
     * @param key 要回源取值的键
     * @return 如果有值，返回值；如果无值，返回 null
     */
    V load(K key);

    /**
     * 批量回源取值
     *
     * @param keys 要回源取值的键集
     * @return 返回键值对集合，不能返回 null
     */
    default Map<K, V> loadAll(Set<? extends K> keys) {
        Map<K, V> map = Maps.newHashMap(keys.size());
        for (K key : keys) {
            V value = load(key);
            if (value != null) {
                map.put(key, value);
            }
        }
        return map;
    }

}
```

#### 7.1.2. 用法示例

**实现类**：

```java
public record UserCacheLoader(UserDao userDao) implements CacheLoader<Long, User> {

    @Override
    public User load(Long id) {
        return this.userDao.findUser(id);
    }

    @Override
    public Map<Long, User> loadAll(Set<? extends Long> ids) {
        return this.userDao.findUserList(ids);
    }

}
```

**用法示例一**：

```java

@Service
public class UserCacheService {

    private final Cache<Long, User> cache;
    private final CacheLoader<Long, User> cacheLoader;

    public UserCacheService(UserDao userDao, CacheManager cacheManager) {
        this.cache = cacheManager.getOrCreateCache("user", Long.class, User.class);
        this.cacheLoader = new UserCacheLoader(userDao);
    }

    /**
     * 根据用户ID获取单个用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    public User getUser(Long id) {
        // 1. 首先查询缓存，如果缓存命中，则直接返回缓存数据；
        // 2. 如果缓存未命中，则由缓存直接调用 cacheLoader 从数据源加载数据。
        return cache.getOrLoad(id, cacheLoader);
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
        return cache.getAllOrLoad(ids, this.cacheLoader);
    }

}
```

**用法示例二**：

1、`UserCacheLoader` 作为 bean 对象注入到 spring 容器。

> CacheManager 创建 Cache 对象时，会根据缓存名称查找对应的 CacheLoader，如果与 Cache 同名的 CacheLoader 存在，则将其设为该
> Cache 对象的属性。

```java

@Configuration
@AutoConfigureBefore(CacheAutoConfiguration.class)
public class CacheLoaderAutoConfiguration {

    @Bean
    CacheLoaderHolder userCacheLoader(UserDao userDao) {
        // 创建一个 CacheLoaderHolder
        CacheLoaderHolder holder = new CacheLoaderHolder();
        // 将 UserCacheLoader 放入到 holder，键为缓存名称 “user”。
        holder.put("user", new UserCacheLoader(userDao));
        return holder;
    }

}
```

2、查询数据时，改为调用 `cache.getOrLoad(key)` 或 `cache.getAllOrLoad(keys)` 方法。

当缓存未命中时，Cache 对象将调用内部的 `CacheLoader` 从数据源读取数据然后再存入缓存。

> 注意：调用这两个方法时，如果 CacheLoader 不存在，将抛出异常。

```java

@Service
public class UserCacheService {

    private final Cache<Long, User> cache;

    public UserCacheService(CacheManager cacheManager) {
        // 创建名称为 "user" 的 Cache 对象
        this.cache = cacheManager.getOrCreateCache("user", Long.class, User.class);
    }

    /**
     * 获取单个用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    public User getUser(Long id) {
        // 1. 首先查询缓存，如果缓存命中，则直接返回缓存数据；
        // 2. 如果缓存未命中，则由缓存调用内部的 cacheLoader 从数据源加载数据，然后存入缓存。
        return cache.getOrLoad(id);
    }

    /**
     * 批量获取用户信息
     *
     * @param ids 用户ID集合
     * @return 用户信息集合
     */
    public Map<Long, User> getUsers(Set<Long> ids) {
        // 1. 首先查询缓存，如果缓存全部命中，则直接返回缓存数据；
        // 2. 如果存在未命中数据，则调用 cache 对象内部的 cacheLoader 从数据源加载。
        return cache.getAllorLoad(ids);
    }

}
```

#### 7.1.3. 小结

以上两个示例中，一是使用方法中传入的 `CacheLoader` 回源取值，二是使用注入的 `CacheLoader` 回源取值。

这两种方式的使用效果是完全一致的，且两种方式可以混用，用户可以根据业务场景和编程喜好自由选择。

另，如果希望使用缓存数据刷新功能，则必须通过自动配置注入 `CacheLoader`。

### 7.2. 数据回写

#### 7.2.1. CacheWriter

`CacheWriter` 主要用于实现 write-through 和 write-behind 模式。

> `CacheWriter` 已在 1.0.0 稳定版本中移除，数据回写需考虑各种业务场景和可能异常，并不适合集成到缓存框架。

### 7.3. 存在断言

#### 7.3.1. ContainsPredicate

cache 内部在调用 `CacheLoader` 之前，如果存在 `ContainsPredicate`，先调用  `ContainsPredicate`  判断数据源是否存在该数据，只有为
`true` 时才会调用 `CacheLoader`，其接口定义如下：

```java
/**
 * 判断数据源是否有值 <p>
 * 作用：处理缓存穿透问题 <p>
 * 譬如：预读取数据源中的所有 Key 生成布隆过滤器，每次回源查询数据时可以通过布隆过滤器来判断数据源是否有值
 *
 * @param <K> 键类型
 */
@FunctionalInterface
public interface ContainsPredicate<K> {

    /**
     * 判断数据源是否存在 key 对应的 value
     *
     * @param key 键
     * @return 数据源存在 key 对应的 value，返回 true，否则返回 false
     */
    boolean test(K key);

}
```

#### 7.3.2. 用法示例

`ContainsPredicate` 的实现类作为 bean 对象注入到 spring 容器。

> `CacheManager` 创建 `Cache` 对象时，会使用缓存名称查找 `ContainsPredicate`，如果与 Cache 同名的 `ContainsPredicate`
> 存在，则将其设为该 Cache 对象实例的内部属性。

```java

@Configuration
// 此对象的自动配置需要在创建 CacheManager 之前
@AutoConfigureBefore(CacheAutoConfiguration.class)
public class ContainsPredicateAutoConfiguration {

    /**
     * 创建 ContainsPredicate
     * 
     * @param userBloomFilter 用于判断用户 id 是否存在。
     * 注：完整的 BloomFilter 实现较复杂，要考虑数据更新的实时性和一致性问题等，
     * 而且跟业务场景强相关，因此这里仅作演示，不作具体实现。
     * 另，并非一定要用 BloomFilter，用 Set 等其它数据结构也是可以的。
     */
    @Bean
    ContainsPredicateHolder userContainsPredicate(UserBloomFilter userBloomFilter) {
        // 创建一个 ContainsPredicateHolder，其作用是建立缓存名称 与 ContainsPredicate 的一一对应关系
        ContainsPredicateHolder holder = new ContainsPredicateHolder();
        // 将 ContainsPredicate 放入到 holder，键为缓存名称 “user”。
        holder.put("user", new ContainsPredicate<Long>() {

            @Override
            public boolean test(Long id) {
                return UserBloomFilter.exists(key);
            }

        });
        return holder;
    }

}
```

用户仅需将 `ContainsPredicate` 实现类作为 bean 注入到 Spring 容器，具体使用由 Cache 对象内部自动处理。

## 8. 缓存模式

### 8.1. Cache-Aside

Cache-Aside 策略是最常用的缓存模式，其主要特点是缓存对象不与数据源进行直接交互，仅作为旁路逻辑。

#### 8.1.1. 读数据

2. 应用程序从缓存读取数据。
3. 如果缓存中有该数据，【结束】。
4. 如果缓存中无该数据，由**应用程序**从数据源读取数据，并将该数据写入到缓存，【结束】。

![image-20241122205329526](images/cache-aside1.png)

#### 8.1.2. 写数据

1. 应用程序将数据写入数据源。
2. 应用程序将数据写入缓存。

![image-20241122205733264](images/cache-aside2.png)

#### 8.1.3. 代码示例

```java
/**
 * 读数据
 */
public User getUser(Long id) {
    CacheValue<User> cacheValue = cache.getCacheValue(id);
    if (cacheValue != null) {
        // 如果缓存中有数据，直接返回缓存数据；
        return cacheValue.getValue();
    }
    // 如果缓存中无数据，从数据源查找数据，并将结果存入缓存
    User user = userDao.find(id);
    cache.put(id, user);
    return user;
}

/**
 * 写数据
 */
public void updateUser(Long id, User user) {
    // 更新数据源
    userDao.update(id, user);
    // 删除缓存数据（或更新缓存数据）
    cache.remove(id);
    // cache.put(id, user);
}
```

### 8.2. Read-Through

Read-Through 策略也是常用的缓存模式，其主要特点是由**缓存**与数据源直接交互，执行读数据的操作。

#### 8.2.1. 读数据

2. 应用程序从缓存读取数据。
3. 如果缓存中有该数据：返回缓存数据，【结束】。
4. 如果缓存中无该数据：由**缓存**从数据源读取数据，并将该数据写入到缓存，【结束】。

![image-20241122210852584](images/read-through.png)

#### 8.2.2. 代码示例

见 [7.1.2. 用法示例](#7.1.2. 用法示例)

### 8.3. Write-Through

Write-Through 的主要特点是缓存与数据源直接交互，由**缓存**将数据**同步**写入数据源。

#### 8.3.1. 写数据

2. 应用程序向缓存请求写入数据。
3. **缓存**先将数据**同步**写入数据源。
4. **缓存**再将数据写入自身存储。

![image-20241122212348666](images/wright-through.png)

#### 8.3.2. 代码示例

`Xcache` 不支持此模式。

### 8.4. Write-Behind

`Write-behind` 又称为 `Wright-back`，其主要特点是缓存与数据源直接交互，由**缓存**将数据**异步**写入数据源。

#### 8.4.1. 写数据

1. 应用程序向缓存请求写入数据。
2. **缓存**先将数据**异步**写入数据源。
3. **缓存**再将数据写入自身存储。

![image-20241122212749274](images/wright-behind.png)

#### 8.4.2. 代码示例

`Xcache` 不支持此模式。

### 8.5. Refresh-Ahead

Refresh-Ahead，即预刷新，一般会使用独立的线程（进程）在缓存数据过期之前从数据源加载数据并存入缓存。

Xcache 支持 Refresh-Ahead 策略，可以通过配置开启。

```yaml
xcache: #【2】
  group: shop #【2】分组名称 (必填)
  template: #【3】公共模板配置 (必填，仅需配置与默认配置不同的部分)，列表类型，可配置多个模板。
    - id: t0 #【4】模板ID (必填)，建议将其中一个模板的 id 配置为 t0。
      cache-refresh: #【5】缓存刷新配置
        provider: none #【6】CacheRefreshProviderId（默认值：none，不启用缓存刷新）
        refresh-after-write: 10000 #【7】数据写入缓存后，每隔此配置的时长刷新一次（默认值：3600000 毫秒）
        refresh-tasks-size: 16384 #【8】刷新线程一个周期发起运行的最大任务数（默认值：16384）
        refresh-thread-period: 10000 #【9】刷新线程运行间隔周期（默认值：10000 毫秒）
      first: #【10】一级缓存配置
        provider: caffeine #【11】使用 id 为 caffeine 的 StoreProvider 作为一级缓存
        expire-after-write: 3600000 #【12】数据写入后的存活时间（内嵌缓存默认值：3600000 毫秒）
        enable-random-ttl: true #【13】是否使用随机存活时间（默认值：true）
      second: #【14】二级缓存配置
        provider: lettuce #【15】使用 id 为 lettuce 的 StoreProvider 作为二级缓存（即【19】中设定的 id）
        expire-after-write: 7200000 #【16】数据写入后的存活时间（外部缓存默认值：7200000 毫秒）
        enable-random-ttl: true #【13】是否使用随机存活时间（默认值：true）
  redis: #【17】Redis 配置
    lettuce: #【24】Lettuce 客户端配置
      - id: lettuce #【26】创建 id 为 lettuce 的 RedisOperatorFactory
        standalone: #【27】单机模式 或 副本集模式
          node: 192.168.0.100:6379 #【28】Redis 节点
```

1、【6】`provider`：这里指定了缓存刷新的具体实现，配置的可选值有 `none`，`embed` 或自定义 id。

- 当配置为 `none` 时，即不开启缓存数据刷新。
- 当配置为  `embed` 时，实现类为  `EmbedCacheRefreshProvider`，其采用本地 `HashMap` 记录查询过的 key。当有多个进程实例时，相同的
  key 可能会同时存在于多个实例，而且每个进程实例都会回源读取数据。
- 当配置为自定义 id 时，譬如这里的 `lettuce` ，实现类为  `RedisCacheRefreshProvider`，其采用 Redis 集中存储查询过的
  key，因此不会重复回源，且同一时刻最多只有一个进程实例（多个线程）会回源读取数据。

2、【7】`refresh-after-write`：数据刷新的间隔周期。

- 我们期望的是在缓存值过期之前刷新数据，因此这个数值要小于最后一级缓存的 `expire-after-write` 配置值。

缓存数据刷新需用到 Cache 对象内部的 `CacheLoader` ，因此需通过自动配置方式注入与缓存同名的 `CacheLoader`  对象。

### 8.6. 小结

这是五种常见的缓存模式，大家可以根据业务场景选择合适的策略组合。

一般来说，读操作用 Read-Through，写操作用 Cache-Aside，如需要在数据过期前预刷新，则再加上 Refresh-Ahead 。

## 9. 模块简介

Xcache 拆分为多个子模块，以下是所有模块列表。

| 项目名称                                      | 类型  | 项目说明                                           |
|:------------------------------------------|:---:|:-----------------------------------------------|
| xcache-parent                             | pom | 所有子项目的最顶层父项目，主要用于统一的项目构建。                      |
| xcache-common                             | jar | 基础模块，主要用于定义基础接口、数据对象和配置项。                      |
| xcache-core                               | jar | 核心模块，主要用于实现具体的缓存逻辑。                            |
| xcache-annotation                         | jar | 缓存注解。                                          |
| xcache-caffeine                           | jar | 使用 caffeine 实现内嵌缓存。                            |
| xcache-caffeine-spring-boot-autoconfigure | jar | xcache-caffeine 模块的 Spring boot 自动配置。          |
| xcache-bom                                | pom | 统一的依赖包管理。                                      |
| xcache-extension                          | pom | 扩展模块的父项目。                                      |
| xcache-extension-codec                    | jar | 编解码接口。如希望开发自定义实现，可依赖此项目。                       |
| xcache-extension-common                   | jar | 可扩展模块接口。如希望开发自定义实现，可依赖此项目。                     |
| xcache-extension-jackson                  | jar | 使用 Jackson 实现的编解码。                             |
| xcache-jackson-spring-boot-autoconfigure  | jar | xcache-extension-jackson 模块的 Spring boot 自动配置。 |
| xcache-redis                              | pom | Redis 公共接口和相关实现。                               |
| xcache-spring                             | pom | spring 相关项目的父项目。                               |
| xcache-spring-adapter                     | jar | 适配 Spring cache。                               |
| xcache-spring-adapter-autoconfigure       | jar | 适配 Spring cache 的自动配置。                         |
| xcache-spring-aop                         | jar | 通过 spring-aop 实现对 Xcache 注解的支持。                |
| xcache-spring-boot-autoconfigure          | jar | Xcache 核心功能自动配置。                               |
| xcache-spring-boot-starter                | jar | Xcache 常用组件集成（`Caffeine`、`Jackson`、`Lettuce`）。 |
| xcache-redis-spring-boot-autoconfigure    | jar | Redis 扩展实现的自动配置。                               |
| xcache-test                               | pom | 所有测试项目的直接父项目。                                  |
| xcache-spring-boot-starter-test           | jar | 主要用于 Xcache 注解的测试。                             |
| xcache-spring-adapter-test                | jar | 主要用于 Spring cache 适配的测试。                       |
| xcache-test-base                          | jar | 主要用于缓存方法的测试，与及基础接口的公共测试用例。                     |
| xcache-test-domain                        | jar | 测试项目的数据对象定义。                                   |

## 10. 相关项目

[xtool](https://github.com/patricklaux/xtool)：一个简单的 Java 工具集。

[xredis](https://github.com/patricklaux/xredis)： `Lettuce` 的简单封装。