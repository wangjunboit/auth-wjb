package com.wjb.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wjb.auth.common.exception.ServiceException;
import com.wjb.auth.dto.PageResult;
import com.wjb.auth.dto.UserSaveRequest;
import com.wjb.auth.entity.SysUser;
import com.wjb.auth.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysUserService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public SysUserService(SysUserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /** 分页查询(可按用户名模糊) */
    public PageResult<SysUser> page(long pageNo, long pageSize, String username) {
        Page<SysUser> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.hasText(username), SysUser::getUsername, username)
                .orderByDesc(SysUser::getId);
        Page<SysUser> result = userMapper.selectPage(page, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    /** 详情 */
    public SysUser getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    /** 新增 */
    public void add(UserSaveRequest req) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername()));
        if (count != null && count > 0) {
            throw new ServiceException("用户名已存在");
        }
        if (!StringUtils.hasText(req.getPassword())) {
            throw new ServiceException("新增用户必须设置密码");
        }
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        userMapper.insert(user);
    }

    /** 修改(不改密码) */
    public void update(UserSaveRequest req) {
        if (req.getId() == null) {
            throw new ServiceException("用户id不能为空");
        }
        SysUser user = userMapper.selectById(req.getId());
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        user.setNickname(req.getNickname());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        if (req.getStatus() != null) {
            user.setStatus(req.getStatus());
        }
        userMapper.updateById(user);
    }

    /** 删除(逻辑删除) */
    public void remove(Long id) {
        if (id == 1L) {
            throw new ServiceException("超级管理员不可删除");
        }
        userMapper.deleteById(id);
    }
}
