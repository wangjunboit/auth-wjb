-- M2 种子数据:用户管理菜单与权限码,并授予 admin 角色(role id=1)

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, perm, sort, visible, deleted)
SELECT 100, 0, '系统管理', 'M', '/system', NULL, 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 100);

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perm, sort, visible, deleted)
SELECT 101, 100, '用户管理', 'C', 'user', 'system/user/index', 'system:user:list', 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 101);

INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, sort, visible, deleted)
SELECT 1011, 101, '用户查询', 'F', 'system:user:query', 1, 1, 0 WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1011);
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, sort, visible, deleted)
SELECT 1012, 101, '用户新增', 'F', 'system:user:add', 2, 1, 0 WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1012);
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, sort, visible, deleted)
SELECT 1013, 101, '用户修改', 'F', 'system:user:edit', 3, 1, 0 WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1013);
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, sort, visible, deleted)
SELECT 1014, 101, '用户删除', 'F', 'system:user:remove', 4, 1, 0 WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1014);

INSERT INTO sys_role_menu (role_id, menu_id, deleted)
SELECT 1, m.id, 0 FROM sys_menu m
WHERE m.id IN (100, 101, 1011, 1012, 1013, 1014)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id);
