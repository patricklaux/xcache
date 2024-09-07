# Xcache

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html) [![Release](https://img.shields.io/github/v/release/patricklaux/xcache)](https://github.com/patricklaux/xcache/releases) [![Maven](https://img.shields.io/maven-central/v/com.igeeksky.xcache/xcache-parent.svg)](https://central.sonatype.com/namespace/com.igeeksky.xcache) [![Last commit](https://img.shields.io/github/last-commit/patricklaux/xcache)](https://github.com/patricklaux/xcache/commits)

## 简介

Xcache 是易于扩展、功能强大且配置灵活的 Java 多级缓存框架。

## 架构

![Architecture](docs/images/architecture.png)

## 特性

- 支持多种缓存模式：Cache-Aside，Read-Through，Write-Through，Write-Behind。
- 支持缓存数据同步：通过缓存事件广播，多个应用实例的缓存数据保持一致。
- 支持缓存指标统计：通过日志方式输出和 Redis Stream 方式输出，便于统计各种缓存指标。
- 支持缓存自动刷新：自动刷新缓存数据，避免慢查询导致应用响应缓慢。
- 支持数据回源加锁：加锁确保相同的键同时仅有一个线程回源查询，降低回源次数，减轻数据源压力。
- 支持缓存数据压缩：通过压缩数据，降低内存消耗。
- 支持多级缓存实现：内嵌缓存采用 Caffeine，外部缓存采用 Redis，并可通过实现 Store 接口扩展缓存能力，最多可支持三级缓存。
- 适配 SpringCache：无需修改现有代码，引入 Xcache 依赖，即可支持更多缓存功能配置。
- 更强大的缓存注解：Cacheable，CacheableAll，CachePut，CachePutAll，CacheEvict，CacheEvictAll，CacheClear
- 数据存在断言：通过实现数据存在断言接口，譬如 Bloom Filter，避免回源查询。
- 支持缓存空值：当数据源确定无数据时，可缓存空值，避免缓存穿透。
- 虚拟线程优化：需要加锁执行或 IO 等待的定时任务，采用虚拟线程执行，降低平台线程资源占用。

## 使用

### 运行环境

SpringBoot：[3.3.0, )
JDK：21

### 参考项目

使用 Xcache 注解：

https://github.com/patricklaux/xcache/tree/main/xcache-test/xcache-spring-boot-starter-test

使用 SpringCache 注解：

https://github.com/patricklaux/xcache/tree/main/xcache-test/xcache-spring-adapter-test

### 基本使用

#### Maven

如果不使用缓存注解，直接通过代码调用的方式操作缓存，可以采用此依赖配置。

```xml
<dependencies>
    <dependency>
        <groupId>com.igeeksky.xcache</groupId>
        <artifactId>xcache-spring-boot-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
    <!-- ... other ... -->
</dependencies>
```

**配置说明**

- 使用 Caffeine 作为内嵌缓存
- 使用 Lettuce 操作 Redis
- 使用 Jackson 序列化数据

#### 代码示例

```java
@Service
public class UserService {
    private final UserDao userDao;
    private final Cache<Long, User> cache;

    public UserService(CacheManager cacheManager, UserDao userDao) {
        this.cache = cacheManager.getOrCreateCache("user", Long.class, User.class);
        this.userDao = userDao;
    }

    public User getUser(Long id) {
        // 1. 从缓存查询数据：
        // 2. 如果缓存有数据，返回缓存数据；
        // 3. 如果缓存无数据，调用 userDao.findUser(id) 方法，并将查询结果存入缓存
        return cache.get(key, userDao::findUser);
    }

    public User saveUser(User user) {
        userDao.saveUser(user);
        return cache.put(user.getId, user);
    }

    public void saveUsers(List<User> users) {
        userDao.saveUsers(users);
        Map<Long, User> keyValues = Maps.newHashMap(users.size());
        users.forEach(user-> keyValues.put(user.getId, user));
        cache.putAll(keyValues);
    }

    public void deleteUser(Long id) {
        userDao.deleteById(id);
        cache.evict(id);
    }

    public void deleteUsers(Set<Long> ids) {
        userDao.deleteUsrs(ids);
        cache.evictAll(ids);
    }

}
```



### 使用 xcache 注解

#### Maven

如果希望使用 xcache 自定义注解，那么可以使用以下配置。

```xml
<dependencies>
    <dependency>
        <groupId>com.igeeksky.xcache</groupId>
        <artifactId>xcache-spring-boot-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>com.igeeksky.xcache</groupId>
        <artifactId>xcache-spring-aop</artifactId>
        <version>1.0.0</version>
    </dependency>
    <!-- ... other ... -->
</dependencies>
```



### 适配 Spring Cache 

#### Maven

如果希望使用 Spring Cache 及其注解，那么可以采用以下配置。

```xml
<dependencies>
    <dependency>
        <groupId>com.igeeksky.xcache</groupId>
        <artifactId>xcache-spring-boot-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>com.igeeksky.xcache</groupId>
        <artifactId>xcache-spring-adapter-autoconfigure</artifactId>
        <version>1.0.0</version>
    </dependency>
    <!-- ... other ... -->
</dependencies>
```

### 配置选项



## 缓存锁



## 缓存键



## 序列化