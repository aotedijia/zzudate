# ZZUDate 后端

郑州大学校园灵魂匹配平台后端服务，基于 Spring Boot 3 构建。用户通过 40 道深度问卷生成四维度灵魂画像，系统每周三定时执行全局贪心匹配算法，将契合度最高的同学配对，并附带社区论坛功能。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.13 | 核心框架 |
| JDK | 17 | 运行环境 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| MySQL | - | 关系型数据库 |
| Redis | - | 缓存/Token/验证码/经验值日限 |
| Spring Boot Mail | - | 邮件发送（163 SMTP） |
| Fastjson | 2.0.32 | JSON 处理 |
| Lombok | - | 代码简化 |
| Maven | - | 构建工具 |

## 项目结构

```
src/main/java/org/example/zzudate/
├── ZzudateApplication.java       # 启动类（@EnableScheduling）
├── Result.java                    # 统一响应封装
├── EmailService.java              # 邮件发送服务
├── GenerateEmailCode.java         # 6 位验证码生成器
├── config/
│   └── WebConfig.java             # 拦截器注册（排除 /auth/**）
├── controller/
│   ├── AuthController.java        # 认证：验证码/登录/注册/登出
│   ├── MatchController.java       # 匹配：资料保存/结果查询/坦白
│   ├── InfoController.java        # 社区帖子 CRUD
│   └── BaseInfoController.java    # 基础信息 CRUD（早期版本）
├── dto/
│   ├── UserBaseInfoDto.java       # 用户基础信息
│   ├── UserSoulInfoDto.java       # 灵魂问卷答案
│   └── UserAnswersDto.java        # 问卷答案
├── entity/
│   ├── User.java                  # 用户实体（含画像、经验值）
│   ├── MatchResult.java           # 匹配结果实体
│   └── Info.java                  # 社区帖子实体
├── mapper/
│   ├── UserMapper.java            # 用户 Mapper
│   ├── MatchResultMapper.java     # 匹配结果 Mapper
│   └── InfoMapper.java            # 帖子 Mapper
├── service/
│   ├── UserService.java           # 用户服务接口
│   ├── UserServiceImpl.java       # 用户服务实现
│   ├── InfoService.java           # 帖子服务接口
│   ├── InfoServiceImpl.java       # 帖子服务实现
│   ├── Match.java                 # 灵魂匹配核心算法
│   └── WeeklyMatchService.java    # 每周定时匹配服务
├── utils/
│   ├── CurrentUser.java           # ThreadLocal 当前用户工具
│   └── LoginInterceptor.java      # 登录拦截器
└── vo/
    ├── TokenVo.java               # 登录令牌视图对象
    └── MatchResultVo.java         # 匹配结果视图对象
```

## API 接口

### 认证模块 `/auth`（无需登录）

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/auth/getemailcode` | 发送邮箱验证码（60s 间隔，每日每 IP 15 次上限） |
| POST | `/auth/emailloginautoregister` | 邮箱验证码登录/自动注册 |
| POST | `/auth/logout` | 登出 |

> 仅允许 `gs.zzu.edu.cn`、`zzu.edu.cn`、`stu.zzu.edu.cn` 域名的邮箱注册。

### 匹配模块 `/match`（需登录）

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/match/getuserinfo` | 获取当前用户信息 |
| POST | `/match/savebaseinfo` | 保存基础信息（姓名/性别/身高/学院/校区/年级/倾向/联系方式） |
| POST | `/match/saveuserinfo` | 保存灵魂问卷答案（40 题 JSON） |
| POST | `/match/getmatchresult` | 获取本周匹配结果（含坦白三阶段） |
| POST | `/match/shownumber` | 坦白自己的联系方式 |

### 社区模块 `/Info`（需登录）

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/Info/saveInfo` | 发帖（+20 经验，每日上限 100） |
| POST | `/Info/updateInfo` | 更新帖子 |
| POST | `/Info/deleteInfo` | 删除帖子 |
| GET | `/Info/list` | 查询所有帖子 |
| GET | `/Info/listByCategory` | 按分类查询 |
| GET | `/Info/listByUserId` | 按用户查询 |

## 核心业务：灵魂匹配算法

**匹配时机**：每周三 19:00（`@Scheduled(cron = "0 0 19 ? * WED")`）

**匹配流程**：
1. 清空上周匹配结果（阅后即焚）
2. 筛选已填写问卷的用户
3. 预解析所有用户 40 题画像 JSON 为 Map 缓存
4. 双循环计算所有合法候选对分数（同校区 + 双向性别倾向验证）
5. 按分数从高到低排序，全局贪心配对
6. 低于 30% 阈值不配对
7. 生成匹配描述文案并批量写入数据库

**四维度权重分配**：

| 维度 | 题号 | 权重 | 占比 |
|------|------|------|------|
| 物质底色 | Q1-Q8 | 0.025/题 | ~20% |
| 精神依恋 | Q9-Q18 | 0.025/题 | ~25% |
| 生活节律 | Q19-Q28 | 0.015/题 | ~15% |
| 灵魂底线 | Q29-Q40 | 1/30/题 | ~40% |

**坦白三阶段机制**：
- **阶段 C**（信息封锁）：双方均未坦白 → 看不到任何号码
- **阶段 B**（单向展示）：我坦白但对方没有 → 只能看到自己的号码
- **阶段 A**（共鸣达成）：双方都坦白 → 互相看到对方号码

## 经验值系统

| 行为 | 经验值 | 每日上限 |
|------|--------|----------|
| 每日首次登录 | +20 | 无（每日触发一次） |
| 发帖 | +20 | 100/天 |

**等级体系**：1-5 级初来乍到 → 5-10 级常驻居民 → 10+ 级小有名气 → 校园达人 → 一代宗师

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis

### 配置

修改 `src/main/resources/application.properties`：

```properties
# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/zzudate?serverTimezone=GMT%2B8&characterEncoding=utf-8
spring.datasource.username=root
spring.datasource.password=your_password

# 邮件 SMTP
spring.mail.host=smtp.163.com
spring.mail.port=465
spring.mail.username=your_email@163.com
spring.mail.password=your_auth_code
```

### 建表

项目未包含自动建表脚本，需手动创建以下三张表：

```sql
CREATE DATABASE IF NOT EXISTS zzudate CHARACTER SET utf8mb4;

USE zzudate;

CREATE TABLE user (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50),
    email VARCHAR(100),
    create_time DATETIME,
    number VARCHAR(50),
    gender BOOLEAN,
    height VARCHAR(10),
    college VARCHAR(100),
    campus VARCHAR(20),
    grade INT,
    answers TEXT,
    choose VARCHAR(5),
    exp BIGINT DEFAULT 0
);

CREATE TABLE match_result (
    id VARCHAR(36) PRIMARY KEY,
    user_id_a VARCHAR(36),
    user_id_b VARCHAR(36),
    user_name_a VARCHAR(50),
    user_name_b VARCHAR(50),
    user_answer_a TEXT,
    user_answer_b TEXT,
    score DOUBLE,
    description TEXT,
    number_a VARCHAR(50),
    number_b VARCHAR(50)
);

CREATE TABLE info (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    user_name VARCHAR(50),
    title VARCHAR(200),
    content TEXT,
    create_time DATETIME,
    update_time DATETIME,
    category VARCHAR(50)
);
```

### 运行

```bash
cd zzudate
./mvnw spring-boot:run
```

服务默认启动在 `http://localhost:8080`。

### 认证方式

登录后返回 UUID Token，存入 Redis（48 小时过期），后续请求通过 `Authorization` 请求头携带 Token 进行认证。
