package com.wjb.auth.common.constant;

/** 安全相关常量:网关与下游服务的契约 */
public final class SecurityConstants {

    private SecurityConstants() {}

    /** 网关鉴权通过后注入、下游读取的当前用户 id 请求头(M2 启用) */
    public static final String HEADER_USER_ID = "X-User-Id";

    /** 动态RBAC:网关读取的 URL→权限码 映射(JSON 数组) 的 Redis key */
    public static final String API_PERM_KEY = "rbac:api-perms";

    /** 动态RBAC:映射变更通知的 pub/sub 频道,网关订阅后重载 */
    public static final String API_PERM_REFRESH_CHANNEL = "rbac:api-perms:refresh";
}
