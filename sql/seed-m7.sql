-- M7 种子:账号绑定菜单,授予 admin 角色(role id=1)
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perm, sort, visible, deleted)
SELECT 104, 100, '账号绑定', 'C', 'binding', 'system/binding', NULL, 4, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 104);

INSERT INTO sys_role_menu (role_id, menu_id, deleted)
SELECT 1, 104, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = 104);
