### 已完成任务

- [X] 【开发】 Lettuce 单机连接
- [X] 【开发】 Lettuce 连接池（无需配置连接池）
- [X] 【开发】 Redis 单机 clear 操作
- [X] 【开发】 Redis 集群 clear 操作
- [X] 【开发】 CacheStatMonitor
- [X] 【开发】 CacheSyncMonitor
- [X] 【开发】 Lettuce 主从连接
- [X] 【开发】 Lettuce 哨兵连接
- [X] 【开发】 Lettuce 集群连接
- [X] 【测试】 Lettuce 单机基本测试
- [X] 【测试】 Lettuce 主从基本测试
- [X] 【测试】 Lettuce 集群基本测试
- [X] 【测试】 Lettuce 哨兵基本测试
- [X] 【测试】 RedisStringStore 测试
- [X] 【测试】 RedisHashStore 测试
- [X] 【测试】 Lettuce 配置测试
- [X] 【开发】 Lettuce unixSocket
- [X] 【开发】 注册 MavenCentral
- [X] 【开发】 CacheBuilder 构造 Cache 实例
- [X] 【开发】 SpringCacheAdapter
- [X] 【测试】 SpringCacheAdapter
- [X] 【开发】 cacheable & cacheableAll 不能与其它注解共同用于同一个方法
- [X] 【思考】 RedisCacheStatProvider，channel 根据 group 进行区分，避免每新增一个缓存名称就需写一套采集程序
- [X] 【开发】 分布式缓存的前缀（group + ":" + cacheName + ":"）
- [X] 【开发】 NONE 默认为大写
- [X] 【开发】 删除 ContainsPredicateProvider
- [X] 【开发】 缓存接口方法名调整

### 待完成任务

- [ ] 【文档】 Guide，Reference
- [ ] 【开发】 补充代码注释
- [ ] 【开发】 xcache-jedis
- [ ] 【开发】 ConcurrentHashMap 实现无过期时间的缓存
- [ ] 【开发】 RedisHashStore 分为两类实现：一是集群模式，二是非集群模式。
- [ ] 【开发】 RedisHashStore，initKeys 方法，用于初始化缓存的 key 列表，需可配置数量。
- [ ] 【开发】 缓存数据刷新策略（根据时间周期触发）
- [ ] 【测试】 测试 Xcache 注解参数
