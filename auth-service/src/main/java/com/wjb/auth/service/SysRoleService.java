package com.wjb.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wjb.auth.common.exception.ServiceException;
import com.wjb.auth.dto.PageResult;
import com.wjb.auth.dto.RoleSaveRequest;
import com.wjb.auth.entity.SysRole;
import com.wjb.auth.entity.SysRoleMenu;
import com.wjb.auth.mapper.SysRoleMapper;
import com.wjb.auth.mapper.SysRoleMenuMapper;
import com.wjb.auth.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;

    public PageResult<SysRole> page(long pageNo, long pageSize, String roleName) {
        Page<SysRole> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName)
                .orderByDesc(SysRole::getId);
        Page<SysRole> result = roleMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    public SysRole getById(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new ServiceException("角色不存在");
        }
        return role;
    }

    /** 角色已绑定的菜单 id 列表 */
    public List<Long> getMenuIds(Long roleId) {
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }

    public void add(RoleSaveRequest req) {
        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, req.getRoleKey()));
        if (count != null && count > 0) {
            throw new ServiceException("角色标识已存在");
        }
        SysRole role = new SysRole();
        role.setRoleName(req.getRoleName());
        role.setRoleKey(req.getRoleKey());
        role.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        role.setRemark(req.getRemark());
        roleMapper.insert(role);
    }

    public void update(RoleSaveRequest req) {
        if (req.getId() == null) {
            throw new ServiceException("角色id不能为空");
        }
        SysRole role = roleMapper.selectById(req.getId());
        if (role == null) {
            throw new ServiceException("角色不存在");
        }
        role.setRoleName(req.getRoleName());
        role.setRoleKey(req.getRoleKey());
        if (req.getStatus() != null) {
            role.setStatus(req.getStatus());
        }
        role.setRemark(req.getRemark());
        roleMapper.updateById(role);
    }

    public void remove(Long id) {
        if (id == 1L) {
            throw new ServiceException("超级管理员角色不可删除");
        }
        long bound = userRoleMapper.countByRoleId(id);
        if (bound > 0) {
            throw new ServiceException("该角色已分配给用户,不可删除");
        }
        roleMapper.deleteById(id);
    }

    /** 给角色分配菜单:删旧 + 插新 */
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        if (roleMapper.selectById(roleId) == null) {
            throw new ServiceException("角色不存在");
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds != null) {
            for (Long menuId : menuIds) {
                roleMenuMapper.insert(new SysRoleMenu(roleId, menuId));
            }
        }
    }
}
