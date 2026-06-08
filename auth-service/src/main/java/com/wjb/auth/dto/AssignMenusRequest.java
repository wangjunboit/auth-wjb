package com.wjb.auth.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class AssignMenusRequest {
    @NotNull(message = "角色id不能为空")
    private Long roleId;
    private List<Long> menuIds;

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public List<Long> getMenuIds() { return menuIds; }
    public void setMenuIds(List<Long> menuIds) { this.menuIds = menuIds; }
}
