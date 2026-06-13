# 动态 RBAC 网关鉴权 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把网关鉴权从硬编码 `SaRouter.match` 规则改造为动态 RBAC：`URL+方法→权限码` 映射挂在 `sys_menu`，由 auth-service 同步到 Redis，网关内存缓存 + pub/sub 实时刷新，运行时「最具体优先」匹配并用 Sa-Token 校验当前用户权限。

**Architecture:** auth-service 拥有「DB→Redis」同步（`ApiPermPublisher`），网关拥有「Redis→内存」消费（`ApiPermCache` + 纯逻辑 `ApiPermMatcher`）。网关 `SaReactorFilter.setAuth` 保留不变的白名单/登录校验/统一错误返回，只把权限规则来源换成动态查表。无映射的接口=只需登录。

**Tech Stack:** Spring Cloud Gateway (WebFlux) + Sa-Token + MyBatis-Plus + Redis(StringRedisTemplate / RedisMessageListenerContainer) + Jackson + Spring `PathPattern` + Vue3/Element-Plus。构建用 JDK21（`$env:JAVA_HOME='C:\Program Files\Java\latest\jdk-21'`）。

**设计文档:** [docs/superpowers/specs/2026-06-13-dynamic-rbac-gateway-design.md](../specs/2026-06-13-dynamic-rbac-gateway-design.md)
**当前分支:** `feature/dynamic-rbac-gateway`

---

## 文件结构

**auth-common（共享契约）**
- 新建 `auth-common/src/main/java/com/wjb/auth/common/rbac/ApiPermDef.java` — 映射条目 DTO（method/url/perm）
- 修改 `auth-common/src/main/java/com/wjb/auth/common/constant/SecurityConstants.java` — Redis key 与刷新频道常量

**sql**
- 新建 `sql/seed-m8-api-perm.sql` — 加字段 + 16 条 UPDATE + 3 行隐藏 INSERT
- 修改 `sql/schema.sql`、`sql/alter-comments.sql` — sys_menu 同步两列

**auth-service**
- 修改 `entity/SysMenu.java`、`dto/MenuSaveRequest.java`、`dto/MenuTreeNode.java`、`service/SysMenuService.java`
- 新建 `rbac/ApiPermPublisher.java`
- 新建测试 `src/test/java/com/wjb/auth/ApiPermPublisherTest.java`

**auth-gateway**
- 新建 `security/ApiPermMatcher.java`（纯逻辑）、`security/ApiPermCache.java`（Spring 组件）、`config/ApiPermRedisConfig.java`（pub/sub 注册）
- 修改 `config/SaTokenGatewayConfig.java`、`auth-gateway/pom.xml`（加 test 依赖）
- 新建测试 `src/test/java/com/wjb/auth/gateway/ApiPermMatcherTest.java`

**frontend**
- 修改 `frontend/src/views/system/MenuManage.vue`

---

## Task 1: auth-common 共享契约（ApiPermDef + 常量）

**Files:**
- Create: `auth-common/src/main/java/com/wjb/auth/common/rbac/ApiPermDef.java`
- Modify: `auth-common/src/main/java/com/wjb/auth/common/constant/SecurityConstants.java`

- [ ] **Step 1: 创建 ApiPermDef record**

`auth-common/src/main/java/com/wjb/auth/common/rbac/ApiPermDef.java`:

```java
package com.wjb.auth.common.rbac;

/**
 * 动态 RBAC 的一条「接口→权限码」映射,网关与 auth-service 间的 JSON 契约。
 * method: HTTP 方法(GET/POST/...),null 或 "*" 表示任意;url: Ant 风格路径模式;perm: 所需权限码。
 */
public record ApiPermDef(String method, String url, String perm) {
}
```

- [ ] **Step 2: 在 SecurityConstants 加两个常量**

修改 `SecurityConstants.java`，在 `HEADER_USER_ID` 常量后追加：

```java
    /** 动态RBAC:网关读取的 URL→权限码 映射(JSON 数组) 的 Redis key */
    public static final String API_PERM_KEY = "rbac:api-perms";

    /** 动态RBAC:映射变更通知的 pub/sub 频道,网关订阅后重载 */
    public static final String API_PERM_REFRESH_CHANNEL = "rbac:api-perms:refresh";
```

- [ ] **Step 3: 编译验证**

Run: `$env:JAVA_HOME='C:\Program Files\Java\latest\jdk-21'; mvn -q -pl auth-common compile`
Expected: 无输出（成功）

