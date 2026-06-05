package com.wjb.auth.controller;

import com.wjb.auth.common.Result;
import com.wjb.auth.dto.PageResult;
import com.wjb.auth.dto.UserSaveRequest;
import com.wjb.auth.entity.SysUser;
import com.wjb.auth.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    private final SysUserService userService;

    public SysUserController(SysUserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户分页列表")
    @GetMapping("/list")
    public Result<PageResult<SysUser>> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String username) {
        return Result.success(userService.page(pageNo, pageSize, username));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody UserSaveRequest req) {
        userService.add(req);
        return Result.success();
    }

    @Operation(summary = "修改用户")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody UserSaveRequest req) {
        userService.update(req);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        userService.remove(id);
        return Result.success();
    }
}
