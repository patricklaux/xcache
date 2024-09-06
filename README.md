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
- 支持数据存在断言：通过实现数据存在断言接口，避免回源查询不存在的值，减轻数据源压力。
- 支持多级缓存实现：内嵌缓存采用 Caffeine，外部缓存采用 Redis，并可通过实现 Store 接口扩展缓存能力，最多可支持三级缓存。
- 适配 SpringCache：适配 Spring Cache，无需修改代码，引入 Xcache 依赖，即可支持更多缓存功能配置。
- 更强大的缓存注解：Cacheable，CacheableAll，CachePut，CachePutAll，CacheEvict，CacheEvictAll，CacheClear
- 支持虚拟线程特性：存在加锁执行或 IO 等待的定时任务，采用虚拟线程进行优化，降低平台线程资源占用。

## 使用

### 基本使用

#### 引入依赖

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

### 使用 xcache 注解

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



### 使用 Spring Cache 注解

```xml
<dependencies>
    <dependency>
        <groupId>com.igeeksky.xcache</groupId>
        <artifactId>xcache-spring-boot-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>com.igeeksky.xcache</groupId>
        <artifactId>xcache-spring-adapter</artifactId>
        <version>1.0.0</version>
    </dependency>
    <!-- ... other ... -->
</dependencies>
```



## 缓存锁



## 缓存键



## 序列化