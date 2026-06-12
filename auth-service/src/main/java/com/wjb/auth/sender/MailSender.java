package com.wjb.auth.sender;

/** 邮件发送器(可插拔:日后接 SMTP 只换实现) */
public interface MailSender {
    void send(String email, String code);
}
