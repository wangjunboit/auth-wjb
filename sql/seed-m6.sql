-- M6 种子:给 admin(id=1)补手机号与邮箱,便于验证码登录联调
UPDATE sys_user SET phone = '13800000000', email = 'admin@test.com'
WHERE id = 1 AND (phone IS NULL OR phone = '');
