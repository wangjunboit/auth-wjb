package com.wjb.auth.gateway.security;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 网关鉴权数据源:从登录时写入会话的权限快照读取(不连数据库) */
@Component
public class StpInterfaceImpl implements StpInterface {

    /** 与 auth-service AuthService.SESSION_PERMS 约定一致 */
    private static final String SESSION_PERMS = "perms";

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getPermissionList(Object loginId, String loginType) {
        SaSession session = StpUtil.getSessionByLoginId(loginId, false);
        if (session == null) {
            return Collections.emptyList();
        }
        Object perms = session.get(SESSION_PERMS);
        if (perms instanceof List) {
            return new ArrayList<>((List<String>) perms);
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 角色校验本期不在网关使用,返回空
        return Collections.emptyList();
    }
}