- [ ] **Step 4: Commit**

```bash
git add auth-common/src/main/java/com/wjb/auth/common/rbac/ApiPermDef.java auth-common/src/main/java/com/wjb/auth/common/constant/SecurityConstants.java
git commit -m "feat(rbac): auth-common 加 ApiPermDef 契约与 Redis 常量"
```

---

## Task 2: SQL — sys_menu 加字段 + 种子迁移

**Files:**
- Create: `sql/seed-m8-api-perm.sql`
- Modify: `sql/schema.sql:44-61`（sys_menu 建表）, `sql/alter-comments.sql:34-49`

- [ ] **Step 1: 新建迁移脚本 sql/seed-m8-api-perm.sql**

```sql
-- M8 动态RBAC:sys_menu 加 api_url/api_method,把现有 19 条网关规则迁为数据
-- 说明:list 权限码在 C 型菜单(101/102/103)上,其余在 F 按钮上;按 perm 更新即可

-- 1) 加字段(只跑一次)
ALTER TABLE sys_menu ADD COLUMN api_url    VARCHAR(200) NULL COMMENT '后端接口URL模式(Ant风格),仅承载鉴权映射的菜单使用' AFTER perm;
ALTER TABLE sys_menu ADD COLUMN api_method VARCHAR(10)  NULL COMMENT 'HTTP方法GET/POST/PUT/DELETE,空或*为任意' AFTER api_url;

-- 2) 标准 16 条(按 perm 更新现有菜单行)
UPDATE sys_menu SET api_method='GET',    api_url='/system/user/list'           WHERE perm='system:user:list';
UPDATE sys_menu SET api_method='POST',   api_url='/system/user'                WHERE perm='system:user:add';
UPDATE sys_menu SET api_method='PUT',    api_url='/system/user'                WHERE perm='system:user:edit';
UPDATE sys_menu SET api_method='DELETE', api_url='/system/user/**'             WHERE perm='system:user:remove';
UPDATE sys_menu SET api_method='GET',    api_url='/system/user/*'              WHERE perm='system:user:query';
UPDATE sys_menu SET api_method='GET',    api_url='/system/role/list'           WHERE perm='system:role:list';
UPDATE sys_menu SET api_method='POST',   api_url='/system/role'                WHERE perm='system:role:add';
UPDATE sys_menu SET api_method='PUT',    api_url='/system/role'                WHERE perm='system:role:edit';
UPDATE sys_menu SET api_method='DELETE', api_url='/system/role/**'             WHERE perm='system:role:remove';
UPDATE sys_menu SET api_method='GET',    api_url='/system/role/*'              WHERE perm='system:role:query';
UPDATE sys_menu SET api_method='POST',   api_url='/system/role/assign-menus'   WHERE perm='system:role:assign';
UPDATE sys_menu SET api_method='GET',    api_url='/system/menu/list'           WHERE perm='system:menu:list';
UPDATE sys_menu SET api_method='POST',   api_url='/system/menu'                WHERE perm='system:menu:add';
UPDATE sys_menu SET api_method='PUT',    api_url='/system/menu'                WHERE perm='system:menu:edit';
UPDATE sys_menu SET api_method='DELETE', api_url='/system/menu/**'             WHERE perm='system:menu:remove';
UPDATE sys_menu SET api_method='GET',    api_url='/system/menu/*'              WHERE perm='system:menu:query';

-- 3) 方式甲:3 个子资源接口,各加一行隐藏菜单(visible=0)承载映射,perm 沿用现有
--    这些行不授予任何角色(它们的 perm 已通过 1013/1011/1021 授权),仅供网关查映射
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, api_url, api_method, sort, visible, deleted)
SELECT 1015, 101, '用户分配角色(隐藏)', 'F', 'system:user:edit',  '/system/user/assign-roles', 'POST', 90, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1015);
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, api_url, api_method, sort, visible, deleted)
SELECT 1016, 101, '用户角色查询(隐藏)', 'F', 'system:user:query', '/system/user/*/roles',      'GET',  91, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1016);
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, api_url, api_method, sort, visible, deleted)
SELECT 1026, 102, '角色菜单查询(隐藏)', 'F', 'system:role:query', '/system/role/*/menus',      'GET',  90, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1026);
```

- [ ] **Step 2: 同步 schema.sql 的 sys_menu 建表**

修改 `sql/schema.sql`，在 `sys_menu` 表 `perm` 列之后插入两列。原文（第 51 行）：

