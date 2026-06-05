# auth-wjb

基于 Sa-Token 的用户权限登录系统(RBAC)。前后端分目录单仓库。

- backend/  Spring Boot 3 + Sa-Token + MyBatis-Plus(端口 8080)
- frontend/ Vue 3 + Element Plus(后续里程碑)
- sql/      建表脚本
- docs/     设计文档与实现计划

## 启动(后端)
1. 建库:`CREATE DATABASE auth_wjb DEFAULT CHARSET utf8mb4;` 并执行 `sql/schema.sql`
2. 准备可用的 Redis
3. 改 `backend/src/main/resources/application.yml` 中数据库/Redis 连接
4. `cd backend && mvn spring-boot:run`
5. 文档:http://localhost:8080/doc.html
