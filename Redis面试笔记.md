# Redis 面试笔记

> 基于 NBlog 项目实战 | Spring Boot + Redis

---

## 一、Redis 是什么

- 基于**内存**的键值数据库
- 读写速度微秒级，单机 10w+ QPS
- 支持多种数据结构
- 常用于缓存、计数器、排行榜、分布式锁

### 与 MySQL 对比

- Redis：数据在内存，微秒级读写，支持 String/Hash/Set/ZSet/List，默认不持久化，适合缓存和计数
- MySQL：数据在磁盘，毫秒级读写，支持表/行/列，默认持久化，适合业务数据永久存储

---

## 二、五种数据结构

### 2.1 String（字符串）

- 一个 key 对应一个 value，value 可以是字符串、数字、JSON
- 常用命令：`SET` `GET` `INCR` `INCRBY` `DEL` `EXPIRE`
- Java 对应：`opsForValue().set/get/increment`

**项目中的使用：**

- `categoryNameList` → 分类名列表的 JSON
- `tagCloudList` → 标签云列表的 JSON
- `siteInfoMap` → 站点信息的 JSON
- `newBlogList` → 最新推荐博客列表的 JSON
- `aboutInfoMap` → 关于我页面内容
- `friendInfoMap` → 友链页面内容
- `archiveBlogMap` → 归档数据
- 限流计数器 `IP:METHOD:URI` → 访问次数 + TTL 自动过期

---

### 2.2 Hash（哈希表）

- 一个 key 下存多个 field-value 对
- 类比 Java 的 `Map<String, Map<String, Object>>`
- 常用命令：`HSET` `HGET` `HGETALL` `HINCRBY` `HDEL` `HEXISTS`
- Java 对应：`opsForHash().put/get/entries/increment`

**项目中的使用：**

- `blogViewsMap` → `{博客ID: 浏览量}`，存所有博客的实时浏览量
- `homeBlogInfoList` → `{页码: 该页数据}`，首页分页缓存
- `qqAvatarUrlMap` → `{QQ号: 头像URL}`，QQ 头像缓存

**为什么浏览量用 Hash 而不是 String？**

- 如果用 String，1000 篇博客就要 1000 个 key，难管理、内存大
- Hash 把同类数据聚合在一个 key 下，便于管理，底层 ziplist 压缩省内存

---

### 2.3 Set（集合）

- 一个 key 下存不重复的元素，无序
- 支持快速判断元素是否存在（O(1)）
- 常用命令：`SADD` `SISMEMBER` `SCARD` `SREM`
- Java 对应：`opsForSet().add/isMember/size/remove`

**项目中的使用：**

- `identificationSet` → 每日访客 UUID 集合，用于 UV 去重
- 每天 0 点定时任务 `DEL` 清空这个 Set，重新计数

**为什么去重用 Set 而不是 List？**

- Set 保证元素唯一
- `SISMEMBER` 判断是否存在是 O(1)，List 是 O(n)
- `SCARD` 直接获取今日 UV 数

---

### 2.4 ZSet（有序集合）

- Set 的升级版，每个元素关联一个分数，按分数自动排序
- 常用命令：`ZADD` `ZINCRBY` `ZREVRANGE` `ZSCORE` `ZRANK`
- Java 对应：`opsForZSet().add/incrementScore/reverseRange/score/rank`

**项目中的使用：**

- `hotBlogList` → `{博客ID: 热度分}`，每次访问 `ZINCRBY +1`
- 取 Top10：`ZREVRANGE hotBlogList 0 9`（降序，最高分在前）

**ZSet 底层原理（面试重点）：**

- 数据少时用 ziplist 压缩列表
- 数据多时用 skiplist（跳表）+ dict（哈希表）
- 跳表负责按分数排序，哈希表负责按元素查分数
- 增删改查都是 O(log N)

---

### 2.5 List（列表）

- 本项目暂未使用，但面试常考
- 常用命令：`LPUSH` `RPUSH` `LPOP` `RPOP` `LRANGE` `BLPOP`（阻塞弹出）
- 典型用途：消息队列、最新动态时间线

---

## 三、Cache-Aside 缓存模式（核心）

### 读流程

```
请求 → 查 Redis → 命中 → 直接返回
                → 未命中 → 查 MySQL → 写入 Redis → 返回
```

### 写流程

```
更新 MySQL → 删除 Redis 缓存 → 下次有人读时自动重建
```

### 为什么删除缓存而不是更新缓存？

- 更新缓存需要知道新值的完整数据，计算复杂
- 如果缓存涉及多张表关联，更新逻辑非常复杂
- 删除后惰性加载，只有真正需要的数据才进入缓存
- 删除策略简单可靠，不容易出错

**项目中的代码（BlogServiceImpl）：**

新增/修改/删除博客后，删除三个相关的缓存 key：首页列表、最新推荐、归档页面

---

## 四、实战场景

### 4.1 博客浏览量计数器（Hash）

**方案：Redis 实时计数 + 定时同步 MySQL**

```
每次访问 → Redis HINCRBY（实时、原子、快）
每天凌晨 1 点 → Quartz 定时任务 → 遍历 Hash → 批量 UPDATE MySQL
```

**为什么不每次直接更新 MySQL？**

- MySQL 磁盘写入比 Redis 慢 100-1000 倍
- 高并发下频繁 UPDATE 会导致行锁竞争
- Redis 的 HINCRBY 是原子操作，无并发问题

**启动恢复：** `@PostConstruct` 检查 Redis 是否为空，为空就从 MySQL 加载

**数据一致性：** 最终一致性方案。如果面试问怎么改进，可以回答：
- 加消息队列异步消费，准实时同步
- Redis 开启 AOF 持久化，重启不丢

---

### 4.2 热门文章排行榜（ZSet）

