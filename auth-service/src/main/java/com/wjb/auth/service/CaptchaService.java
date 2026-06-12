package com.wjb.auth.service;

import com.wf.captcha.SpecCaptcha;
import com.wjb.auth.common.exception.ServiceException;
import com.wjb.auth.dto.CaptchaResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.UUID;

/** 图形验证码:生成(算术)与校验,答案存 Redis 2 分钟 */
@Service
public class CaptchaService {

    private static final String PREFIX = "captcha:";
    private static final Duration TTL = Duration.ofMinutes(2);

    private final StringRedisTemplate redis;

    public CaptchaService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** 生成图形验证码,返回 key + base64 图(字符型,4 位;JDK17+ 无 Nashorn,故不用算术型) */
    public CaptchaResponse generate() {
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        String answer = captcha.text(); // 4 位字符
        String key = UUID.randomUUID().toString().replace("-", "");
        redis.opsForValue().set(PREFIX + key, answer, TTL);
        return new CaptchaResponse(key, captcha.toBase64());
    }

    /** 校验图形验证码:比对后删除(一次性);失败抛异常 */
    public void verify(String captchaKey, String captchaCode) {
        if (!StringUtils.hasText(captchaKey) || !StringUtils.hasText(captchaCode)) {
            throw new ServiceException("请输入图形验证码");
        }
        String redisKey = PREFIX + captchaKey;
        String answer = redis.opsForValue().get(redisKey);
        if (answer == null) {
            throw new ServiceException("图形验证码已过期");
        }
        redis.delete(redisKey);
        if (!answer.equalsIgnoreCase(captchaCode.trim())) {
            throw new ServiceException("图形验证码错误");
        }
    }
}
