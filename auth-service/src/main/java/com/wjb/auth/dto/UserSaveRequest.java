package com.wjb.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 新增/修改用户请求;修改时 id 必填,新增时 password 必填 */
@Data
public class UserSaveRequest {
    private Long id;
    @NotBlank(message = "用户名不能为空")
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String email;
    private Integer status;
}
