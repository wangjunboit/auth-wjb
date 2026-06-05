package com.wjb.auth.common;

import com.wjb.auth.common.constant.SecurityConstants;
import com.wjb.auth.common.exception.ServiceException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** 从网关注入的 X-User-Id 请求头读取当前登录用户 id */
public final class UserContext {

    private UserContext() {}

    /** 取当前用户 id;取不到抛业务异常(理论上网关已保证) */
    public static Long getUserId() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String uid = attrs.getRequest().getHeader(SecurityConstants.HEADER_USER_ID);
                if (uid != null && !uid.isBlank()) {
                    return Long.valueOf(uid);
                }
            }
        } catch (Exception ignored) {
        }
        throw new ServiceException("无法识别当前用户");
    }
}
