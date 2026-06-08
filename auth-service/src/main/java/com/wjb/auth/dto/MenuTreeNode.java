package com.wjb.auth.dto;

import com.wjb.auth.entity.SysMenu;

import java.util.ArrayList;
import java.util.List;

/** 菜单树节点(在 SysMenu 基础上挂 children) */
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
        return n;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public String getMenuType() { return menuType; }
    public void setMenuType(String menuType) { this.menuType = menuType; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public String getPerm() { return perm; }
    public void setPerm(String perm) { this.perm = perm; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Integer getVisible() { return visible; }
    public void setVisible(Integer visible) { this.visible = visible; }
    public List<MenuTreeNode> getChildren() { return children; }
    public void setChildren(List<MenuTreeNode> children) { this.children = children; }
}
