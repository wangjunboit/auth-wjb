# 动态 RBAC 网关鉴权 设计文档

- 日期: 2026-06-13
- 状态: 已评审通过，待出实施计划
- 涉及模块: auth-gateway / auth-service / sql / frontend

## 1. 背景与目标

### 现状

网关 [SaTokenGatewayConfig](../../../auth-gateway/src/main/java/com/wjb/auth/gateway/config/SaTokenGatewayConfig.java)
的 `SaReactorFilter.setAuth` 里用 **19 条硬编码 `SaRouter.match` 规则**逐条声明「哪个路径+方法需要哪个权限码」。
新增/调整接口都要回到网关改代码、重新部署，规则与 Controller 容易脱节、易漏配。

用户权限快照在登录时写入 Sa-Token 会话（`perms`），网关 `StpInterfaceImpl` 从会话读取，鉴权不连数据库。

### 目标

把网关鉴权从「硬编码规则」改造为**动态 RBAC**:把「URL+方法 → 权限码」的映射作为**数据**存储，
加载到 Redis，网关运行时动态匹配并校验当前用户权限。管理员在后台改动后**即时生效**，无需改代码或重启。

### 已确定的关键决策（评审结论）

1. **映射数据落地**: 挂在现有 `sys_menu` 表上（加 `api_url`/`api_method` 字段），由按钮型菜单（`menu_type='F'`）承载，复用现有 `sys_role_menu → 用户权限` 链路，不新建表。
2. **无映射默认策略**: 请求在映射表里找不到对应权限码时 = **只需登录即放行**（与现状行为一致，迁移不破坏）。
3. **缓存刷新策略**: 网关内存缓存 + Redis pub/sub **变更事件实时刷新**（后台改完即时生效）。
4. **改造范围**: 全栈（schema + 种子迁移 + 网关动态匹配 + auth-service 变更发布 + 管理端 CRUD + 前端表单）。
5. **实现路径**: 保留现有 `SaReactorFilter`，仅把 `setAuth` 回调体从硬编码规则改成动态查表；白名单、统一错误返回、Sa-Token 会话权限读取全部复用。不引入 Spring Security / OPA / Casbin。

## 2. 数据模型

### 2.1 `sys_menu` 加字段

```sql
ALTER TABLE sys_menu
  ADD COLUMN api_url    VARCHAR(200) NULL COMMENT '后端接口URL模式(Ant风格),仅按钮型菜单使用',
  ADD COLUMN api_method VARCHAR(10)  NULL COMMENT 'HTTP方法GET/POST/PUT/DELETE,空或*为任意';
```

一条「映射」 = 一行 `api_url IS NOT NULL` 的菜单（通常 `menu_type='F'` 按钮，本来就带 `perm`）。

### 2.2 匹配语义:最具体优先

请求 `(method, path)` 的所需权限码按如下算法确定:

1. 筛出「方法匹配（entry 的 `api_method` 为空/`*` 或等于请求方法）且 `api_url` 模式命中请求路径」的所有映射行;
2. 在命中集合里取 **`api_url` 模式最具体**的那一行（用 Spring `PathPattern.SPECIFICITY_COMPARATOR`）;
3. 该行的 `perm` 即所需权限码;
4. 若无任何行命中 → 不需要权限码（只需登录）。

由「最具体优先」天然解决原先 `notMatch("/list")` 这类特例:

- `GET /system/user/list` 同时命中 `/system/user/list` 与 `/system/user/*`，最具体者为前者 → `system:user:list`；
- `GET /system/user/5` 仅命中 `/system/user/*` → `system:user:query`。

## 3. 种子迁移（行为与现状完全等价）

### 3.1 标准按钮（按 `perm` UPDATE 现有菜单行）

