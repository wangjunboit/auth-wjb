package com.wjb.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendEmailCodeRequest {
    @NotBlank(message = "邮箱不能为空")
    private String email;
    @NotBlank(message = "请输入图形验证码")
    private String captchaKey;
    @NotBlank(message = "请输入图形验证码")
    private String captchaCode;
}
