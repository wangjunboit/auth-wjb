# 部署手册(auth-wjb)

单端口部署:**网关托管前端 + 后端走 /api**。生产起 auth-service 与 auth-gateway 两个 jar,访问网关端口即整站。

## 1. 前置
- JDK 21
- 可达的 Nacos / MySQL / Redis
- 已在 Nacos 创建配置(见 §2)

## 2. Nacos 配置(生产用 prod 命名空间)
在 Nacos 新建命名空间 `prod`,在其下建以下配置(Group `DEFAULT_GROUP`,YAML):
- `auth-common.yml`:Redis、Sa-Token(同 dev 模板)
- `auth-service-prod.yml`:MySQL、`oauth.github.*`、`auth.dev-return-code: false`(生产关闭验证码回传)
- `auth-gateway-prod.yml`:网关路由
  ```yaml
  spring:
    cloud:
      gateway:
        discovery:
          locator:
            enabled: false
        routes:
          - id: auth-service
            uri: lb://auth-service
            predicates:
              - Path=/api/**
            filters:
              - StripPrefix=1
  ```
> 真实连接/密钥只写在 Nacos,不入 git。

## 3. 构建
仓库根执行:
```
.\build-all.ps1
```
产出:`auth-gateway/target/auth-gateway-0.0.1-SNAPSHOT.jar`(内含前端)、`auth-service/target/auth-service-0.0.1-SNAPSHOT.jar`。

## 4. 启动(生产)
```
java -DNACOS_NAMESPACE=prod -DSPRING_PROFILES_ACTIVE=prod -jar auth-service-0.0.1-SNAPSHOT.jar
java -DNACOS_NAMESPACE=prod -DSPRING_PROFILES_ACTIVE=prod -jar auth-gateway-0.0.1-SNAPSHOT.jar
```
- 默认(不传参)= `test/dev`,本机开发用。
- 访问 `http://<网关host>:8080/` 即整站;后端 API 经 `/api/**`(网关 StripPrefix 转发到 auth-service)。

## 5. GitHub OAuth 生产回调
在 GitHub OAuth App 的 Authorization callback URL **追加**生产地址:`http://<网关host>:8080/oauth/callback`(保留 dev 的 `http://localhost:5173/oauth/callback`)。

## 6. 路径约定(重要)
- 前端 axios `baseURL=/api`;经网关 `/api/**` → StripPrefix=1 → auth-service 的 `/auth/**`、`/system/**`。
- 网关鉴权只拦 `/api/**`,判定时剥 `/api` 前缀后复用白名单与动态RBAC 映射(`sys_menu.api_url`)。
- 前端 history 路由(`/system/user` 等)由网关 `SpaWebFilter` 回退到 `index.html`。

## 7. 附录:Nginx 反代(可选,前后端分离时)
若改用 Nginx 托管前端、网关只做 API:
```nginx
server {
  listen 80;
  location / { root /var/www/auth-wjb; try_files $uri /index.html; }
  location /api/ { proxy_pass http://127.0.0.1:8080; }
}
```