```sql
    perm        VARCHAR(100) NULL COMMENT '权限码',
```

改为：

```sql
    perm        VARCHAR(100) NULL COMMENT '权限码',
    api_url     VARCHAR(200) NULL COMMENT '后端接口URL模式(Ant风格),仅承载鉴权映射的菜单使用',
    api_method  VARCHAR(10)  NULL COMMENT 'HTTP方法GET/POST/PUT/DELETE,空或*为任意',
```

- [ ] **Step 3: 同步 alter-comments.sql**

修改 `sql/alter-comments.sql`，在 sys_menu 段 `MODIFY COLUMN perm ...` 行（第 41 行）之后插入：

```sql
  MODIFY COLUMN api_url     VARCHAR(200) NULL COMMENT '后端接口URL模式(Ant风格)',
  MODIFY COLUMN api_method  VARCHAR(10)  NULL COMMENT 'HTTP方法,空或*为任意',
```

- [ ] **Step 4: （若有本地库）手动执行迁移脚本**

Run: `mysql -u<user> -p<pwd> <db> < sql/seed-m8-api-perm.sql`
Expected: 无报错（ALTER 若已加过会报 duplicate column，可忽略或先删列重跑）
说明：无本地库则跳过，靠后续应用启动时不依赖此步（单测不连库）。

- [ ] **Step 5: Commit**

```bash
git add sql/seed-m8-api-perm.sql sql/schema.sql sql/alter-comments.sql
git commit -m "feat(rbac): sys_menu 加 api_url/api_method 字段并迁移网关规则为种子数据"
```

---

## Task 3: auth-service — 实体/DTO 加字段

**Files:**
- Modify: `auth-service/src/main/java/com/wjb/auth/entity/SysMenu.java`
- Modify: `auth-service/src/main/java/com/wjb/auth/dto/MenuSaveRequest.java`
- Modify: `auth-service/src/main/java/com/wjb/auth/dto/MenuTreeNode.java`
- Modify: `auth-service/src/main/java/com/wjb/auth/service/SysMenuService.java:90-100`

- [ ] **Step 1: SysMenu 加两字段**

在 `SysMenu.java` 的 `private Integer visible;` 之后追加（MyBatis-Plus 驼峰自动映射 api_url/api_method，无需 @TableField）：

```java
    private String apiUrl;
    private String apiMethod;
```

- [ ] **Step 2: MenuSaveRequest 加两字段**

在 `MenuSaveRequest.java` 的 `private Integer visible;` 之后追加：

```java
    private String apiUrl;
    private String apiMethod;
```

- [ ] **Step 3: MenuTreeNode 加两字段并在 from() 带出**

在 `MenuTreeNode.java` 字段区 `private Integer visible;` 之后追加：

```java
    private String apiUrl;
    private String apiMethod;
```

并在 `from(SysMenu m)` 方法的 `n.visible = m.getVisible();` 之后追加：

```java
        n.apiUrl = m.getApiUrl();
        n.apiMethod = m.getApiMethod();
```

- [ ] **Step 4: SysMenuService.applyTo 同步两字段**

在 `SysMenuService.java` 的 `applyTo(SysMenu menu, MenuSaveRequest req)` 方法里，`menu.setVisible(...)` 那行之后追加：

```java
        menu.setApiUrl(req.getApiUrl());
        menu.setApiMethod(req.getApiMethod());
```

- [ ] **Step 5: 编译验证**

Run: `$env:JAVA_HOME='C:\Program Files\Java\latest\jdk-21'; mvn -q -pl auth-service -am compile`
Expected: 无输出（成功）

- [ ] **Step 6: Commit**

```bash
git add auth-service/src/main/java/com/wjb/auth/entity/SysMenu.java auth-service/src/main/java/com/wjb/auth/dto/MenuSaveRequest.java auth-service/src/main/java/com/wjb/auth/dto/MenuTreeNode.java auth-service/src/main/java/com/wjb/auth/service/SysMenuService.java
git commit -m "feat(rbac): 菜单实体/DTO 增加 apiUrl/apiMethod 字段"
```

---

## Task 4: auth-service — ApiPermPublisher（DB→Redis 同步 + 发布）

**Files:**
- Create: `auth-service/src/main/java/com/wjb/auth/rbac/ApiPermPublisher.java`
- Create: `auth-service/src/test/java/com/wjb/auth/ApiPermPublisherTest.java`
- Modify: `auth-service/src/main/java/com/wjb/auth/service/SysMenuService.java`

