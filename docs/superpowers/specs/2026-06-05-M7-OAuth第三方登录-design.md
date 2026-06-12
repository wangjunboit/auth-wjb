# M7:第三方 OAuth 登录(GitHub 真实 + 微信 mock) 设计文档

- **日期**:2026-06-05
- **状态**:已确认,待编写实现计划
- **关系**:在已完成系统(P1–M6)上增加第三方 OAuth 登录,落地原 spec 的"第三方登录"。

## 1. 目标与范围

增加第三方 OAuth 登录与账号绑定。**GitHub 走真实 OAuth,微信 mock**。

**关键决策(已与用户确认)**
- GitHub 真实:使用用户注册的 OAuth App,凭据存 Nacos。
- 微信 mock:Provider 抽象保留,微信用桩实现(固定 openId),便于演示不依赖外部。
- 绑定语义 **B**:GitHub/微信账号**必须先绑定到已存在的本系统用户**才能登录。首次第三方登录且未绑定 → 拒绝,提示先登录后绑定。
- 因此需要"账号绑定"入口(本期新增最小绑定页)。

**非目标(YAGNI)**
- 不做第三方首次登录自动建号(A 方案)。
- 不接真实微信(企业资质)。
- 不做 token 刷新/第三方资料同步。

## 2. 凭据(存 Nacos,不入 git)

`auth-service-dev.yml`(Nacos)新增:
```yaml
oauth:
  github:
    client-id: <GitHub OAuth App Client ID>
    client-secret: <GitHub OAuth App Client Secret>
    redirect-uri: http://localhost:5173/oauth/callback
```
- 代码用 `@Value("${oauth.github.client-id}")` 等读取。
- `docs/nacos/auth-service-dev.yml` 存档(gitignore)。GitHub callback URL 必须与注册一致:`http://localhost:5173/oauth/callback`。

## 3. Provider 抽象

```java
interface OAuthProvider {
    String provider();                 // "github" / "wechat"
    String authorizeUrl(String state); // 拼授权地址
    OAuthUser fetchUser(String code);  // code 换 token + 拉用户信息
}
record OAuthUser(String provider, String openId, String nickname, String avatar) {}
```
- `GitHubOAuthProvider`:真实调用
  - 授权:`https://github.com/login/oauth/authorize?client_id=..&redirect_uri=..&scope=read:user&state=..`
  - 换 token:`POST https://github.com/login/oauth/access_token`(Accept: application/json)
  - 用户:`GET https://api.github.com/user`(Authorization: Bearer token);openId = GitHub 用户 id。
  - 用 `RestClient`(Spring 6)调用。
- `WeChatOAuthProvider`(mock):`authorizeUrl` 直接返回前端回调地址并带固定假 code(如 `mock_wechat_code`);`fetchUser` 对该 code 返回固定 `openId=wx_mock_001`。

## 4. 两种模式与 state

- 生成授权地址时,随机 `state`(UUID)存 Redis,key `oauth:state:{state}`,TTL 5 分钟,value 为 JSON:`{ mode, userId }`。
  - 登录模式:`{ "mode":"login" }`
  - 绑定模式:`{ "mode":"bind", "userId":<当前用户id> }`
- 回调校验 state 存在,取出后**删除**(一次性);据 `mode` 走登录或绑定。

## 5. 接口设计

| 方法 路径 | 鉴权 | 说明 |
|---|---|---|
| GET `/auth/oauth/{provider}/url` | 免登录(网关白名单) | 登录模式:生成 state(mode=login)+ 返回授权 URL |
| POST `/auth/oauth/{provider}/callback` | 免登录(白名单) | 统一回调:校验 state,据 mode 登录或绑定 |
| GET `/system/oauth/{provider}/bind-url` | 需登录 | 绑定模式:生成 state(mode=bind,userId 来自 X-User-Id)+ 返回授权 URL |
| GET `/system/oauth/bindings` | 需登录 | 当前用户已绑定列表(provider + 绑定时间) |
| DELETE `/system/oauth/{provider}` | 需登录 | 解绑当前用户的某平台绑定 |

- `/auth/oauth/**` 已在网关白名单。
- `/system/oauth/**` 走网关登录校验(注入 X-User-Id),不挂权限码(任意登录用户可管理自己的绑定)。

## 6. 核心流程

