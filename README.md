<p align="center">
  <img src="./pic/NBlog.png" alt="NBlog logo" style="width: 200px; height: 200px">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/JDK-1.8+-orange">
  <img src="https://img.shields.io/badge/SpringBoot-2.2.7-brightgreen">
  <img src="https://img.shields.io/badge/MyBatis-3.5.5-red">
  <img src="https://img.shields.io/badge/Redis-6.0+-red">
  <img src="https://img.shields.io/badge/Vue-2.6.11-brightgreen">
  <img src="https://img.shields.io/badge/license-MIT-blue">
</p>

## 简介

Spring Boot + Vue 前后端分离博客系统，基于 [Naccl/NBlog](https://github.com/Naccl/NBlog) 二次开发，加入了 AI 摘要对话、缓存防护、点赞排行等功能。

## 新增功能

### AI 摘要与对话

- 集成**阿里云通义千问（DashScope）**，兼容 OpenAI 接口格式
- **文章摘要**：一键生成 3-5 句中文摘要，Redis 缓存 30 天避免重复调用
- **文章对话**：基于文章内容与读者问答，支持上下文理解
- 长文本自动截断，防止超 token 限制

### 缓存防护

- **缓存击穿**：SETNX 互斥锁 + 双重检查，热点缓存过期时只允许一个线程重建
- **缓存穿透**：空值缓存，不存在的查询结果缓存 60 秒，防止恶意请求穿透到数据库
- **缓存雪崩**：TTL 加随机偏移，避免大量 key 同时过期

### 点赞排行

- **点赞去重**：Redis Set 实现，同一访客不可重复点赞，O(1) 判断
- **热门排行**：Redis ZSet 热度计分，自动排序取 Top10

### 验证码登录

- 后端生成图形验证码，Redis 存储并设置过期时间

### 个性化改造

- 博客名称、背景图片、头像等样式自定义
- 前台 UI 调整

## 技术栈

### 后端

| 技术 | 用途 |
|------|------|
| Spring Boot 2.2.7 | 核心框架 |
| Spring Security + JWT | 认证授权 |
| MyBatis + PageHelper | ORM + 分页 |
| Redis | 缓存 / 点赞 / 排行 / 限流 / 验证码 |
| Quartz | 定时任务 (浏览量同步) |
| RestTemplate | AI API 调用 |
| ip2region | IP 地址解析 |
| commonmark-java | Markdown 转 HTML |

### 前端

- Vue 2.x + Vue Router + Vuex
- Element UI + Semantic UI
- Axios、ECharts、MavonEditor

## 快速开始

1. 创建 MySQL 数据库 `nblog`，执行 `/blog-api/nblog.sql`
2. 修改 `/blog-api/src/main/resources/application-dev.properties` 中的数据库、Redis、邮箱、AI API Key 等配置
3. 安装 Redis 并启动
4. 启动后端：`blog-api` 目录下运行 Spring Boot
5. 启动前台：`cd blog-view && npm install && npm run serve`
6. 启动后台：`cd blog-cms && npm install && npm run serve`

**AI 功能配置**：在 `application-dev.properties` 中设置：
```properties
ai.dashscope.api-key=你的阿里云DashScope API Key
ai.dashscope.model=qwen-plus
ai.dashscope.temperature=0.7
ai.dashscope.max-tokens=2048
```

## 项目结构

```
NBlog
├── blog-api      # Spring Boot 后端
│   ├── src/main/java/top/naccl
│   │   ├── controller/     # 控制器 (含 AiController)
│   │   ├── service/impl/   # 业务层 (含 DashScopeServiceImpl 等)
│   │   ├── config/         # 配置类
│   │   ├── interceptor/    # 拦截器 (限流)
│   │   ├── aspect/         # AOP (日志)
│   │   └── task/           # 定时任务
│   └── src/main/resources/
├── blog-view      # Vue 前台
├── blog-cms       # Vue 后台管理
└── pic            # 项目截图
```

## 缓存架构说明

| Redis Key | 类型 | 用途 |
|-----------|------|------|
| blogViewsMap | Hash | 博客实时浏览量 |
| homeBlogInfoList | Hash | 首页分页缓存 |
| hotBlogList | ZSet | 热门文章排行 |
| blogLike:{id} | Set | 点赞去重 |
| identificationSet | Set | 每日访客 UV 去重 |
| ai:summary:{id} | String | AI 摘要缓存 |

## 致谢

本项目基于 [Naccl/NBlog](https://github.com/Naccl/NBlog)，感谢原作者的开源贡献。

## License

[MIT](./LICENSE)
