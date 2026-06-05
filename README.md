# auth-wjb

基于 Sa-Token 的用户权限登录系统(RBAC)。前后端分目录单仓库。

- backend/  Spring Boot 3 + Sa-Token + MyBatis-Plus(端口 8080)
- frontend/ Vue 3 + Element Plus(后续里程碑)
- sql/      建表脚本
- docs/     设计文档与实现计划

## 启动(后端)
1. 建库:`CREATE DATABASE auth_wjb DEFAULT CHARSET utf8mb4;` 并执行 `sql/schema.sql`
2. 准备可用的 Redis
3. **配置真实连接**:在 `backend/src/main/resources/` 下新建 `application-local.yml`(已被 .gitignore 排除,不会入库),填入真实数据库/Redis 地址账号密码,覆盖 `application.yml` 里的占位值。示例:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://<host>:<port>/auth_wjb?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
       username: <user>
       password: "<password>"
     data:
       redis:
         host: <host>
         port: 6379
   ```
   `application.yml` 默认 `spring.profiles.active: local`,会自动加载该文件。
4. 启动(二选一):
   - 用项目自带包装脚本(推荐,自动用 JDK 21,不改系统环境):
     `cd backend; .\build.ps1 spring-boot:run`(PowerShell)或 `backend\build.cmd spring-boot:run`(CMD)
   - 或直接 `cd backend && mvn spring-boot:run`(需自行保证 JAVA_HOME 指向 JDK 17+)
5. 文档:http://localhost:8080/doc.html

> 说明:本机系统 `JAVA_HOME` 指向旧 JRE,故提供 `backend/build.ps1` / `backend/build.cmd`,仅在本项目命令内临时把 `JAVA_HOME` 指向 JDK 21,不污染全局。所有 `mvn` 操作建议走该脚本。
