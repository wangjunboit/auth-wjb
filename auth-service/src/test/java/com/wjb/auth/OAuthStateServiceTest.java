package com.wjb.auth;

import com.wjb.auth.common.exception.ServiceException;
import com.wjb.auth.service.OAuthStateService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class OAuthStateServiceTest {

    private final StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> ops = Mockito.mock(ValueOperations.class);
    private final OAuthStateService service = new OAuthStateService(redis);

    @Test
    void consume_expired_shouldFail() {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.get(anyString())).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.consume("s"));
        assertEquals("授权已过期或非法请求,请重试", ex.getMessage());
    }

    @Test
    void consume_login_ok() {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.get(anyString())).thenReturn("login");
        OAuthStateService.StateData d = service.consume("s");
        assertEquals("login", d.mode());
        assertNull(d.userId());
    }

    @Test
    void consume_bind_ok() {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.get(anyString())).thenReturn("bind:42");
        OAuthStateService.StateData d = service.consume("s");
        assertEquals("bind", d.mode());
        assertEquals(42L, d.userId());
    }
}
