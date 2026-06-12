package com.wjb.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssignRolesRequest {
    @NotNull(message = "用户id不能为空")
    private Long userId;
    private List<Long> roleIds;
}
