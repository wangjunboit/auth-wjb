-- 给已存在的线上表补字段注释(MySQL 8)。可重复执行。

-- sys_user
ALTER TABLE sys_user
  MODIFY COLUMN id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  MODIFY COLUMN username    VARCHAR(50)  NOT NULL COMMENT '登录账号',
  MODIFY COLUMN password    VARCHAR(100) NULL COMMENT 'BCrypt 密码',
  MODIFY COLUMN nickname    VARCHAR(50)  NULL COMMENT '昵称',
  MODIFY COLUMN phone       VARCHAR(20)  NULL COMMENT '手机号',
  MODIFY COLUMN email       VARCHAR(100) NULL COMMENT '邮箱',
  MODIFY COLUMN avatar      VARCHAR(255) NULL COMMENT '头像',
  MODIFY COLUMN status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0禁用 1正常',
  MODIFY COLUMN dept_id     BIGINT       NULL COMMENT '部门id(预留)',
  MODIFY COLUMN create_time DATETIME     NULL COMMENT '创建时间',
  MODIFY COLUMN update_time DATETIME     NULL COMMENT '更新时间',
  MODIFY COLUMN create_by   BIGINT       NULL COMMENT '创建人id',
  MODIFY COLUMN update_by   BIGINT       NULL COMMENT '更新人id',
  MODIFY COLUMN deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 1已删';

-- sys_role
ALTER TABLE sys_role
  MODIFY COLUMN id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  MODIFY COLUMN role_name   VARCHAR(50)  NOT NULL COMMENT '角色名',
  MODIFY COLUMN role_key    VARCHAR(50)  NOT NULL COMMENT '角色标识',
  MODIFY COLUMN status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0禁用 1正常',
  MODIFY COLUMN remark      VARCHAR(255) NULL COMMENT '备注',
  MODIFY COLUMN create_time DATETIME     NULL COMMENT '创建时间',
  MODIFY COLUMN update_time DATETIME     NULL COMMENT '更新时间',
  MODIFY COLUMN create_by   BIGINT       NULL COMMENT '创建人id',
  MODIFY COLUMN update_by   BIGINT       NULL COMMENT '更新人id',
  MODIFY COLUMN deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 1已删';

-- sys_menu
ALTER TABLE sys_menu
  MODIFY COLUMN id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  MODIFY COLUMN parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父菜单id,0为顶级',
  MODIFY COLUMN menu_name   VARCHAR(50)  NOT NULL COMMENT '菜单名',
  MODIFY COLUMN menu_type   CHAR(1)      NOT NULL COMMENT '类型:M目录 C菜单 F按钮',
  MODIFY COLUMN path        VARCHAR(200) NULL COMMENT '前端路由',
  MODIFY COLUMN component   VARCHAR(255) NULL COMMENT '前端组件路径',
  MODIFY COLUMN perm        VARCHAR(100) NULL COMMENT '权限码',
  MODIFY COLUMN api_url     VARCHAR(200) NULL COMMENT '后端接口URL模式(Ant风格)',
  MODIFY COLUMN api_method  VARCHAR(10)  NULL COMMENT 'HTTP方法,空或*为任意',
  MODIFY COLUMN icon        VARCHAR(100) NULL COMMENT '图标',
  MODIFY COLUMN sort        INT          NOT NULL DEFAULT 0 COMMENT '排序号',
  MODIFY COLUMN visible     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否显示:0隐藏 1显示',
  MODIFY COLUMN create_time DATETIME     NULL COMMENT '创建时间',
  MODIFY COLUMN update_time DATETIME     NULL COMMENT '更新时间',
  MODIFY COLUMN create_by   BIGINT       NULL COMMENT '创建人id',
  MODIFY COLUMN update_by   BIGINT       NULL COMMENT '更新人id',
  MODIFY COLUMN deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 1已删';

-- sys_user_role
ALTER TABLE sys_user_role
  MODIFY COLUMN id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  MODIFY COLUMN user_id     BIGINT   NOT NULL COMMENT '用户id',
  MODIFY COLUMN role_id     BIGINT   NOT NULL COMMENT '角色id',
  MODIFY COLUMN create_time DATETIME NULL COMMENT '创建时间',
  MODIFY COLUMN update_time DATETIME NULL COMMENT '更新时间',
  MODIFY COLUMN create_by   BIGINT   NULL COMMENT '创建人id',
  MODIFY COLUMN update_by   BIGINT   NULL COMMENT '更新人id',
  MODIFY COLUMN deleted     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 1已删';

-- sys_role_menu
ALTER TABLE sys_role_menu
  MODIFY COLUMN id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  MODIFY COLUMN role_id     BIGINT   NOT NULL COMMENT '角色id',
  MODIFY COLUMN menu_id     BIGINT   NOT NULL COMMENT '菜单id',
  MODIFY COLUMN create_time DATETIME NULL COMMENT '创建时间',
  MODIFY COLUMN update_time DATETIME NULL COMMENT '更新时间',
  MODIFY COLUMN create_by   BIGINT   NULL COMMENT '创建人id',
  MODIFY COLUMN update_by   BIGINT   NULL COMMENT '更新人id',
  MODIFY COLUMN deleted     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 1已删';

-- sys_oauth_binding
ALTER TABLE sys_oauth_binding
  MODIFY COLUMN id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  MODIFY COLUMN user_id     BIGINT       NOT NULL COMMENT '关联本系统用户id',
  MODIFY COLUMN provider    VARCHAR(20)  NOT NULL COMMENT '平台:wechat/github',
  MODIFY COLUMN open_id     VARCHAR(100) NOT NULL COMMENT '第三方唯一标识',
  MODIFY COLUMN union_id    VARCHAR(100) NULL COMMENT '微信unionid(可空)',
  MODIFY COLUMN create_time DATETIME     NULL COMMENT '创建时间',
  MODIFY COLUMN update_time DATETIME     NULL COMMENT '更新时间',
  MODIFY COLUMN create_by   BIGINT       NULL COMMENT '创建人id',
  MODIFY COLUMN update_by   BIGINT       NULL COMMENT '更新人id',
  MODIFY COLUMN deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 1已删';
