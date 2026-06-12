package com.wjb.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wjb.auth.common.entity.BaseEntity;

/** 第三方账号绑定 */
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getOpenId() { return openId; }
    public void setOpenId(String openId) { this.openId = openId; }
    public String getUnionId() { return unionId; }
    public void setUnionId(String unionId) { this.unionId = unionId; }
}
