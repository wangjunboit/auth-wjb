package com.wjb.auth.sender;

/** 短信发送器(可插拔:日后接阿里云/腾讯云只换实现) */
public interface SmsSender {
    void send(String phone, String code);
}