- [ ] **Step 1: 写 buildDefs 的失败单测**

`auth-service/src/test/java/com/wjb/auth/ApiPermPublisherTest.java`:

```java
package com.wjb.auth;

import com.wjb.auth.common.rbac.ApiPermDef;
import com.wjb.auth.entity.SysMenu;
import com.wjb.auth.rbac.ApiPermPublisher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiPermPublisherTest {

    private SysMenu menu(String perm, String url, String method) {
        SysMenu m = new SysMenu();
        m.setPerm(perm);
        m.setApiUrl(url);
        m.setApiMethod(method);
        return m;
    }

    @Test
    void buildDefs_filtersBlankPermOrUrl_andMapsFields() {
        List<SysMenu> rows = List.of(
                menu("system:user:list", "/system/user/list", "GET"),
                menu("", "/system/user", "POST"),          // 空 perm,丢弃
                menu("system:user:add", "", "POST"),       // 空 url,丢弃
                menu("system:user:query", "/system/user/*", null) // method 允许 null
        );
        List<ApiPermDef> defs = ApiPermPublisher.buildDefs(rows);
        assertEquals(2, defs.size());
        assertEquals("GET", defs.get(0).method());
        assertEquals("/system/user/list", defs.get(0).url());
        assertEquals("system:user:list", defs.get(0).perm());
        assertTrue(defs.stream().anyMatch(d -> d.perm().equals("system:user:query") && d.method() == null));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `$env:JAVA_HOME='C:\Program Files\Java\latest\jdk-21'; mvn -q -pl auth-service -am test -Dtest=ApiPermPublisherTest`
Expected: 编译失败（`ApiPermPublisher` 不存在 / 无 `buildDefs`）

- [ ] **Step 3: 实现 ApiPermPublisher**

`auth-service/src/main/java/com/wjb/auth/rbac/ApiPermPublisher.java`:

```java
package com.wjb.auth.rbac;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjb.auth.common.constant.SecurityConstants;
import com.wjb.auth.common.rbac.ApiPermDef;
import com.wjb.auth.entity.SysMenu;
import com.wjb.auth.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/** 把 sys_menu 中的 接口→权限码 映射同步到 Redis,并通知网关刷新。启动时与菜单变更后各发布一次。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiPermPublisher implements ApplicationRunner {

    private final SysMenuMapper menuMapper;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        refresh();
    }

    /** 查表 → 组装 → 写 Redis → 发刷新消息 */
    public void refresh() {
        List<SysMenu> rows = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().isNotNull(SysMenu::getApiUrl));
        List<ApiPermDef> defs = buildDefs(rows);
        try {
            String json = objectMapper.writeValueAsString(defs);
            redis.opsForValue().set(SecurityConstants.API_PERM_KEY, json);
            redis.convertAndSend(SecurityConstants.API_PERM_REFRESH_CHANNEL, "1");
            log.info("已发布 API 权限映射 {} 条", defs.size());
        } catch (Exception e) {
            log.error("发布 API 权限映射失败", e);
        }
    }

    /** 纯函数:从菜单行组装映射,过滤 perm/url 为空者(便于单测) */
    public static List<ApiPermDef> buildDefs(List<SysMenu> rows) {
        List<ApiPermDef> defs = new ArrayList<>();
        for (SysMenu m : rows) {
            if (StringUtils.hasText(m.getApiUrl()) && StringUtils.hasText(m.getPerm())) {
                defs.add(new ApiPermDef(m.getApiMethod(), m.getApiUrl(), m.getPerm()));
            }
        }
        return defs;
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `$env:JAVA_HOME='C:\Program Files\Java\latest\jdk-21'; mvn -q -pl auth-service -am test -Dtest=ApiPermPublisherTest`
Expected: BUILD SUCCESS，Tests run: 1, Failures: 0

- [ ] **Step 5: 接入 SysMenuService（增删改后发布）**

修改 `SysMenuService.java`：
1. 字段区追加依赖（已是 `@RequiredArgsConstructor`，加 final 字段即自动注入）：

```java
    private final com.wjb.auth.rbac.ApiPermPublisher apiPermPublisher;
```

2. 在 `add(...)` 的 `menuMapper.insert(menu);` 之后追加 `apiPermPublisher.refresh();`
3. 在 `update(...)` 的 `menuMapper.updateById(menu);` 之后追加 `apiPermPublisher.refresh();`
4. 在 `remove(...)` 的 `menuMapper.deleteById(id);` 之后追加 `apiPermPublisher.refresh();`

> 注意:`SysMenuServiceTest` 现以 `new SysMenuService(menuMapper, roleMenuMapper)` 构造,新增依赖后该构造签名变为 3 参。需同步改测试(见 Step 6)。

- [ ] **Step 6: 修复 SysMenuServiceTest 构造签名**

修改 `auth-service/src/test/java/com/wjb/auth/SysMenuServiceTest.java`：为新依赖加一个 mock 并传入构造器。
在已有 mock 声明区追加：

```java
    private final com.wjb.auth.rbac.ApiPermPublisher apiPermPublisher = mock(com.wjb.auth.rbac.ApiPermPublisher.class);
```

并把 `new SysMenuService(menuMapper, roleMenuMapper)` 改为：

```java
    private final SysMenuService service = new SysMenuService(menuMapper, roleMenuMapper, apiPermPublisher);
```

（若该测试用 `@Mock`/`@ExtendWith` 风格而非 `mock(...)`，则按其现有风格加一个 `@Mock ApiPermPublisher apiPermPublisher` 并放进构造调用。实现前先读该测试文件确认风格。）

- [ ] **Step 7: 运行 auth-service 全部测试**

Run: `$env:JAVA_HOME='C:\Program Files\Java\latest\jdk-21'; mvn -q -pl auth-service -am test`
Expected: BUILD SUCCESS，原有测试 + ApiPermPublisherTest 全绿

- [ ] **Step 8: Commit**

```bash
git add auth-service/src/main/java/com/wjb/auth/rbac/ApiPermPublisher.java auth-service/src/test/java/com/wjb/auth/ApiPermPublisherTest.java auth-service/src/main/java/com/wjb/auth/service/SysMenuService.java auth-service/src/test/java/com/wjb/auth/SysMenuServiceTest.java
git commit -m "feat(rbac): auth-service 新增 ApiPermPublisher,菜单变更同步映射到 Redis"
```

---

## Task 5: auth-gateway — ApiPermMatcher（纯逻辑,TDD 先行）

**Files:**
- Modify: `auth-gateway/pom.xml`（加 test 依赖）
- Create: `auth-gateway/src/test/java/com/wjb/auth/gateway/ApiPermMatcherTest.java`
- Create: `auth-gateway/src/main/java/com/wjb/auth/gateway/security/ApiPermMatcher.java`

- [ ] **Step 1: 给 auth-gateway 加测试依赖**

修改 `auth-gateway/pom.xml`，在 `</dependencies>` 之前追加：

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
```

- [ ] **Step 2: 写失败单测 ApiPermMatcherTest**

`auth-gateway/src/test/java/com/wjb/auth/gateway/ApiPermMatcherTest.java`:

```java
package com.wjb.auth.gateway;

import com.wjb.auth.common.rbac.ApiPermDef;
import com.wjb.auth.gateway.security.ApiPermMatcher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApiPermMatcherTest {

    private ApiPermMatcher matcher() {
        return new ApiPermMatcher(List.of(
                new ApiPermDef("GET", "/system/user/list", "system:user:list"),
                new ApiPermDef("POST", "/system/user", "system:user:add"),
                new ApiPermDef("PUT", "/system/user", "system:user:edit"),
                new ApiPermDef("DELETE", "/system/user/**", "system:user:remove"),
                new ApiPermDef("GET", "/system/user/*", "system:user:query"),
                new ApiPermDef("GET", "/system/user/*/roles", "system:user:query"),
                new ApiPermDef("POST", "/system/user/assign-roles", "system:user:edit")
        ));
    }

    @Test
    void listBeatsQueryBySpecificity() {
        assertEquals("system:user:list", matcher().requiredPerm("GET", "/system/user/list"));
    }

    @Test
    void detailMatchesQuery() {
        assertEquals("system:user:query", matcher().requiredPerm("GET", "/system/user/5"));
    }

    @Test
    void methodFiltering() {
        assertEquals("system:user:add", matcher().requiredPerm("POST", "/system/user"));
        assertEquals("system:user:edit", matcher().requiredPerm("PUT", "/system/user"));
        assertEquals("system:user:remove", matcher().requiredPerm("DELETE", "/system/user/9"));
    }

    @Test
    void subResourceNotSwallowedBySingleStar() {
        assertEquals("system:user:query", matcher().requiredPerm("GET", "/system/user/5/roles"));
    }

    @Test
    void assignRolesMapsToEdit() {
        assertEquals("system:user:edit", matcher().requiredPerm("POST", "/system/user/assign-roles"));
    }

    @Test
    void unmappedReturnsNull() {
        assertNull(matcher().requiredPerm("GET", "/auth/userinfo"));
    }

    @Test
    void blankUrlOrPermEntriesIgnored() {
        ApiPermMatcher m = new ApiPermMatcher(List.of(
                new ApiPermDef("GET", "", "system:x:y"),
                new ApiPermDef("GET", "/a", "")
        ));
        assertNull(m.requiredPerm("GET", "/a"));
    }
}
```

- [ ] **Step 3: 运行确认失败**

Run: `$env:JAVA_HOME='C:\Program Files\Java\latest\jdk-21'; mvn -q -pl auth-gateway -am test -Dtest=ApiPermMatcherTest`
Expected: 编译失败（`ApiPermMatcher` 不存在）

- [ ] **Step 4: 实现 ApiPermMatcher**

`auth-gateway/src/main/java/com/wjb/auth/gateway/security/ApiPermMatcher.java`:

```java
package com.wjb.auth.gateway.security;

import com.wjb.auth.common.rbac.ApiPermDef;
import org.springframework.http.server.PathContainer;
import org.springframework.util.StringUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.ArrayList;
import java.util.List;

/**
 * 不可变的「接口→权限码」匹配器:把映射编译成 PathPattern,按具体度排序(最具体优先),
 * requiredPerm 返回首个「方法匹配且路径命中」的权限码,无匹配返回 null(=只需登录)。
 */
public final class ApiPermMatcher {

    private static final PathPatternParser PARSER = PathPatternParser.defaultInstance;

    private final List<Entry> entries;

    public ApiPermMatcher(List<ApiPermDef> defs) {
        List<Entry> list = new ArrayList<>();
        for (ApiPermDef d : defs) {
            if (d == null || !StringUtils.hasText(d.url()) || !StringUtils.hasText(d.perm())) {
                continue;
            }
            list.add(new Entry(PARSER.parse(d.url()), d.method(), d.perm()));
        }
        list.sort((a, b) -> PathPattern.SPECIFICITY_COMPARATOR.compare(a.pattern(), b.pattern()));
        this.entries = List.copyOf(list);
    }

    /** 返回该请求所需权限码;无任何映射命中返回 null */
    public String requiredPerm(String method, String path) {
        PathContainer pc = PathContainer.parsePath(path);
        for (Entry e : entries) {
            if (methodMatches(e.method(), method) && e.pattern().matches(pc)) {
                return e.perm();
            }
        }
        return null;
    }

    private static boolean methodMatches(String entryMethod, String requestMethod) {
        if (!StringUtils.hasText(entryMethod) || "*".equals(entryMethod)) {
            return true;
        }
        return entryMethod.equalsIgnoreCase(requestMethod);
    }

    private record Entry(PathPattern pattern, String method, String perm) {}
}
```

- [ ] **Step 5: 运行确认通过**

Run: `$env:JAVA_HOME='C:\Program Files\Java\latest\jdk-21'; mvn -q -pl auth-gateway -am test -Dtest=ApiPermMatcherTest`
Expected: BUILD SUCCESS，Tests run: 7, Failures: 0

- [ ] **Step 6: Commit**

```bash
git add auth-gateway/pom.xml auth-gateway/src/test/java/com/wjb/auth/gateway/ApiPermMatcherTest.java auth-gateway/src/main/java/com/wjb/auth/gateway/security/ApiPermMatcher.java
git commit -m "feat(rbac): 网关 ApiPermMatcher 最具体优先匹配(含离线单测)"
```

---

## Task 6: auth-gateway — ApiPermCache（Redis 加载 + pub/sub 刷新）

**Files:**
- Create: `auth-gateway/src/main/java/com/wjb/auth/gateway/security/ApiPermCache.java`
- Create: `auth-gateway/src/main/java/com/wjb/auth/gateway/config/ApiPermRedisConfig.java`

- [ ] **Step 1: 实现 ApiPermCache**

`auth-gateway/src/main/java/com/wjb/auth/gateway/security/ApiPermCache.java`:

```java
package com.wjb.auth.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjb.auth.common.constant.SecurityConstants;
import com.wjb.auth.common.rbac.ApiPermDef;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/** 网关侧映射缓存:启动从 Redis 加载,订阅刷新频道实时重载;对外提供按请求查权限码。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiPermCache implements ApplicationRunner, MessageListener {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    private volatile ApiPermMatcher matcher = new ApiPermMatcher(List.of());

    @Override
    public void run(ApplicationArguments args) {
        load();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        load();
    }

    /** 从 Redis 读 JSON 重建快照;失败则沿用旧快照 */
    public void load() {
        try {
            String json = redis.opsForValue().get(SecurityConstants.API_PERM_KEY);
            List<ApiPermDef> defs = (json == null || json.isBlank())
                    ? List.of()
                    : objectMapper.readerForListOf(ApiPermDef.class).readValue(json);
            this.matcher = new ApiPermMatcher(defs);
            log.info("已加载 API 权限映射 {} 条", defs.size());
        } catch (Exception e) {
            log.warn("加载 API 权限映射失败,沿用旧快照", e);
        }
    }

    /** 返回该请求所需权限码;无映射返回 null(=只需登录) */
    public String requiredPerm(String method, String path) {
        return matcher.requiredPerm(method, path);
    }
}
```

- [ ] **Step 2: 注册 pub/sub 监听容器**

`auth-gateway/src/main/java/com/wjb/auth/gateway/config/ApiPermRedisConfig.java`:

```java
package com.wjb.auth.gateway.config;

