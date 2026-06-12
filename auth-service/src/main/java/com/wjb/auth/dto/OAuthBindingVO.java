package com.wjb.auth.dto;

public class OAuthBindingVO {
    private String provider;
    private String openId;

    public OAuthBindingVO(String provider, String openId) {
        this.provider = provider;
        this.openId = openId;
    }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getOpenId() { return openId; }
    public void setOpenId(String openId) { this.openId = openId; }
}
