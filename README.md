# AI-CRM-FULL （完整版）

> 这是与你手上 `ai-crm` 登录骨架配套的 **完整实现版**。
> 包名仍是 `com.aicrm`，方便两个项目逐文件对照；数据库独立（`ai_crm_full`），互不影响。
> 用途：你看完骨架自己写一遍，再拿本项目的完整代码对照"我漏了哪、哪里该这么做"，用于面试和简历。

## 1. 这是什么

一个**后端纯 API** 的 AI 智能销售 CRM（无前端，接口文档用 Knife4j）。
围绕"线索 → 客户 → 联系人 → 商机 → 跟进"的标准销售流程，实现了：

- 登录鉴权（Sa-Token，JWT 风格 token）
- **数据权限**（销售只能看自己名下；主管/管理员看全部、可转移归属）
- 线索**转客户**（事务）
- 商机**阶段状态机**（1初步接触→…→5赢单/6输单，赢单率、系统跟进记录）
- 销售**漏斗**统计（按阶段聚合数量/金额，手写 SQL）
- **AI 销售助手**（可插拔：本地 Ollama / 真实厂商 API / 离线 Mock）
- 启动自动建表 + 自动造演示数据（幂等）

## 2. 技术栈

| 类别 | 技术 |
|------|------|
| 语言/框架 | Java 17 · Spring Boot 3.2.5 |
| ORM | MyBatis-Plus 3.5.7（含逻辑删除、分页） |
| 数据库 | MySQL 8（启动时 `createDatabaseIfNotExist` 自动建库） |
| 鉴权 | Sa-Token 1.39.0（默认内存会话，可切 Redis） |
| 文档 | Knife4j 4.5.0（springdoc OpenAPI3） |
| 密码 | spring-security-crypto 的 BCrypt |
| AI | JDK HttpClient + OpenAI 兼容协议（无第三方 SDK） |

## 3. 目录结构

```
ai-crm-full
├─ pom.xml
└─ src/main
   ├─ java/com/aicrm
   │  ├─ AiCrmFullApplication.java    启动类
   │  ├─ common/    Result  PageResult  BusinessException  GlobalExceptionHandler  CurrentUser  StageConstants
   │  ├─ config/    SaTokenConfig  MybatisPlusConfig  PasswordConfig
   │  ├─ entity/    7 个实体（CrmLead/Customer/Contact/Opportunity/FollowUp/AiChatLog + SysUser）
   │  ├─ mapper/    6+1 个 Mapper（商机 Mapper 里有漏斗手写 SQL）
   │  ├─ dto/ vo/   入参/出参对象
   │  ├─ service/   Auth Customer Contact Lead Opportunity FollowUp Dashboard AiChat
   │  ├─ controller/ 对 REST 接口
   │  ├─ ai/        AiClient 接口 + MockAiClient + OpenAiCompatClient
   │  └─ init/      DataInitializer（建演示用户 + 演示业务数据，幂等）
   └─ resources
      ├─ application.yml
      └─ db/schema.sql   7 张表（幂等建表）
```

## 4. 数据库（7 张表）

| 表 | 作用 | 有无 deleted(逻辑删除) |
|----|------|------|
| `sys_user` | 用户：ADMIN/MANAGER/SALES | **无** |
| `crm_lead` | 销售线索 | 有 |
| `crm_customer` | 客户（`owner_id` 数据权限字段） | 有 |
| `crm_contact` | 联系人（挂在客户下） | 有 |
| `crm_opportunity` | 商机（`stage` 1~6，`amount` 金额） | 有 |
| `crm_follow_up` | 跟进记录（`type=系统` 是程序自动写的） | 有 |
| `crm_ai_chat_log` | AI 问答日志 | **无** |

> ⚠️ 全局逻辑删除 `logic-delete-field: deleted` 已开启：**有 deleted 列的表，实体要带 `Integer deleted` 字段**；`sys_user`、`crm_ai_chat_log` 没有 deleted，实体**绝不能加** deleted，否则 SQL 会拼 `deleted=0` 报错。

## 5. 启动

只需要 **MySQL 已启动**（本机 root/123456，见 `application.yml`，可按需改）；Redis 不依赖。

```bash
# 1) 编译
mvn clean package -DskipTests

# 2) 运行（默认 8081，避免和你 ai-crm 的 8080 冲突）
java -jar target/ai-crm-full-1.0.0.jar

# 或开发模式
mvn spring-boot:run
```

- 第一次启动会自动建库 `ai_crm_full`、建 7 张表、写入演示数据。
- 接口文档：http://localhost:8081/doc.html

演示账号（密码都 123456）：

| 账号 | 角色 | 数据权限视角 |
|------|------|------|
| admin | ADMIN | 看全部，可转移归属 |
| manager | MANAGER | 看全部，可转移归属 |
| seller | SALES | 只能看自己名下 |

