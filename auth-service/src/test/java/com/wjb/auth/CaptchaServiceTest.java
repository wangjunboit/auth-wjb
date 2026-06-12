package com.wjb.auth;

import com.wjb.auth.common.exception.ServiceException;
import com.wjb.auth.service.CaptchaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CaptchaServiceTest {

    private final StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> ops = Mockito.mock(ValueOperations.class);
    private final CaptchaService service = new CaptchaService(redis);

    @Test
    void verify_expired_shouldFail() {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.get(anyString())).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.verify("k", "8"));
        assertEquals("图形验证码已过期", ex.getMessage());
    }

    @Test
    void verify_wrong_shouldFail() {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.get(anyString())).thenReturn("8");
        ServiceException ex = assertThrows(ServiceException.class, () -> service.verify("k", "9"));
        assertEquals("图形验证码错误", ex.getMessage());
    }

    @Test
    void verify_blank_shouldFail() {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.verify("", ""));
        assertEquals("请输入图形验证码", ex.getMessage());
    }
}
