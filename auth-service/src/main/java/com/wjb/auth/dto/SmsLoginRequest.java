package com.wjb.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class SmsLoginRequest {
    @NotBlank(message = "手机号不能为空")
    private String phone;
    @NotBlank(message = "请输入验证码")
    private String code;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
