package com.wjb.auth.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** mock 短信发送:打日志(开发环境) */
@Slf4j
@Component
public class LogSmsSender implements SmsSender {

    @Override
    public void send(String phone, String code) {
        log.info("【模拟短信】向手机号 {} 发送验证码:{}", phone, code);
    }
}
