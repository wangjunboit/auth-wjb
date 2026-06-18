package com.wjb.auth.common.rbac;

/**
 * 动态 RBAC 的一条「接口→权限码」映射,网关与 auth-service 间的 JSON 契约。
 * method: HTTP 方法(GET/POST/...),null 或 "*" 表示任意;url: Ant 风格路径模式;perm: 所需权限码。
 */
public record ApiPermDef(String method, String url, String perm) {
}
