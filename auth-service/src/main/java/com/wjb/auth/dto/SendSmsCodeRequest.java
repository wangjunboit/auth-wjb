package com.wjb.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class SendSmsCodeRequest {
    @NotBlank(message = "手机号不能为空")
    private String phone;
    @NotBlank(message = "请输入图形验证码")
    private String captchaKey;
    @NotBlank(message = "请输入图形验证码")
    private String captchaCode;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCaptchaKey() { return captchaKey; }
    public void setCaptchaKey(String captchaKey) { this.captchaKey = captchaKey; }
    public String getCaptchaCode() { return captchaCode; }
    public void setCaptchaCode(String captchaCode) { this.captchaCode = captchaCode; }
}
