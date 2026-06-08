package com.wjb.auth;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wjb.auth.common.exception.ServiceException;
import com.wjb.auth.mapper.SysMenuMapper;
import com.wjb.auth.mapper.SysRoleMenuMapper;
import com.wjb.auth.service.SysMenuService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SysMenuServiceTest {

    private final SysMenuMapper menuMapper = Mockito.mock(SysMenuMapper.class);
    private final SysRoleMenuMapper roleMenuMapper = Mockito.mock(SysRoleMenuMapper.class);
    private final SysMenuService service = new SysMenuService(menuMapper, roleMenuMapper);

    @Test
    void remove_hasChildren_shouldFail() {
        when(menuMapper.selectCount(any(Wrapper.class))).thenReturn(2L);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.remove(100L));
        assertEquals("存在子菜单,不可删除", ex.getMessage());
    }

    @Test
    void remove_boundToRole_shouldFail() {
        when(menuMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(roleMenuMapper.countByMenuId(101L)).thenReturn(1L);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.remove(101L));
        assertEquals("该菜单已被角色引用,不可删除", ex.getMessage());
    }
}