import com.wjb.auth.common.constant.SecurityConstants;
import com.wjb.auth.gateway.security.ApiPermCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/** 订阅 Redis 刷新频道,收到消息时由 ApiPermCache 重载映射 */
@Configuration
public class ApiPermRedisConfig {

    @Bean
    public RedisMessageListenerContainer apiPermListenerContainer(
            RedisConnectionFactory connectionFactory, ApiPermCache apiPermCache) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(apiPermCache,
                new ChannelTopic(SecurityConstants.API_PERM_REFRESH_CHANNEL));
        return container;
    }
}
```

- [ ] **Step 3: 编译验证**

Run: `$env:JAVA_HOME='C:\Program Files\Java\latest\jdk-21'; mvn -q -pl auth-gateway -am compile`
Expected: 无输出（成功）

- [ ] **Step 4: Commit**

```bash
git add auth-gateway/src/main/java/com/wjb/auth/gateway/security/ApiPermCache.java auth-gateway/src/main/java/com/wjb/auth/gateway/config/ApiPermRedisConfig.java
git commit -m "feat(rbac): 网关 ApiPermCache 启动加载 + Redis pub/sub 实时刷新"
```

---

## Task 7: auth-gateway — setAuth 改为动态查表

**Files:**
- Modify: `auth-gateway/src/main/java/com/wjb/auth/gateway/config/SaTokenGatewayConfig.java`

- [ ] **Step 1: 抽白名单常量 + 注入 ApiPermCache + 重写 setAuth**

把 `SaTokenGatewayConfig.java` 整体替换为：

```java
package com.wjb.auth.gateway.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.wjb.auth.gateway.security.ApiPermCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/** 网关统一鉴权:登录态 + 动态 RBAC(查映射决定所需权限码),失败返回统一 JSON */
@Configuration
public class SaTokenGatewayConfig {

