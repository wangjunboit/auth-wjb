package com.wjb.auth.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class AssignRolesRequest {
    @NotNull(message = "用户id不能为空")
    private Long userId;
    private List<Long> roleIds;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
}
