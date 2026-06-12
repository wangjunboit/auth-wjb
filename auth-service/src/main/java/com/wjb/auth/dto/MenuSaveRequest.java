package com.wjb.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuSaveRequest {
    private Long id;
    private Long parentId;
    @NotBlank(message = "菜单名不能为空")
    private String menuName;
    @NotBlank(message = "菜单类型不能为空")
    private String menuType;
    private String path;
    private String component;
    private String perm;
    private String icon;
    private Integer sort;
    private Integer visible;
}
