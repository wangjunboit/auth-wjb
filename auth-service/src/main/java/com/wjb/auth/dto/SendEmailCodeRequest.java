package com.wjb.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class SendEmailCodeRequest {
    @NotBlank(message = "邮箱不能为空")
    private String email;
    @NotBlank(message = "请输入图形验证码")
    private String captchaKey;
    @NotBlank(message = "请输入图形验证码")
    private String captchaCode;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCaptchaKey() { return captchaKey; }
    public void setCaptchaKey(String captchaKey) { this.captchaKey = captchaKey; }
    public String getCaptchaCode() { return captchaCode; }
    public void setCaptchaCode(String captchaCode) { this.captchaCode = captchaCode; }
}
