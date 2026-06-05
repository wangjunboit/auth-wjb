package com.wjb.auth;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wjb.auth.common.exception.ServiceException;
import com.wjb.auth.dto.UserSaveRequest;
import com.wjb.auth.mapper.SysUserMapper;
import com.wjb.auth.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SysUserServiceTest {

    private final SysUserMapper userMapper = Mockito.mock(SysUserMapper.class);
    private final PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
    private final SysUserService service = new SysUserService(userMapper, encoder);

    @Test
    void add_duplicateUsername_shouldFail() {
        when(userMapper.selectCount(any(Wrapper.class))).thenReturn(1L);
        UserSaveRequest req = new UserSaveRequest();
        req.setUsername("admin");
        req.setPassword("123456");
        ServiceException ex = assertThrows(ServiceException.class, () -> service.add(req));
        assertEquals("用户名已存在", ex.getMessage());
    }

    @Test
    void add_missingPassword_shouldFail() {
        when(userMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        UserSaveRequest req = new UserSaveRequest();
        req.setUsername("newuser");
        ServiceException ex = assertThrows(ServiceException.class, () -> service.add(req));
        assertEquals("新增用户必须设置密码", ex.getMessage());
    }

    @Test
    void remove_superAdmin_shouldFail() {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.remove(1L));
        assertEquals("超级管理员不可删除", ex.getMessage());
    }
}
