-- M3 种子数据:角色管理、菜单管理 的菜单与权限码,并授予 admin 角色(role id=1)

-- 角色管理菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perm, sort, visible, deleted)
SELECT 102, 100, '角色管理', 'C', 'role', 'system/role/index', 'system:role:list', 2, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 102);
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, sort, visible, deleted)
SELECT 1021, 102, '角色查询', 'F', 'system:role:query', 1, 1, 0 WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1021);
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, sort, visible, deleted)
SELECT 1022, 102, '角色新增', 'F', 'system:role:add', 2, 1, 0 WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1022);
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, sort, visible, deleted)
SELECT 1023, 102, '角色修改', 'F', 'system:role:edit', 3, 1, 0 WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1023);
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, sort, visible, deleted)
SELECT 1024, 102, '角色删除', 'F', 'system:role:remove', 4, 1, 0 WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1024);
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, sort, visible, deleted)
SELECT 1025, 102, '分配菜单', 'F', 'system:role:assign', 5, 1, 0 WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1025);

-- 菜单管理菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perm, sort, visible, deleted)
SELECT 103, 100, '菜单管理', 'C', 'menu', 'system/menu/index', 'system:menu:list', 3, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 103);
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, sort, visible, deleted)
SELECT 1031, 103, '菜单查询', 'F', 'system:menu:query', 1, 1, 0 WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1031);
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, sort, visible, deleted)
SELECT 1032, 103, '菜单新增', 'F', 'system:menu:add', 2, 1, 0 WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1032);
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, sort, visible, deleted)
SELECT 1033, 103, '菜单修改', 'F', 'system:menu:edit', 3, 1, 0 WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1033);
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, sort, visible, deleted)
SELECT 1034, 103, '菜单删除', 'F', 'system:menu:remove', 4, 1, 0 WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1034);

-- 授予 admin 角色(id=1)
INSERT INTO sys_role_menu (role_id, menu_id, deleted)
SELECT 1, m.id, 0 FROM sys_menu m
WHERE m.id IN (102,1021,1022,1023,1024,1025, 103,1031,1032,1033,1034)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id);
