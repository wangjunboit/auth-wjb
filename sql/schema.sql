-- auth-wjb RBAC 建表脚本(MySQL 8, utf8mb4)
-- 执行前:CREATE DATABASE auth_wjb DEFAULT CHARSET utf8mb4;

-- 公共字段约定:create_time / update_time / create_by / update_by / deleted

-- 1. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    password    VARCHAR(100) NULL COMMENT 'BCrypt 密码',
    nickname    VARCHAR(50)  NULL COMMENT '昵称',
    phone       VARCHAR(20)  NULL COMMENT '手机号',
    email       VARCHAR(100) NULL COMMENT '邮箱',
    avatar      VARCHAR(255) NULL COMMENT '头像',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0禁用 1正常',
    dept_id     BIGINT       NULL COMMENT '部门id(预留)',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_time DATETIME     NULL COMMENT '更新时间',
    create_by   BIGINT       NULL COMMENT '创建人id',
    update_by   BIGINT       NULL COMMENT '更新人id',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_name   VARCHAR(50)  NOT NULL COMMENT '角色名',
    role_key    VARCHAR(50)  NOT NULL COMMENT '角色标识',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0禁用 1正常',
    remark      VARCHAR(255) NULL COMMENT '备注',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_time DATETIME     NULL COMMENT '更新时间',
    create_by   BIGINT       NULL COMMENT '创建人id',
    update_by   BIGINT       NULL COMMENT '更新人id',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 菜单/权限表
CREATE TABLE IF NOT EXISTS sys_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父菜单id,0为顶级',
    menu_name   VARCHAR(50)  NOT NULL COMMENT '菜单名',
    menu_type   CHAR(1)      NOT NULL COMMENT '类型:M目录 C菜单 F按钮',
    path        VARCHAR(200) NULL COMMENT '前端路由',
    component   VARCHAR(255) NULL COMMENT '前端组件路径',
    perm        VARCHAR(100) NULL COMMENT '权限码',
    icon        VARCHAR(100) NULL COMMENT '图标',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    visible     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否显示:0隐藏 1显示',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_time DATETIME     NULL COMMENT '更新时间',
    create_by   BIGINT       NULL COMMENT '创建人id',
    update_by   BIGINT       NULL COMMENT '更新人id',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 1已删',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 4. 用户-角色关联
CREATE TABLE IF NOT EXISTS sys_user_role (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT   NOT NULL COMMENT '用户id',
    role_id     BIGINT   NOT NULL COMMENT '角色id',
    create_time DATETIME NULL COMMENT '创建时间',
    update_time DATETIME NULL COMMENT '更新时间',
    create_by   BIGINT   NULL COMMENT '创建人id',
    update_by   BIGINT   NULL COMMENT '更新人id',
    deleted     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- 5. 角色-菜单关联
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_id     BIGINT   NOT NULL COMMENT '角色id',
    menu_id     BIGINT   NOT NULL COMMENT '菜单id',
    create_time DATETIME NULL COMMENT '创建时间',
    update_time DATETIME NULL COMMENT '更新时间',
    create_by   BIGINT   NULL COMMENT '创建人id',
    update_by   BIGINT   NULL COMMENT '更新人id',
    deleted     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联';

-- 6. 第三方账号绑定
CREATE TABLE IF NOT EXISTS sys_oauth_binding (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '关联本系统用户id',
    provider    VARCHAR(20)  NOT NULL COMMENT '平台:wechat/github',
    open_id     VARCHAR(100) NOT NULL COMMENT '第三方唯一标识',
    union_id    VARCHAR(100) NULL COMMENT '微信unionid(可空)',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_time DATETIME     NULL COMMENT '更新时间',
    create_by   BIGINT       NULL COMMENT '创建人id',
    update_by   BIGINT       NULL COMMENT '更新人id',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_provider_openid (provider, open_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='第三方账号绑定';

-- 初始数据:超级管理员账号骨架(密码在 P2 登录功能实现时由代码写入 BCrypt)
INSERT INTO sys_user (id, username, nickname, status, deleted)
SELECT 1, 'admin', '超级管理员', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE id = 1);

INSERT INTO sys_role (id, role_name, role_key, status, deleted)
SELECT 1, '超级管理员', 'admin', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE id = 1);

INSERT INTO sys_user_role (user_id, role_id, deleted)
SELECT 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_user_role WHERE user_id = 1 AND role_id = 1);
