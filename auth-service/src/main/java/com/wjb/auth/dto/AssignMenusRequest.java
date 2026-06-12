package com.wjb.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssignMenusRequest {
    @NotNull(message = "角色id不能为空")
    private Long roleId;
    private List<Long> menuIds;
}
