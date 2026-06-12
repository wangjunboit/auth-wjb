package com.wjb.auth.dto;

public class CaptchaResponse {
    private String captchaKey;
    private String imageBase64;

    public CaptchaResponse(String captchaKey, String imageBase64) {
        this.captchaKey = captchaKey;
        this.imageBase64 = imageBase64;
    }

    public String getCaptchaKey() { return captchaKey; }
    public void setCaptchaKey(String captchaKey) { this.captchaKey = captchaKey; }
    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}
