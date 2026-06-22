# auth-wjb — 项目约定

基于 Sa-Token 的 RBAC 登录系统,微服务 + 单仓库。项目概览见 [README.md](README.md),部署见 [docs/DEPLOY.md](docs/DEPLOY.md)。

## 红线(违反会出事)

- **真实连接/密钥绝不入 git**:MySQL/Redis/OAuth 凭据只放 Nacos;本地存档在 gitignore 的 `docs/nacos/`、`application-local.yml`、`application-seed.yml`。提交前确认无明文凭据(历史上多次被安全分类器拦)。
- **构建必须走 `build.ps1`**:本机全局 `JAVA_HOME` 指向 JRE1.8(无 javac),裸跑 `mvn` 会报 "No compiler is provided"。`build.ps1` 临时切到 JDK21,不污染全局。
- **凭据善后**:GitHub OAuth Client Secret 曾在对话中出现明文,收尾时应去 GitHub 后台 regenerate。

## 速查

```powershell
.\build.ps1 <maven 参数>      # 例:.\build.ps1 -pl auth-service -am test
.\build-all.ps1               # 一键:前端 build → 拷网关 static → 打包(网关 jar 内含前端)
```

- 测试间歇失败(NoSuchBean/NoClassDefFound)是 Windows 文件锁导致 `clean` 删不净 `target`,**非代码问题**:先 `rm -rf */target` 再 `mvn test`(不带 clean)。
- `HealthControllerTest` 是连 Nacos/MySQL/Redis 的 `@SpringBootTest`,需中间件可达,单跑约 25s。

## 架构约束

- **统一鉴权在网关**:登录态 + 权限判定都在 auth-gateway,不要把鉴权逻辑搬回 auth-service。
- **路由**:前端 axios `baseURL=/api` → 网关 `/api/**` → `StripPrefix=1` → auth-service 的 `/auth/**`、`/system/**`。新增后端接口时,网关白名单(`SaTokenGatewayConfig`)按**剥前缀后的逻辑路径**匹配。
- **动态 RBAC**:权限规则来自 `sys_menu.api_url/api_method`,经 Redis pub/sub 下发到网关内存缓存。新增受控接口 → 在 `sys_menu` 配 `api_url/api_method` 并绑定到角色,**不要在网关硬编码**。
- **前端 history 路由**由网关 `SpaWebFilter` 回退 `index.html`;静态资源(带点的路径)不回退。
- **公共代码进 auth-common**(Result/BaseEntity/常量/异常),被 service 与 gateway 共用。
- 实体继承 `BaseEntity`(create_time/update_time/create_by/update_by/deleted,审计字段由 MetaObjectHandler 自动填充,`@TableLogic` 逻辑删除)。

## 深入文档

| 主题 | 位置 |
|---|---|
| 部署(单端口、Nacos 参数化、生产启动) | [docs/DEPLOY.md](docs/DEPLOY.md) |
| 各里程碑设计文档 | `docs/superpowers/specs/*-design.md` |
| 各里程碑实现计划 | `docs/superpowers/plans/*.md` |
| 建表 + 种子数据 | `sql/schema.sql`、`sql/seed-*.sql` |
