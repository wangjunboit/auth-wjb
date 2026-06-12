package com.wjb.auth.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** mock 短信发送:打日志(开发环境) */
@Component
public class LogSmsSender implements SmsSender {
    private static final Logger log = LoggerFactory.getLogger(LogSmsSender.class);

    @Override
    public void send(String phone, String code) {
        log.info("【模拟短信】向手机号 {} 发送验证码:{}", phone, code);
    }
}
