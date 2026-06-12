package com.wjb.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class EmailLoginRequest {
    @NotBlank(message = "邮箱不能为空")
    private String email;
    @NotBlank(message = "请输入验证码")
    private String code;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