### 6.1 登录模式
```
前端登录页点「GitHub 登录」
  → GET /auth/oauth/github/url → 返回授权 URL(state mode=login)
  → 浏览器跳 GitHub 授权 → 回调 http://localhost:5173/oauth/callback?code&state
  → 前端回调页 POST /auth/oauth/github/callback { code, state }
  → 后端:校验+删 state(mode=login)→ provider.fetchUser(code) 得 openId
       → 查 sys_oauth_binding(provider, open_id):
            命中 → doLogin(关联 user) → 返回我们的 token
            未命中 → 抛 "该 GitHub 未绑定,请先登录后在『账号绑定』绑定"
```

### 6.2 绑定模式
```
已登录用户在「账号绑定」点「绑定 GitHub」
  → GET /system/oauth/github/bind-url(带 token,网关注入 X-User-Id)
       → 生成 state(mode=bind, userId)+ 返回授权 URL
  → 跳 GitHub 授权 → 回调 /oauth/callback → 前端 POST /auth/oauth/github/callback { code, state }
  → 后端:state mode=bind → fetchUser → openId
       → 校验该 (provider, openId) 未被他人绑定(uk_provider_openid 唯一)
       → 校验当前 userId 未绑定过该 provider(一个用户一个平台一条)
       → 写 sys_oauth_binding(user_id, provider, open_id)
  → 前端提示"绑定成功"
```

### 6.3 解绑
```
DELETE /system/oauth/github → 删除当前用户在该 provider 的绑定(逻辑删除)
```

## 7. 前端改造

1. **登录页**:三 Tab 下方加"第三方登录"区:GitHub、微信 图标按钮。点击 → `GET .../url` → `window.location.href = url`。
2. **回调页** `OAuthCallback.vue`,路由 `/oauth/callback`(免登录守卫):
   - 从 query 取 `code`、`state` → POST `/auth/oauth/{provider}/callback`。
   - provider 如何确定:授权 URL 由后端拼,GitHub 回调不带 provider;故**前端发起时把 provider 存 `sessionStorage`**,回调页读出来决定调哪个 provider 的 callback。
   - 登录模式成功(返回 token)→ 存 token → `loadUserData` → 跳 `/`;绑定模式成功 → 提示 → 跳 `/system/binding`;失败 → 提示 → 跳 `/login`。
3. **账号绑定页** `Binding.vue`,路由 `/system/binding`:
   - 显示 GitHub/微信 绑定状态(查 `/system/oauth/bindings`)。
   - 未绑定 → "绑定"按钮(`GET .../bind-url` → 跳转);已绑定 → "解绑"按钮(`DELETE`)。
   - 侧边栏可经菜单进入(种子加一个"账号绑定"菜单),或直接 URL 访问。
4. 前端 API:`oauthUrlApi`、`oauthCallbackApi`、`oauthBindUrlApi`、`oauthBindingsApi`、`oauthUnbindApi`。

## 8. 错误处理

统一 `Result` + 全局异常。state 失效、未绑定、已被他人绑定、重复绑定等用 `ServiceException` 返回中文提示(`code:500`)。回调失败前端统一提示并回登录页。

## 9. 测试策略

- **后端单测**:state 生成/解析(JSON)、绑定查重逻辑(mock Redis/Mapper)。
- **微信 mock 全自动联调**:走 mock provider,脚本跑通"登录态绑定 → 退出 → 微信登录成功 → 解绑"全流程(不依赖外部)。
- **GitHub 真实**:最后授权需**用户本人在浏览器点 GitHub 授权**(Claude 无法替登 GitHub)。流程:起服务 → 引导用户在登录页点「绑定 GitHub」(先用 admin 登录)完成绑定 → 退出 → 点「GitHub 登录」验证登录成功。后端逻辑先用微信 mock + 脚本充分验证。

## 10. 风险/说明

- `sys_oauth_binding` 表(P1 已建)字段够用。
- GitHub callback URL 必须与 OAuth App 注册值一致(`http://localhost:5173/oauth/callback`)。
- Client Secret 仅存 Nacos;因曾在对话中出现,建议项目完成后在 GitHub 端 Regenerate。
- 微信为 mock,生产接入真实微信需企业资质,届时只替换 `WeChatOAuthProvider` 实现。

## 11. 里程碑拆分(交给 writing-plans)

单里程碑:后端(Provider 抽象 + GitHub 真实 + 微信 mock + state + 回调/绑定/解绑接口 + Nacos 凭据)+ 前端(登录页第三方按钮 + 回调页 + 绑定页 + 菜单种子)+ 联调验证(微信 mock 脚本 + GitHub 用户手动)。
