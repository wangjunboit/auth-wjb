package com.wjb.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailLoginRequest {
    @NotBlank(message = "邮箱不能为空")
    private String email;
    @NotBlank(message = "请输入验证码")
    private String code;
}
