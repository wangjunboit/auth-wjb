package com.wjb.auth.common.constant;

/** 安全相关常量:网关与下游服务的契约 */
public final class SecurityConstants {

    private SecurityConstants() {}

    /** 网关鉴权通过后注入、下游读取的当前用户 id 请求头(M2 启用) */
    public static final String HEADER_USER_ID = "X-User-Id";
}
