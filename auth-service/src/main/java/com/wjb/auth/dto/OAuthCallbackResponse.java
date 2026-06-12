package com.wjb.auth.dto;

/** OAuth 回调结果:mode=login 时带 token;mode=bind 时 token 为 null */
public class OAuthCallbackResponse {
    private String mode;
    private String token;

    public OAuthCallbackResponse(String mode, String token) {
        this.mode = mode;
        this.token = token;
    }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
