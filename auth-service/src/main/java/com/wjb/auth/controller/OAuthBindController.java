package com.wjb.auth.controller;

import com.wjb.auth.common.Result;
import com.wjb.auth.common.UserContext;
import com.wjb.auth.dto.OAuthBindingVO;
import com.wjb.auth.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "账号绑定")
@RestController
@RequestMapping("/system/oauth")
@RequiredArgsConstructor
public class OAuthBindController {

    private final OAuthService oauthService;

    @Operation(summary = "获取绑定授权地址")
    @GetMapping("/{provider}/bind-url")
    public Result<String> bindUrl(@PathVariable String provider) {
        return Result.success(oauthService.bindUrl(provider, UserContext.getUserId()));
    }

    @Operation(summary = "当前用户绑定列表")
    @GetMapping("/bindings")
    public Result<List<OAuthBindingVO>> bindings() {
        return Result.success(oauthService.listBindings(UserContext.getUserId()));
    }

    @Operation(summary = "解绑")
    @DeleteMapping("/{provider}")
    public Result<Void> unbind(@PathVariable String provider) {
        oauthService.unbind(UserContext.getUserId(), provider);
        return Result.success();
    }
}
