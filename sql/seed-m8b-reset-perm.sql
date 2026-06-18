-- M8:把"重置密码"接口纳入动态RBAC 映射(否则网关视为仅需登录=越权)。复用 system:user:edit。
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perm, api_url, api_method, sort, visible, deleted)
SELECT 1017, 101, '用户重置密码(隐藏)', 'F', 'system:user:edit', '/system/user/*/reset-password', 'PUT', 92, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1017);