## 6. 接口一览（前缀 `/api`，除 login 外都要带请求头 `Authorization: <token>`）

| 模块 | 方法与路径 | 说明 |
|------|-----------|------|
| auth | POST `/auth/login` | 登录（公开） |
| auth | POST `/auth/logout` · GET `/auth/me` | 登出 / 当前用户 |
| customers | GET `/customers`（别名 `/customers/page`） | 客户分页（name/industry/level/ownerId 筛） |
| customers | GET `/customers/{id}` | 客户详情（含联系人） |
| customers | POST `/customers` | 新增 |
| customers | PUT `/customers/{id}` · DELETE `/customers/{id}` | 编辑 / 逻辑删除 |
| customers | PUT `/customers/{id}/transfer` | 转移归属（仅 ADMIN/MANAGER） |
| contacts | GET/POST `/customers/{customerId}/contacts` · PUT/DELETE `/customers/{customerId}/contacts/{id}` | 联系人的列表/新增/编辑/删除 |
| leads | GET `/leads`（别名 `/leads/page`）· POST `/leads` · PUT/DELETE `/leads/{id}` | 线索 CRUD |
| leads | POST `/leads/{id}/convert` | **线索转客户**（事务） |
| opportunities | GET `/opportunities`（别名 `/opportunities/page`）· POST `/opportunities` | 商机列表（customerId/stage 筛）/新增 |
| opportunities | PUT `/opportunities/{id}` · DELETE `/opportunities/{id}` | 商机编辑 / 逻辑删除（阶段变更请走 `/stage`） |
| opportunities | PUT `/opportunities/{id}/stage` | **阶段推进（状态机）** |
| opportunities | GET `/opportunities/{id}/follow-ups` | 商机时间线 |
| follow-ups | GET/POST `/follow-ups` | 跟进列表/新增 |
| dashboard | GET `/dashboard/funnel` | **销售漏斗** |
| ai | POST `/ai/chat` | 向 AI 提问（可带 customerId/opportunityId 上下文） |
| ai | GET `/ai/chats` | 我的提问历史 |

### 6.1 与《开发计划》的对齐说明（有意为之）

- **列表路径**：三个列表接口同时开放 `/api/customers|leads|opportunities` 与其 `/page` 别名（分页参数一致 `pageNum/pageSize`），《开发计划》§6/附A 里的 `/page` curl 可直接跑通。
- **"CRUD"名副其实**：商机、联系人已补全编辑/删除。注意**商机阶段变更只能走 `PUT /{id}/stage`**，普通编辑 body 里若改了 stage 会被拒（防止绕过状态机）；商机归属客户不可通过编辑修改。
- **数据权限比《开发计划》接口表标注的"登录"更严**：线索、跟进的增删查同样按 owner/所属客户继承校验（SALES 只能动自己名下），与整体数据权限主线一致。
- **新增商机归属默认继承客户 owner**（管理员显式传 `ownerId` 才覆盖），保证同一客户下的商机对归属销售可见、漏斗按人统计口径一致。

## 7. 核心业务规则（重点）

### 7.1 数据权限（`CustomerService` / `CurrentUser`）
- SALES 列表查询**强制** `owner_id = 当前人`，入参 ownerId 被忽略；SALES 访问别人客户 → `无权访问该客户`。
- MANAGER/ADMIN 看全部，可按 ownerId 筛，可调用 transfer 转移归属。
- `CustomerService.getAccessible(id)` 是**统一越权守卫**，联系人/商机/跟进/AI 都复用它，判断只写一份。

### 7.2 线索转客户（`LeadService.convert`，`@Transactional`）
校验线索存在且未转化 → 建客户（名字 = company 或 name，status=1）→ 建联系人 → 线索 status=2。
任何一步失败整体回滚。重复调用会提示"线索不存在或已转化"。

### 7.3 商机状态机（`OpportunityService.changeStage`）
1. 终态(赢单/输单)不可再变更；
2. 不能回退（目标 < 当前），唯一例外可跳到"输单"；
3. 赢单 → win_rate 置 100（可传参覆盖）；
4. 输单 → 记 fail_reason；
5. 每次变更自动插一条 `type=系统` 的跟进，形成时间线。

### 7.4 漏斗（`CrmOpportunityMapper.funnelAll/funnelByOwner`）
手写 `@Select` 聚合 SQL。注意：**手写 SQL 不会套逻辑删除**，所以必须手动 `WHERE deleted = 0`。

## 8. AI 助手配置

`application.yml`：

```yaml
ai:
  enabled: true                     # true=真实HTTP调用；false=Mock(离线可用)
  base-url: http://localhost:11434/v1
  api-key: ${AI_API_KEY:}           # Ollama 留空；DeepSeek 等填 sk-xxx
  model: qwen2.5
  timeout-seconds: 60
```