```
每次访问文章 → ZINCRBY hotBlogList 1 博客ID
取 Top10 → ZREVRANGE hotBlogList 0 9
```

**为什么 ZSet 适合排行榜？**

- 自动按分数排序
- ZINCRBY 原子递增，并发安全
- 取 Top N 复杂度 O(log N + M)
- 查排名用 ZREVRANK，O(log N)

---

### 4.3 接口限流（固定窗口算法）

**原理：** Redis Key = `IP:请求方法:请求URI`，Value = 访问次数，TTL = 时间窗口

```
请求 → GET key
     → 值为 null → 第一次访问 → INCR + EXPIRE → 放行
     → 值 < 限制  → INCR +1 → 放行
     → 值 >= 限制 → 返回 403
```

**用到的 Redis 特性：**

- INCR 原子操作：多线程同时请求不会计数错误
- EXPIRE TTL：时间窗口自动重置，无需手动清理

**固定窗口的问题：** 窗口边界可能被突发流量打穿（临界问题）
- 改进方案：滑动窗口（用 ZSet + 时间戳）、令牌桶、漏桶算法

---

### 4.4 每日 UV 去重（Set）

```
每次访问 → SISMEMBER identificationSet uuid
         → 存在 → 跳过
         → 不存在 → SADD 加入 → 记录访问日志

每天 0 点 → DEL identificationSet（新的一天重新计数）
         → 统计昨日日志 → 写入 MySQL
```

---

## 五、Redis 序列化

### 本项目配置

使用 `Jackson2JsonRedisSerializer`，存到 Redis 的数据是**可读的 JSON 格式**

### 常见方式对比

- **JDK 序列化**：二进制乱码不可读，仅 Java 能用，不推荐
- **Jackson2Json**：可读 JSON，跨语言，本项目使用，推荐
- **String 序列化**：最简单但不能直接存对象
- **Protobuf**：性能最好但复杂，极致性能场景用

---

## 六、高频面试题

### Q1：Redis 有哪些数据结构？你用过哪些？

五种：String、Hash、List、Set、ZSet。

我的博客项目全部用到了：
- String：分类列表、标签云、站点信息等 JSON 缓存
- Hash：博客浏览量（blogId → views）
- Set：每日访客 UUID 去重
- ZSet：热门文章排行榜

### Q2：Redis 和 MySQL 数据不一致怎么办？

分场景：

- 我的博客浏览量用最终一致性：Redis 实时更新，每天凌晨定时同步 MySQL
- 更实时的可以用消息队列异步消费
- 强一致性场景不建议用 Redis，直接用 MySQL

### Q3：缓存穿透、击穿、雪崩？

- **穿透**：查不存在的数据，每次都打到 MySQL。解决：缓存空值、布隆过滤器
- **击穿**：热点 key 过期，大量请求同时打到 MySQL。解决：互斥锁 SETNX、永不过期
- **雪崩**：大量 key 同时过期。解决：TTL 加随机值、多级缓存、熔断降级

### Q4：Redis 为什么快？

- 纯内存操作，不涉及磁盘 IO
- 单线程模型，避免上下文切换和锁竞争
- IO 多路复用，epoll 一个线程处理大量连接
- 专门优化过的数据结构（SDS、ziplist、skiplist）

### Q5：Redis 持久化方式？

- **RDB**：定时快照，恢复快、文件小，可能丢最后几分钟数据
- **AOF**：记录每条写命令，数据更安全，文件大、恢复慢
- **混合**：Redis 4.0+ 支持两者结合

### Q6：单线程的 Redis 为什么还要原子操作？

- 单线程保证单个命令原子，但"读-改-写"多命令组合之间可能插入其他客户端的命令
- 复杂操作用原子命令（INCR/HINCRBY）或 Lua 脚本

### Q7：ZSet 底层数据结构？

- 数据少：ziplist 压缩列表
- 数据多：skiplist（跳表）+ dict（哈希表）
- 跳表负责范围查询和排序，哈希表负责点查询
- 不用红黑树因为跳表实现简单、范围查询更快

### Q8：Redis 过期策略？

- 惰性删除：访问 key 时检查，过期则删
- 定期删除：每 100ms 随机抽一批检查，过期则删
- 内存淘汰：内存满时按策略淘汰（LRU/LFU/TTL/随机）

### Q9：如何用 Redis 实现分布式锁？

```
SET lock_key unique_value NX EX 30
```

- NX：key 不存在才设置（互斥）
- EX 30：30 秒过期（防死锁）
- unique_value：释放时校验，防止误删别人的锁
- 释放用 Lua 脚本保证原子性

---

## 七、项目 Redis 全景速查

```
Redis Key                   类型      用途
──────────────────────────────────────────────────
blogViewsMap               Hash      博客浏览量
homeBlogInfoList           Hash      首页分页缓存
qqAvatarUrlMap             Hash      QQ头像URL缓存
categoryNameList           String    分类名列表
tagCloudList               String    标签云列表
siteInfoMap                String    站点信息
aboutInfoMap               String    关于我页面
friendInfoMap              String    友链页面
newBlogList                String    最新推荐博客
archiveBlogMap             String    归档数据
identificationSet          Set       每日访客UUID
hotBlogList                ZSet      热门文章排行榜
IP:METHOD:URI              String    接口限流计数器
```

---

## 八、学习路线

1. 把项目跑起来，开 `redis-cli` 执行 `MONITOR`，正常使用网站，看每个操作触发了什么 Redis 命令
2. 背熟每种数据结构的命令和 Java 代码对应关系
3. 理解为什么这么设计（Hash vs String、删缓存 vs 更新缓存）
4. 动手改造：给缓存加随机 TTL（防雪崩）、用 SETNX 防击穿
