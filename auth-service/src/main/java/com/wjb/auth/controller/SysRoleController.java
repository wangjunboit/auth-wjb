package com.wjb.auth.controller;

import com.wjb.auth.common.Result;
import com.wjb.auth.dto.AssignMenusRequest;
import com.wjb.auth.dto.PageResult;
import com.wjb.auth.dto.RoleSaveRequest;
import com.wjb.auth.entity.SysRole;
import com.wjb.auth.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @Operation(summary = "角色分页列表")
    @GetMapping("/list")
    public Result<PageResult<SysRole>> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String roleName) {
        return Result.success(roleService.page(pageNo, pageSize, roleName));
    }

    @Operation(summary = "角色详情")
    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @Operation(summary = "角色已绑定菜单id")
    @GetMapping("/{id}/menus")
    public Result<List<Long>> menuIds(@PathVariable Long id) {
        return Result.success(roleService.getMenuIds(id));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody RoleSaveRequest req) {
        roleService.add(req);
        return Result.success();
    }

    @Operation(summary = "修改角色")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody RoleSaveRequest req) {
        roleService.update(req);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        roleService.remove(id);
        return Result.success();
    }

    @Operation(summary = "给角色分配菜单")
    @PostMapping("/assign-menus")
    public Result<Void> assignMenus(@Valid @RequestBody AssignMenusRequest req) {
        roleService.assignMenus(req.getRoleId(), req.getMenuIds());
        return Result.success();
    }
}
