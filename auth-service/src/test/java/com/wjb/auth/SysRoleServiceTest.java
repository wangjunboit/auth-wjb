package com.wjb.auth;

import com.wjb.auth.common.exception.ServiceException;
import com.wjb.auth.mapper.SysRoleMapper;
import com.wjb.auth.mapper.SysRoleMenuMapper;
import com.wjb.auth.mapper.SysUserRoleMapper;
import com.wjb.auth.service.SysRoleService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class SysRoleServiceTest {

    private final SysRoleMapper roleMapper = Mockito.mock(SysRoleMapper.class);
    private final SysRoleMenuMapper roleMenuMapper = Mockito.mock(SysRoleMenuMapper.class);
    private final SysUserRoleMapper userRoleMapper = Mockito.mock(SysUserRoleMapper.class);
    private final SysRoleService service = new SysRoleService(roleMapper, roleMenuMapper, userRoleMapper);

    @Test
    void remove_adminRole_shouldFail() {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.remove(1L));
        assertEquals("超级管理员角色不可删除", ex.getMessage());
    }

    @Test
    void remove_roleBoundToUsers_shouldFail() {
        when(userRoleMapper.countByRoleId(2L)).thenReturn(3L);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.remove(2L));
        assertEquals("该角色已分配给用户,不可删除", ex.getMessage());
    }

    @Test
    void assignMenus_roleNotExists_shouldFail() {
        when(roleMapper.selectById(99L)).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.assignMenus(99L, java.util.List.of(1L, 2L)));
        assertEquals("角色不存在", ex.getMessage());
    }
}
