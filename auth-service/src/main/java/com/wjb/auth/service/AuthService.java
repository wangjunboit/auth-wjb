package com.wjb.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wjb.auth.common.UserContext;
import com.wjb.auth.common.exception.ServiceException;
import com.wjb.auth.dto.LoginRequest;
import com.wjb.auth.dto.LoginResponse;
import com.wjb.auth.dto.UserInfoResponse;
import com.wjb.auth.entity.SysUser;
import com.wjb.auth.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    /** 存入 Sa-Token 会话的权限码 key,网关据此读取做权限校验 */
    public static final String SESSION_PERMS = "perms";

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final com.wjb.auth.mapper.SysMenuMapper menuMapper;

    public AuthService(SysUserMapper userMapper, PasswordEncoder passwordEncoder,
                       com.wjb.auth.mapper.SysMenuMapper menuMapper) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.menuMapper = menuMapper;
    }

    /** 账号密码登录:校验通过后登录 + 把权限码快照写入共享会话(供网关读取) */
    public LoginResponse login(LoginRequest req) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername()));
        if (user == null || user.getPassword() == null
                || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new ServiceException("账号已被禁用");
        }
        StpUtil.login(user.getId());
        List<String> perms = userMapper.selectPermsByUserId(user.getId());
        StpUtil.getSession().set(SESSION_PERMS, perms);
        return new LoginResponse(StpUtil.getTokenValue(), user.getId());
    }

    /** 退出登录(网关已注入 X-User-Id;此处按当前用户登出) */
    public void logout() {
        StpUtil.logout(UserContext.getUserId());
    }

    /** 当前登录用户信息 + 角色 + 权限码(用户身份来自网关注入的 X-User-Id) */
    public UserInfoResponse currentUserInfo() {
        Long userId = UserContext.getUserId();
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        UserInfoResponse resp = new UserInfoResponse();
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setRoles(userMapper.selectRoleKeysByUserId(userId));
        resp.setPerms(userMapper.selectPermsByUserId(userId));
        return resp;
    }

    /** 当前用户可见菜单树(M/C 类型,按 parent_id 组装) */
    public java.util.List<com.wjb.auth.dto.MenuTreeNode> currentUserMenus() {
        Long userId = UserContext.getUserId();
        java.util.List<com.wjb.auth.entity.SysMenu> menus = menuMapper.selectVisibleMenusByUserId(userId);
        java.util.List<com.wjb.auth.dto.MenuTreeNode> nodes = new java.util.ArrayList<>();
        for (com.wjb.auth.entity.SysMenu m : menus) {
            nodes.add(com.wjb.auth.dto.MenuTreeNode.from(m));
        }
        java.util.Map<Long, com.wjb.auth.dto.MenuTreeNode> byId = new java.util.HashMap<>();
        for (com.wjb.auth.dto.MenuTreeNode n : nodes) {
            byId.put(n.getId(), n);
        }
        java.util.List<com.wjb.auth.dto.MenuTreeNode> roots = new java.util.ArrayList<>();
        for (com.wjb.auth.dto.MenuTreeNode n : nodes) {
            if (n.getParentId() == null || n.getParentId() == 0L) {
                roots.add(n);
            } else {
                com.wjb.auth.dto.MenuTreeNode parent = byId.get(n.getParentId());
                if (parent != null) {
                    parent.getChildren().add(n);
                } else {
                    roots.add(n);
                }
            }
        }
        return roots;
    }
}
