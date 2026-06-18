package com.wjb.auth.controller;

import com.wjb.auth.common.Result;
import com.wjb.auth.dto.CaptchaResponse;
import com.wjb.auth.dto.ChangePasswordRequest;
import com.wjb.auth.dto.EmailLoginRequest;
import com.wjb.auth.dto.ProfileUpdateRequest;
import com.wjb.auth.dto.ProfileVO;
import com.wjb.auth.dto.LoginRequest;
import com.wjb.auth.dto.LoginResponse;
import com.wjb.auth.dto.SendEmailCodeRequest;
import com.wjb.auth.dto.SendSmsCodeRequest;
import com.wjb.auth.dto.SmsLoginRequest;
import com.wjb.auth.dto.UserInfoResponse;
import com.wjb.auth.service.AuthService;
import com.wjb.auth.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    /** dev 环境回传验证码 code 便于联调;生产置 false */
    @Value("${auth.dev-return-code:false}")
    private boolean devReturnCode;

    @Operation(summary = "获取图形验证码")
    @GetMapping("/captcha")
    public Result<CaptchaResponse> captcha() {
        return Result.success(captchaService.generate());
    }

    @Operation(summary = "发送手机验证码")
    @PostMapping("/sms-code")
    public Result<String> smsCode(@Valid @RequestBody SendSmsCodeRequest req) {
        String code = authService.sendSmsCode(req.getPhone(), req.getCaptchaKey(), req.getCaptchaCode());
        return Result.success(devReturnCode ? code : null);
    }

    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/email-code")
    public Result<String> emailCode(@Valid @RequestBody SendEmailCodeRequest req) {
        String code = authService.sendEmailCode(req.getEmail(), req.getCaptchaKey(), req.getCaptchaCode());
        return Result.success(devReturnCode ? code : null);
    }

    @Operation(summary = "账号密码登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(authService.login(req));
    }

    @Operation(summary = "手机验证码登录")
    @PostMapping("/login/sms")
    public Result<LoginResponse> loginSms(@Valid @RequestBody SmsLoginRequest req) {
        return Result.success(authService.loginBySms(req.getPhone(), req.getCode()));
    }

    @Operation(summary = "邮箱验证码登录")
    @PostMapping("/login/email")
    public Result<LoginResponse> loginEmail(@Valid @RequestBody EmailLoginRequest req) {
        return Result.success(authService.loginByEmail(req.getEmail(), req.getCode()));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/userinfo")
    public Result<UserInfoResponse> userinfo() {
        return Result.success(authService.currentUserInfo());
    }

    @Operation(summary = "当前用户可见菜单树")
    @GetMapping("/menus")
    public Result<java.util.List<com.wjb.auth.dto.MenuTreeNode>> menus() {
        return Result.success(authService.currentUserMenus());
    }

    @Operation(summary = "获取当前用户资料")
    @GetMapping("/profile")
    public Result<ProfileVO> profile() {
        return Result.success(authService.getProfile());
    }

    @Operation(summary = "修改当前用户资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody ProfileUpdateRequest req) {
        authService.updateProfile(req);
        return Result.success();
    }

    @Operation(summary = "修改当前用户密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(req);
        return Result.success();
    }
}
