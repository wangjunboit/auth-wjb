package com.wjb.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wjb.auth.common.exception.ServiceException;
import com.wjb.auth.dto.MenuSaveRequest;
import com.wjb.auth.dto.MenuTreeNode;
import com.wjb.auth.entity.SysMenu;
import com.wjb.auth.mapper.SysMenuMapper;
import com.wjb.auth.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final com.wjb.auth.rbac.ApiPermPublisher apiPermPublisher;

    /** 全部菜单构造成树(按 sort 升序) */
    public List<MenuTreeNode> tree() {
        List<SysMenu> all = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort));
        List<MenuTreeNode> nodes = all.stream().map(MenuTreeNode::from).collect(Collectors.toList());
        Map<Long, MenuTreeNode> byId = nodes.stream()
                .collect(Collectors.toMap(MenuTreeNode::getId, n -> n));
        List<MenuTreeNode> roots = new ArrayList<>();
        for (MenuTreeNode n : nodes) {
            if (n.getParentId() == null || n.getParentId() == 0L) {
                roots.add(n);
            } else {
                MenuTreeNode parent = byId.get(n.getParentId());
                if (parent != null) {
                    parent.getChildren().add(n);
                } else {
                    roots.add(n); // 父节点缺失则当根
                }
            }
        }
        return roots;
    }

    public SysMenu getById(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new ServiceException("菜单不存在");
        }
        return menu;
    }

    public void add(MenuSaveRequest req) {
        SysMenu menu = new SysMenu();
        applyTo(menu, req);
        menuMapper.insert(menu);
        apiPermPublisher.refresh();
    }

    public void update(MenuSaveRequest req) {
        if (req.getId() == null) {
            throw new ServiceException("菜单id不能为空");
        }
        SysMenu menu = menuMapper.selectById(req.getId());
        if (menu == null) {
            throw new ServiceException("菜单不存在");
        }
        applyTo(menu, req);
        menuMapper.updateById(menu);
        apiPermPublisher.refresh();
    }

    public void remove(Long id) {
        long childCount = menuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new ServiceException("存在子菜单,不可删除");
        }
        long boundRoles = roleMenuMapper.countByMenuId(id);
        if (boundRoles > 0) {
            throw new ServiceException("该菜单已被角色引用,不可删除");
        }
        menuMapper.deleteById(id);
        apiPermPublisher.refresh();
    }

    private void applyTo(SysMenu menu, MenuSaveRequest req) {
        menu.setParentId(req.getParentId() == null ? 0L : req.getParentId());
        menu.setMenuName(req.getMenuName());
        menu.setMenuType(req.getMenuType());
        menu.setPath(req.getPath());
        menu.setComponent(req.getComponent());
        menu.setPerm(req.getPerm());
        menu.setIcon(req.getIcon());
        menu.setSort(req.getSort() == null ? 0 : req.getSort());
        menu.setVisible(req.getVisible() == null ? 1 : req.getVisible());
        menu.setApiUrl(req.getApiUrl());
        menu.setApiMethod(req.getApiMethod());
    }
}
