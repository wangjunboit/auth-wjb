package com.wjb.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wjb.auth.common.UserContext;
import com.wjb.auth.common.exception.ServiceException;
import com.wjb.auth.dto.LoginRequest;
import com.wjb.auth.dto.LoginResponse;
import com.wjb.auth.dto.UserInfoResponse;
import com.wjb.auth.entity.SysUser;
import com.wjb.auth.mapper.SysMenuMapper;
import com.wjb.auth.mapper.SysUserMapper;
import com.wjb.auth.sender.MailSender;
import com.wjb.auth.sender.SmsSender;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class AuthService {

    public static final String SESSION_PERMS = "perms";
    private static final int MAX_FAIL = 5;
    private static final Duration LOCK_TTL = Duration.ofMinutes(10);

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SysMenuMapper menuMapper;
    private final StringRedisTemplate redis;
    private final CaptchaService captchaService;
    private final VerifyCodeService verifyCodeService;
    private final SmsSender smsSender;
    private final MailSender mailSender;

    public AuthService(SysUserMapper userMapper, PasswordEncoder passwordEncoder, SysMenuMapper menuMapper,
                       StringRedisTemplate redis, CaptchaService captchaService, VerifyCodeService verifyCodeService,
                       SmsSender smsSender, MailSender mailSender) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.menuMapper = menuMapper;
        this.redis = redis;
        this.captchaService = captchaService;
        this.verifyCodeService = verifyCodeService;
        this.smsSender = smsSender;
        this.mailSender = mailSender;
    }

    /** 账号密码登录:图形码 + 防爆破 */
    public LoginResponse login(LoginRequest req) {
        String lockKey = "login:lock:" + req.getUsername();
        String failKey = "login:fail:" + req.getUsername();
        if (Boolean.TRUE.equals(redis.hasKey(lockKey))) {
            throw new ServiceException("账号已锁定,请 10 分钟后再试");
        }
        captchaService.verify(req.getCaptchaKey(), req.getCaptchaCode());

        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername()));
        if (user == null || user.getPassword() == null
                || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            Long cnt = redis.opsForValue().increment(failKey);
            if (cnt != null && cnt == 1L) {
                redis.expire(failKey, LOCK_TTL);
            }
            if (cnt != null && cnt >= MAX_FAIL) {
                redis.opsForValue().set(lockKey, "1", LOCK_TTL);
            }
            throw new ServiceException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new ServiceException("账号已被禁用");
        }
        redis.delete(failKey);
        redis.delete(lockKey);
        return doLogin(user);
    }

    /** 发送手机验证码 */
    public String sendSmsCode(String phone, String captchaKey, String captchaCode) {
        captchaService.verify(captchaKey, captchaCode);
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
        if (user == null) {
            throw new ServiceException("该手机号未注册");
        }
        String code = verifyCodeService.generateAndStore("sms:" + phone, "sms:limit:" + phone);
        smsSender.send(phone, code);
        return code;
    }

    /** 发送邮箱验证码 */
    public String sendEmailCode(String email, String captchaKey, String captchaCode) {
        captchaService.verify(captchaKey, captchaCode);
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email));
        if (user == null) {
            throw new ServiceException("该邮箱未注册");
        }
        String code = verifyCodeService.generateAndStore("email:" + email, "email:limit:" + email);
        mailSender.send(email, code);
        return code;
    }

    /** 手机验证码登录(账号必须已存在) */
    public LoginResponse loginBySms(String phone, String code) {
        verifyCodeService.verifyAndConsume("sms:" + phone, code);
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
        if (user == null) {
            throw new ServiceException("账号不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new ServiceException("账号已被禁用");
        }
        return doLogin(user);
    }

    /** 邮箱验证码登录(账号必须已存在) */
    public LoginResponse loginByEmail(String email, String code) {
        verifyCodeService.verifyAndConsume("email:" + email, code);
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email));
        if (user == null) {
            throw new ServiceException("账号不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new ServiceException("账号已被禁用");
        }
        return doLogin(user);
    }

    /** 登录收口:登录 + 权限快照入会话 + 返回 token */
    private LoginResponse doLogin(SysUser user) {
        StpUtil.login(user.getId());
        List<String> perms = userMapper.selectPermsByUserId(user.getId());
        StpUtil.getSession().set(SESSION_PERMS, perms);
        return new LoginResponse(StpUtil.getTokenValue(), user.getId());
    }

    /** 按用户 id 登录(供 OAuth 等已确认身份的场景收口复用) */
    public LoginResponse loginByUserId(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new ServiceException("账号已被禁用");
        }
        return doLogin(user);
    }

    public void logout() {
        StpUtil.logout(UserContext.getUserId());
    }

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