| perm | api_method | api_url |
|---|---|---|
| system:user:list | GET | /system/user/list |
| system:user:add | POST | /system/user |
| system:user:edit | PUT | /system/user |
| system:user:remove | DELETE | /system/user/** |
| system:user:query | GET | /system/user/* |
| system:role:list | GET | /system/role/list |
| system:role:add | POST | /system/role |
| system:role:edit | PUT | /system/role |
| system:role:remove | DELETE | /system/role/** |
| system:role:query | GET | /system/role/* |
| system:role:assign | POST | /system/role/assign-menus |
| system:menu:list | GET | /system/menu/list |
| system:menu:add | POST | /system/menu |
| system:menu:edit | PUT | /system/menu |
| system:menu:remove | DELETE | /system/menu/** |
| system:menu:query | GET | /system/menu/* |

### 3.2 子资源接口:方式甲（隐藏菜单行，权限码沿用，行为零变化）

3 个接口现复用别的按钮权限码但 URL 不同，一行一按钮装不下，故各加一行 `visible=0` 隐藏菜单承载:

| api_method | api_url | perm（沿用现有） |
|---|---|---|
| POST | /system/user/assign-roles | system:user:edit |
| GET | /system/user/*/roles | system:user:query |
| GET | /system/role/*/menus | system:role:query |

> 选方式甲（而非给子资源新设独立权限码）的理由: 现有角色已分配的权限完全不用动，迁移后行为与现状一模一样，风险最小。将来要细化再单独做。

### 3.3 文件

- 新增 `sql/seed-m8-api-perm.sql`: ALTER 加字段 + 16 条 UPDATE + 3 行隐藏 INSERT;
- 同步更新 `sql/schema.sql` 的 `sys_menu` 建表语句（新库直接含两列）与 `sql/alter-comments.sql` 注释。

## 4. 网关侧（auth-gateway）

### 4.1 Redis 数据契约

- key `rbac:api-perms`（String，JSON 数组）: `[{"method":"GET","url":"/system/user/list","perm":"system:user:list"}, ...]`
- 刷新通道 `rbac:api-perms:refresh`（pub/sub）: 消息体随意，收到即重载。

单 JSON String 而非 Hash —— 网关每次整份快照重建，一把读出最简单。

### 4.2 新增组件 `ApiPermCache`

职责单一: 把 Redis 映射变成「能按请求查权限码」的内存快照。

- 状态: `volatile List<Entry> snapshot`，`Entry = (PathPattern pattern, String method, String perm)`，按模式具体度排序;
- 启动加载（`ApplicationRunner`）: 从 Redis 读 JSON → 用 `PathPatternParser` 编译 url → 排序 → 原子替换快照;
- pub/sub 监听（`RedisMessageListenerContainer` 订阅 `rbac:api-perms:refresh`）: 收到消息即重载 —— 实现「后台改完即时生效」;
- 对外: `String requiredPerm(String method, String path)` —— 按具体度顺序返回首个「方法匹配且 pattern 命中」的 `perm`，无匹配返回 `null`;
- 并发: 快照为不可变 List，刷新整体换 `volatile` 引用，读不加锁。

### 4.3 `setAuth` 改为动态查表

`saReactorFilter(ApiPermCache cache)` 注入缓存，回调体:

```java
.setAuth(obj -> {
    // 1) 白名单 + 登录校验(白名单清单与写法不变,抽成共用常量 WHITELIST)
    SaRouter.match("/**").notMatch(WHITELIST)
            .check(r -> StpUtil.checkLogin());

    // 2) 动态权限校验:查映射,命中才校验权限码,没命中=只需登录
    SaRouter.match("/**").notMatch(WHITELIST)
            .check(r -> {
                String method = SaHolder.getRequest().getMethod();
                String path   = SaHolder.getRequest().getRequestPath();
                String perm   = cache.requiredPerm(method, path);
                if (perm != null) {
                    StpUtil.checkPermission(perm);
                }
            });
})
.setError(e -> { /* 现有统一 JSON 返回,不动 */ });
```

- 权限校验仍走 `StpUtil.checkPermission` → 失败抛 `NotPermissionException` → 落现有 `setError` 返回 403，错误链路复用;
- 用户权限快照仍来自 Sa-Token 会话 `perms`（`StpInterfaceImpl` 读），零改动;
- 白名单清单抽成 `String[] WHITELIST` 常量，两处共用，避免写两遍。

### 4.4 依赖

auth-gateway 已有 `spring-boot-starter-data-redis` + `commons-pool2`，`RedisMessageListenerContainer`/`StringRedisTemplate` 自动配置即可;JSON 用 classpath 已有的 Jackson。**无需新依赖**。

## 5. auth-service 侧

### 5.1 实体/DTO 加字段

- `SysMenu`: 加 `apiUrl` / `apiMethod`（驼峰自动映射 `api_url`/`api_method`）;
- `MenuSaveRequest`: 加 `apiUrl` / `apiMethod`（可空，无校验）;
- `MenuTreeNode`: 加 `apiUrl` / `apiMethod`，并在 `from(SysMenu)` 带出（编辑回填 + 发布都要读）;
- `SysMenuService.applyTo(...)`: 把两字段 set 进实体。

### 5.2 发布组件 `ApiPermPublisher`

职责: 把 DB 映射同步到 Redis 并通知网关。

- `refresh()`:
  1. 查 `sys_menu` 中 `api_url` 非空的行 → 组装 `List<{method,url,perm}>`（过滤 `perm` 为空者）;
  2. Jackson 序列化 → 写 Redis key `rbac:api-perms`;
  3. `convertAndSend("rbac:api-perms:refresh", "1")` 通知网关。
- 触发时机: 启动时 `ApplicationRunner` 调一次（保证初始数据）;`SysMenuService.add/update/remove` 成功后调一次。
- 复用已有 `StringRedisTemplate`，无新依赖。

## 6. 前端（MenuManage.vue）

- 表单加两项，**仅 `menuType==='F'`（按钮）时显示**:
  - 接口URL: `<el-input v-model="form.apiUrl">`;
  - 接口方法: `<el-select v-model="form.apiMethod">` 选项 GET/POST/PUT/DELETE/任意(空)。
- `form` 初值、`resetForm()`、`openEdit(row)` 各加 `apiUrl`/`apiMethod`（回填依赖 5.1 让 `MenuTreeNode` 返回两字段）;
- 列表可选地加一列「接口」展示 `api_method api_url`;
- `frontend/src/api/menu.js` 不改（payload `...form` 透传）。

## 7. 错误处理与边界

- Redis 不可用 / key 为空: 网关快照为空 → 业务接口退化为「只需登录」（不会全站 403），打 `warn` 日志。（注: Redis 真挂时连 Sa-Token 登录校验本身也失败，属更上游问题。）
- 启动时序: 网关先起、Redis 无数据也无妨 —— auth-service 起来写 key 并发刷新消息，网关收到补齐;网关启动自身也主动读一次。
- 模式匹配用 Spring WebFlux 自带的 `PathPattern`;`api_method` 为空/`*` 表示任意方法。

## 8. 测试策略

- **auth-gateway 新增 `ApiPermCacheTest`**（离线，不连 Redis）: 喂一组 entry，断言
  - 最具体优先: `GET /system/user/list` → list 而非 query;
  - 方法过滤: `POST /system/user` → add;`GET /system/user/5` → query;
  - 子资源: `GET /system/user/5/roles` → query（验证 `/system/user/*` 单层不误吞）;
  - 无匹配 → `null`。
- **auth-service `ApiPermPublisherTest`（可选，mock mapper）**: 断言从菜单列表正确组装 JSON、过滤空 perm。
- 现有 15 个测试保持绿;`SaTokenGatewayConfig` 改动后保证编译通过。
- 完成判据: `mvn clean test`（JDK21）全绿。

## 9. 不做（YAGNI）

- 不做接口级 ABAC / 数据行级权限;
- 不引入 Spring Security / OPA / Casbin;
- 网关侧不做定时兜底重载（pub/sub + 启动加载已够）;
- 不改造 user/role/menu 之外的鉴权。

## 10. 落地清单速览

| 模块 | 改动 |
|---|---|
| sql | 加字段 + 种子迁移（16 改 + 3 隐藏行）+ schema/comments 同步 |
| auth-service | SysMenu/MenuSaveRequest/MenuTreeNode/applyTo 加字段;新增 ApiPermPublisher;菜单增删改后 refresh |
| auth-gateway | 新增 ApiPermCache（加载+pub/sub+匹配）;SaTokenGatewayConfig.setAuth 改动态查表 |
| frontend | MenuManage.vue 表单加 apiUrl/apiMethod（按钮型显示） |
| 测试 | ApiPermCacheTest + 可选 publisher 测试;mvn clean test 全绿 |

## 附: 构建环境提示

本机全局 `JAVA_HOME` 指向 JRE1.8（无 javac），Maven 构建需用 JDK21:
`$env:JAVA_HOME='C:\Program Files\Java\latest\jdk-21'; mvn ...`
