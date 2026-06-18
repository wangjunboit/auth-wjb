package com.wjb.auth;

import com.wjb.auth.common.exception.ServiceException;
import com.wjb.auth.mapper.SysUserMapper;
import com.wjb.auth.mapper.SysUserRoleMapper;
import com.wjb.auth.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ResetPasswordTest {

    private final SysUserMapper userMapper = Mockito.mock(SysUserMapper.class);
    private final PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
    private final SysUserRoleMapper userRoleMapper = Mockito.mock(SysUserRoleMapper.class);
    private final SysUserService service = new SysUserService(userMapper, encoder, userRoleMapper);

    @Test
    void resetPassword_userNotFound_shouldFail() {
        when(userMapper.selectById(99L)).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.resetPassword(99L));
        assertEquals("用户不存在", ex.getMessage());
    }
}
