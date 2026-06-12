package com.wjb.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wjb.auth.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 第三方账号绑定 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_oauth_binding")
public class SysOauthBinding extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String provider;
    private String openId;
    private String unionId;

    public SysOauthBinding() {}

    public SysOauthBinding(Long userId, String provider, String openId) {
        this.userId = userId;
        this.provider = provider;
        this.openId = openId;
    }
}
