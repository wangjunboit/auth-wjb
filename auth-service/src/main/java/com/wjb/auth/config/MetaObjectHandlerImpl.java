package com.wjb.auth.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.wjb.auth.common.constant.SecurityConstants;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/** 插入/更新自动填充审计字段;当前用户从网关注入的 X-User-Id 读取 */
@Component
public class MetaObjectHandlerImpl implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long userId = currentUserId();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createBy", Long.class, userId);
        this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", Long.class, currentUserId());
    }

    /** 从当前请求头 X-User-Id 取;无则返回 null */
    private Long currentUserId() {
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
        return null;
    }
}
