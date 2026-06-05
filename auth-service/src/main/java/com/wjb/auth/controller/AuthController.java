package com.wjb.auth.controller;

import com.wjb.auth.common.Result;
import com.wjb.auth.dto.LoginRequest;
import com.wjb.auth.dto.LoginResponse;
import com.wjb.auth.dto.UserInfoResponse;
import com.wjb.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "账号密码登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(authService.login(req));
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
}
