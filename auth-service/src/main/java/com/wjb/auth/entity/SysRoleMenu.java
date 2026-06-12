package com.wjb.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wjb.auth.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 角色-菜单关联 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role_menu")
public class SysRoleMenu extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roleId;
    private Long menuId;

    public SysRoleMenu() {}

    public SysRoleMenu(Long roleId, Long menuId) {
        this.roleId = roleId;
        this.menuId = menuId;
    }
}
