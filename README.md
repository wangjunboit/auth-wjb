# auth-wjb

基于 **Sa-Token** 的用户权限登录系统(RBAC),微服务架构,前后端单仓库。

## 架构

```
浏览器 ──> auth-gateway(8080)─┬─ /            托管前端静态资源(Vue3 SPA,history 回退)
                              └─ /api/**      StripPrefix=1 后转发 ──> auth-service
                                              统一鉴权(登录态 + 动态 RBAC)
auth-service ──> MySQL 8 / Redis           业务:登录、用户/角色/菜单、OAuth 绑定
Nacos ──> 注册中心 + 配置中心(真实连接/密钥只放这里,不入 git)
```

- **技术栈**:Spring Boot 3.2.5 + Spring Cloud 2023.0.1 + Spring Cloud Alibaba 2023.0.1.0 + Sa-Token 1.37.0 + MyBatis-Plus 3.5.5 + MySQL 8 + Redis;前端 Vue 3 + Vite + Element Plus + Pinia + vue-router。
- **统一鉴权在网关**:登录态校验 + 权限判定都在 auth-gateway;权限规则来自 `sys_menu.api_url/api_method`,经 Redis 下发到网关内存缓存(pub/sub 刷新)。鉴权通过后向下游注入 `X-User-Id`。
- **登录方式**:账号密码(图形验证码)、短信验证码、邮箱验证码(短信/邮箱均为 MOCK,验证码打日志+开发态回传)、GitHub OAuth(真实)、微信 OAuth(mock)。手机/邮箱登录要求账号已存在;OAuth 须先绑定到已有账号。
- **安全**:登录失败 5 次锁定 10 分钟;改密/重置后强制重新登录。

## 目录结构

```
auth-common/    公共模块:Result、BaseEntity、常量、异常
auth-service/   业务服务(端口默认由 Nacos 配置)
auth-gateway/   网关(8080):统一鉴权 + 托管前端 + /api 路由
frontend/       Vue3 前端(npm run build 产物由 build-all.ps1 拷进网关 static)
sql/            建表脚本 schema.sql + 各里程碑 seed-*.sql + 注释脚本 alter-comments.sql
docs/           DEPLOY.md 部署手册;superpowers/ 下为各里程碑设计文档与实现计划
build.ps1       根级 Maven 包装(临时固定 JDK21,不污染全局)
build-all.ps1   一键构建:前端 build → 拷进网关 static → 打包后端
```

## 前置

- **JDK 21**(本机全局 `JAVA_HOME` 指向 JRE1.8,无 javac;务必用下文 `build.ps1`,它临时切到 JDK21)
- 可达的 **Nacos / MySQL 8 / Redis**
- Nacos 中已建好配置(命名空间 `test`,见 `docs/DEPLOY.md` §2)
- Node.js(构建前端)

> 真实数据库/Redis/OAuth 连接与密钥**只写在 Nacos**,不入库。本地另有 gitignore 的 `docs/nacos/` 存档。

## 快速开始(本地开发)

```powershell
# 1. 建库并执行建表
#    CREATE DATABASE auth_wjb DEFAULT CHARSET utf8mb4;  然后执行 sql/schema.sql + 需要的 seed-*.sql

# 2. 启动后端(两个服务,默认 namespace=test / profile=dev)
.\build.ps1 -pl auth-service -am spring-boot:run    # 业务服务
.\build.ps1 -pl auth-gateway -am spring-boot:run    # 网关 8080

# 3. 前端开发(Vite dev server,代理 /api → localhost:8080)
cd frontend; npm install; npm run dev
```

- 接口文档:`http://localhost:8080/doc.html`(Knife4j)
- 前端开发态访问 Vite 端口(默认 5173);生产态访问网关 `http://localhost:8080/` 即整站。
- 默认管理员 `admin`,初始密码见 seed 脚本(BCrypt 写入)。

## 构建与部署

```powershell
.\build-all.ps1     # 前端 build → 拷入网关 static → mvn package(网关 jar 内含前端)
```

产出 `auth-gateway` 与 `auth-service` 两个可执行 jar,**访问网关单端口即整站**。生产启动参数、Nacos 命名空间切换、GitHub 回调配置见 **[docs/DEPLOY.md](docs/DEPLOY.md)**。

> 构建说明:所有 `mvn` 操作走 `build.ps1`,它仅在命令内把 `JAVA_HOME` 临时指向 JDK21,不改系统环境。
