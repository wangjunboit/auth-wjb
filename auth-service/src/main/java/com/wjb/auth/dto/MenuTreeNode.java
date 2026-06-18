package com.wjb.auth.dto;

import com.wjb.auth.entity.SysMenu;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 菜单树节点(在 SysMenu 基础上挂 children) */
@Data
public class MenuTreeNode {
    private Long id;
    private Long parentId;
    private String menuName;
    private String menuType;
    private String path;
    private String component;
    private String perm;
    private String icon;
    private Integer sort;
    private Integer visible;
    private String apiUrl;
    private String apiMethod;
    private List<MenuTreeNode> children = new ArrayList<>();

    public static MenuTreeNode from(SysMenu m) {
        MenuTreeNode n = new MenuTreeNode();
        n.id = m.getId();
        n.parentId = m.getParentId();
        n.menuName = m.getMenuName();
        n.menuType = m.getMenuType();
        n.path = m.getPath();
        n.component = m.getComponent();
        n.perm = m.getPerm();
        n.icon = m.getIcon();
        n.sort = m.getSort();
        n.visible = m.getVisible();
        n.apiUrl = m.getApiUrl();
        n.apiMethod = m.getApiMethod();
        return n;
    }
}
