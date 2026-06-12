package com.wjb.auth.controller;

import com.wjb.auth.common.Result;
import com.wjb.auth.dto.OAuthCallbackResponse;
import com.wjb.auth.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "第三方登录")
@RestController
@RequestMapping("/auth/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oauthService;

    @Operation(summary = "获取第三方登录授权地址")
    @GetMapping("/{provider}/url")
    public Result<String> url(@PathVariable String provider) {
        return Result.success(oauthService.loginUrl(provider));
    }

    @Operation(summary = "第三方回调(登录或绑定)")
    @PostMapping("/{provider}/callback")
    public Result<OAuthCallbackResponse> callback(@PathVariable String provider,
                                                  @RequestBody Map<String, String> body) {
        return Result.success(oauthService.callback(provider, body.get("code"), body.get("state")));
    }
}
