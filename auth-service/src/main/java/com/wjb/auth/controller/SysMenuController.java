package com.wjb.auth.controller;

import com.wjb.auth.common.Result;
import com.wjb.auth.dto.MenuSaveRequest;
import com.wjb.auth.dto.MenuTreeNode;
import com.wjb.auth.entity.SysMenu;
import com.wjb.auth.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    private final SysMenuService menuService;

    public SysMenuController(SysMenuService menuService) {
        this.menuService = menuService;
    }

    @Operation(summary = "菜单树")
    @GetMapping("/list")
    public Result<List<MenuTreeNode>> list() {
        return Result.success(menuService.tree());
    }

    @Operation(summary = "菜单详情")
    @GetMapping("/{id}")
    public Result<SysMenu> getById(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody MenuSaveRequest req) {
        menuService.add(req);
        return Result.success();
    }

    @Operation(summary = "修改菜单")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody MenuSaveRequest req) {
        menuService.update(req);
        return Result.success();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        menuService.remove(id);
        return Result.success();
    }
}