    /** 鉴权白名单(放行:无需登录) */
    private static final String[] WHITELIST = {
            "/auth/login", "/auth/login/**", "/auth/sms-code", "/auth/email-code",
            "/auth/oauth/**", "/auth/captcha", "/health", "/doc.html",
            "/webjars/**", "/v3/api-docs/**", "/swagger-ui/**", "/favicon.ico"
    };

    @Bean
    public SaReactorFilter saReactorFilter(ApiPermCache apiPermCache) {
        return new SaReactorFilter()
                .addInclude("/**")
                .setAuth(obj -> {
                    // 1) 白名单外:必须登录
                    SaRouter.match("/**").notMatch(WHITELIST)
                            .check(r -> StpUtil.checkLogin());

                    // 2) 动态权限校验:查映射,命中才校验权限码,未命中=只需登录
                    SaRouter.match("/**").notMatch(WHITELIST)
                            .check(r -> {
                                String method = SaHolder.getRequest().getMethod();
                                String path = SaHolder.getRequest().getRequestPath();
                                String perm = apiPermCache.requiredPerm(method, path);
                                if (perm != null) {
                                    StpUtil.checkPermission(perm);
                                }
                            });
                })
                .setError(e -> {
                    SaHolder.getResponse().setHeader("Content-Type", "application/json; charset=utf-8");
                    Map<String, Object> body = new HashMap<>();
                    if (e instanceof NotLoginException) {
                        body.put("code", 401);
                        body.put("msg", "登录已过期,请重新登录");
                    } else if (e instanceof NotPermissionException) {
                        body.put("code", 403);
                        body.put("msg", "无操作权限");
                    } else {
                        body.put("code", 500);
                        body.put("msg", e.getMessage());
                    }
                    body.put("data", null);
                    return body;
                });
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `$env:JAVA_HOME='C:\Program Files\Java\latest\jdk-21'; mvn -q -pl auth-gateway -am compile`
Expected: 无输出（成功）

- [ ] **Step 3: Commit**

```bash
git add auth-gateway/src/main/java/com/wjb/auth/gateway/config/SaTokenGatewayConfig.java
git commit -m "feat(rbac): 网关 setAuth 改为动态查表,移除硬编码 SaRouter.match 规则"
```

---

## Task 8: frontend — MenuManage.vue 加 apiUrl/apiMethod 表单项

**Files:**
- Modify: `frontend/src/views/system/MenuManage.vue`

- [ ] **Step 1: form 初值与 resetForm 加两字段**

把 `const form = reactive({ ... })` 那行（含 `sort: 0`）改为：

```javascript
const form = reactive({ id: null, parentId: null, menuName: '', menuType: 'C', path: '', component: '', perm: '', sort: 0, apiUrl: '', apiMethod: '' })
```

在 `resetForm` 函数体里 `form.perm = ''; form.sort = 0` 一行末尾追加：

```javascript
  form.apiUrl = ''; form.apiMethod = ''
```

- [ ] **Step 2: openEdit 回填两字段**

在 `openEdit(row)` 里 `form.component = row.component; form.perm = row.perm; form.sort = row.sort || 0` 之后追加：

```javascript
  form.apiUrl = row.apiUrl || ''; form.apiMethod = row.apiMethod || ''
```

- [ ] **Step 3: 表单加两项(仅按钮型显示)**

在 `<el-form-item label="权限码">...</el-form-item>` 之后插入：

```html
        <el-form-item label="接口URL" v-if="form.menuType === 'F'">
          <el-input v-model="form.apiUrl" placeholder="如 /system/user/list,支持 * 与 **" />
        </el-form-item>
        <el-form-item label="接口方法" v-if="form.menuType === 'F'">
          <el-select v-model="form.apiMethod" clearable placeholder="任意" style="width: 100%">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
```

- [ ] **Step 4: 构建前端验证（若已装依赖）**

Run: `cd frontend; npm run build`
Expected: 构建成功，无报错
说明：若未装依赖（`npm install` 未跑过）则跳过，仅做语法肉眼检查。

- [ ] **Step 5: Commit**

```bash
git add frontend/src/views/system/MenuManage.vue
git commit -m "feat(rbac): 菜单管理页按钮型菜单可配置 apiUrl/apiMethod"
```

---

## Task 9: 全量验证与收尾

**Files:** 无（仅验证）

- [ ] **Step 1: 全量编译 + 测试**

Run: `$env:JAVA_HOME='C:\Program Files\Java\latest\jdk-21'; mvn clean test`
Expected: BUILD SUCCESS，三模块全编译，`Tests run: N, Failures: 0, Errors: 0`（含原 15 个 + ApiPermPublisherTest 1 + ApiPermMatcherTest 7）

- [ ] **Step 2: （可选,需 Redis+Nacos+MySQL）端到端手验**

启动 auth-service 与 auth-gateway，验证：
- 用 admin 登录拿 token，调 `GET /system/user/list` 应 200；
- 用无 `system:user:list` 权限的账号调同接口应返回 403（`{"code":403,"msg":"无操作权限"}`）；
- 在菜单管理改某按钮的 api_url 后,不重启网关,新规则即时生效（验证 pub/sub 刷新）。
说明：无完整中间件环境则跳过，以 Step 1 全绿为完成判据。

- [ ] **Step 3: 推送分支（征得用户同意后）**

```bash
git push -u origin feature/dynamic-rbac-gateway
```

---

## 完成判据

- `mvn clean test`（JDK21）三模块全绿，新增 8 个单测通过，原有 15 个不回归；
- 网关 `SaTokenGatewayConfig` 不再含逐条硬编码 `SaRouter.match` 权限规则，改由 `ApiPermCache` 动态查表；
- 菜单变更后 auth-service 发布、网关 pub/sub 即时刷新；
- 行为与改造前等价（同一请求所需权限码不变）。
