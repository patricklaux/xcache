# Xcache Design

**Author**: Patrick.Lau	**Version**: 1.0.0



## 缓存操作流程

**原则**：数据查询顺序从一级缓存到三级缓存，数据写入、删除顺序从三级缓存到一级缓存。

### 数据查询

![cache-data-query](images/Design/cache-data-query.png)



### 数据写入

![cache-data-write](images/Design/cache-data-write.png)



### 数据删除

![cache-data-delete](images/Design/cache-data-delete.png)



### 接口与实现

![cache-interface](images/cache-interface.png)

1. 当配置为使用全部三级缓存时，`CacheManager` 会创建 `ThreeLevelCache`；
2. 当配置为使用其中两级缓存时，`CacheManager` 会创建 `TwoLevelCache`；
3. 当配置为仅仅使用一级缓存时，`CacheManager` 会创建 `OneLevelCache`；
4. 当配置为不使用缓存时，`CacheManager` 会创建 `NoOpcache`（原来使用缓存，现在不再使用，仅需修改配置，无需修改代码）。



## 缓存数据存储

### 接口与实现

![store-interface](images/store-interface.png)



`CaffeineStore` 通过 `CaffeineStoreProvider` 进行创建





`RedisStore`：





## 数据存在断言





## 数据回源加载





## 缓存数据刷新

接口：CacheRefreshProvider

实现：





## 缓存数据同步



## 缓存指标统计



## 编解码与压缩



## 缓存锁





## 缓存组件管理

### 组件注册



### 组件容器



### 单例模式与延迟加载



### 优雅停机

