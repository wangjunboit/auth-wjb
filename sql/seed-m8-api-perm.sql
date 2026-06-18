-- M8 动态RBAC:sys_menu 加 api_url/api_method,把现有 19 条网关规则迁为数据
-- 说明:list 权限码在 C 型菜单(101/102/103)上,其余在 F 按钮上;按 perm 更新即可

-- 1) 加字段(只跑一次)
ALTER TABLE sys_menu ADD COLUMN api_url    VARCHAR(200) NULL COMMENT '后端接口URL模式(Ant风格),仅承载鉴权映射的菜单使用' AFTER perm;
ALTER TABLE sys_menu ADD COLUMN api_method VARCHAR(10)  NULL COMMENT 'HTTP方法GET/POST/PUT/DELETE,空或*为任意' AFTER api_url;

-- 2) 标准 16 条(按 perm 更新现有菜单行)
UPDATE sys_menu SET api_method='GET',    api_url='/system/user/list'           WHERE perm='system:user:list';
UPDATE sys_menu SET api_method='POST',   api_url='/system/user'                WHERE perm='system:user:add';
UPDATE sys_menu SET api_method='PUT',    api_url='/system/user'                WHERE perm='system:user:edit';
UPDATE sys_menu SET api_method='DELETE', api_url='/system/user/**'             WHERE perm='system:user:remove';
UPDATE sys_menu SET api_method='GET',    api_url='/system/user/*'              WHERE perm='system:user:query';
UPDATE sys_menu SET api_method='GET',    api_url='/system/role/list'           WHERE perm='system:role:list';
UPDATE sys_menu SET api_method='POST',   api_url='/system/role'                WHERE perm='system:role:add';
UPDATE sys_menu SET api_method='PUT',    api_url='/system/role'                WHERE perm='system:role:edit';
UPDATE sys_menu SET api_method='DELETE', api_url='/system/role/**'             WHERE perm='system:role:remove';
UPDATE sys_menu SET api_method='GET',    api_url='/system/role/*'              WHERE perm='system:role:query';
UPDATE sys_menu SET api_method='POST',   api_url='/system/role/assign-menus'   WHERE perm='system:role:assign';
UPDATE sys_menu SET api_method='GET',    api_url='/system/menu/list'           WHERE perm='system:menu:list';
UPDATE sys_menu SET api_method='POST',   api_url='/system/menu'                WHERE perm='system:menu:add';
UPDATE sys_menu SET api_method='PUT',    api_url='/system/menu'                WHERE perm='system:menu:edit';
UPDATE sys_menu SET api_method='DELETE', api_url='/system/menu/**'             WHERE perm='system:menu:remove';
UPDATE sys_menu SET api_method='GET',    api_url='/system/menu/*'              WHERE perm='system:menu:query';

-- 3) 方式甲:3 个子资源接口,各加一行隐藏菜单(visible=0)承载映射,perm 沿用现有
--    这些行不授予任何角色(它们的 perm 已通过 1013/1011/1021 授权),仅供网关查映射
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, api_url, api_method, sort, visible, deleted)
SELECT 1015, 101, '用户分配角色(隐藏)', 'F', 'system:user:edit',  '/system/user/assign-roles', 'POST', 90, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1015);
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, api_url, api_method, sort, visible, deleted)
SELECT 1016, 101, '用户角色查询(隐藏)', 'F', 'system:user:query', '/system/user/*/roles',      'GET',  91, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1016);
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, api_url, api_method, sort, visible, deleted)
SELECT 1026, 102, '角色菜单查询(隐藏)', 'F', 'system:role:query', '/system/role/*/menus',      'GET',  90, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1026);
