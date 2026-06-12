package com.wjb.auth.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** mock 邮件发送:打日志(开发环境) */
@Slf4j
@Component
public class LogMailSender implements MailSender {

    @Override
    public void send(String email, String code) {
        log.info("【模拟邮件】向邮箱 {} 发送验证码:{}", email, code);
    }
}
