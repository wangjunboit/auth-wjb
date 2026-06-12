package com.wjb.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendSmsCodeRequest {
    @NotBlank(message = "手机号不能为空")
    private String phone;
    @NotBlank(message = "请输入图形验证码")
    private String captchaKey;
    @NotBlank(message = "请输入图形验证码")
    private String captchaCode;
}