三种用法：

1. **离线 Mock（`ai.enabled=false`）**：不联网、不装模型，返回写死的"销售教练"回答，用于先跑通流程/演示。
2. **本地 Ollama（默认配置）**：
   ```bash
   ollama pull qwen2.5       # 先拉模型
   ollama serve              # 保持 Ollama 运行
   ```
   `ai.enabled=true` 且 base-url 指向 `http://localhost:11434/v1`，api-key 留空。
3. **真实厂商（DeepSeek / OpenAI 等 OpenAI 兼容服务）**：改 base-url、填 api-key、换 model。
   - DeepSeek：`base-url: https://api.deepseek.com/v1`，`api-key: sk-xxx`，`model: deepseek-chat`
   - OpenAI：`base-url: https://api.openai.com/v1`，`api-key: sk-xxx`

AI 调用走 `AiClient` 接口，业务代码只依赖接口，不关心底下是 Mock 还是真模型（面向接口编程）。
请求带客户/商机上下文时，`AiChatService` 会先把"客户档案+商机+最近跟进"拼好，同时做数据权限校验；问答都会落 `crm_ai_chat_log`。

## 9. 常见 curl 示例

> Windows 终端默认 GBK，curl 里直接写中文可能乱码。建议中文 body 存成 **UTF-8 文件**再 `--data @file.json`，或直接用 postman/接口文档调试。

```bash
# 登录，拿 token
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 带 token 查客户（管理员看全部）
curl "http://localhost:8081/api/customers?pageSize=10" \
  -H "Authorization: <你的token>"

# 线索转客户（幂等，跑两次第二次会被拒）
curl -X POST http://localhost:8081/api/leads/1/convert \
  -H "Authorization: <token>"

# 商机阶段推进：1初步接触 -> 5赢单
curl -X PUT http://localhost:8081/api/opportunities/6/stage \
  -H "Authorization: <token>" -H "Content-Type: application/json" \
  -d '{"targetStage":5}'

# 销售漏斗
curl "http://localhost:8081/api/dashboard/funnel" -H "Authorization: <token>"
```

## 10. 启用 Redis 会话（可选）

默认会话存内存：重启后要重新登录。若想"登录态持久化到 Redis"，前提 Redis 已启动：

1. `pom.xml` 打开 `sa-token-redis-jackson` 依赖的注释；
2. 新建 `config/SaTokenRedisConfig.java`：

```java
package com.aicrm.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoRedisJackson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class SaTokenRedisConfig {

    @Bean
    public SaTokenDao saTokenDao(RedisConnectionFactory factory) {
        // 注意：Sa-Token 1.39.0 的 SaTokenDaoRedisJackson 没有 RedisTemplate 构造器，
        // 需用 no-arg 构造 + init(RedisConnectionFactory) 方式注册
        SaTokenDaoRedisJackson dao = new SaTokenDaoRedisJackson();
        dao.init(factory);
        return dao;
    }
}
```

> 若启动报"重复 Bean / SaTokenDao"，把上面方法名改成 `saTokenDaoInit` 或用 `@Primary` 标注，以你实际运行的 Sa-Token 版本为准（README 基于 1.39.0）。

## 11. 常见坑（对照自己写的时候最容易错的）

1. **逻辑删除与手写 SQL**：逻辑删除只对 MyBatis-Plus 自动 SQL 生效；自己写 `@Select` 必须手动拼 `deleted=0`（漏斗就是例子）。
2. **没有 deleted 列的表，实体别写 deleted**：`sys_user`、`crm_ai_chat_log` 一旦写上 deleted 会 SQL 报错。
3. **金额用 `BigDecimal`**，别用 double 算钱。
4. **状态/阶段别用魔法数字**：统一走 `StageConstants`。
5. **新增时不需要手填 create_time/update_time**：数据库列默认 `CURRENT_TIMESTAMP`，实体为 null 时 MyBatis-Plus 会跳过该列。
6. **Sa-Token 会话默认在内存**：不配 Redis 也能跑；切 Redis 用上面第 10 节方式。

## 12. 与 ai-crm 骨架的对应

| 内容 | ai-crm（你的骨架，自己写） | ai-crm-full（本完整版） |
|------|------|------|
| 端口 | 8080 | 8081 |
| 数据库 | `ai_crm` | `ai_crm_full` |
| 登录/auth/common/config | ✅ 已有 | 相同（可直接对照） |
| 业务实体/服务/控制器 | ❌ 你来写 | ✅ 全量 |
| AI | ❌ | ✅ |
| 演示数据 | 仅用户 | 用户 + 一批客户/商机/跟进 |
