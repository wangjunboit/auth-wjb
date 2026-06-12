package com.wjb.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** OAuth 回调结果:mode=login 时带 token;mode=bind 时 token 为 null */
@Data
@AllArgsConstructor
public class OAuthCallbackResponse {
    private String mode;
    private String token;
}
