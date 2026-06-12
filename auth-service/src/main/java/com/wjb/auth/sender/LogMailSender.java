package com.wjb.auth.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** mock 邮件发送:打日志(开发环境) */
@Component
public class LogMailSender implements MailSender {
    private static final Logger log = LoggerFactory.getLogger(LogMailSender.class);

    @Override
    public void send(String email, String code) {
        log.info("【模拟邮件】向邮箱 {} 发送验证码:{}", email, code);
    }
}
