package com.wjb.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wjb.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /** 查某用户拥有的所有权限码(去重,排除空 perm) */
    @Select("""
            SELECT DISTINCT m.perm
            FROM sys_user_role ur
            JOIN sys_role_menu rm ON rm.role_id = ur.role_id AND rm.deleted = 0
            JOIN sys_menu m ON m.id = rm.menu_id AND m.deleted = 0
            WHERE ur.user_id = #{userId}
              AND ur.deleted = 0
              AND m.perm IS NOT NULL AND m.perm <> ''
            """)
    List<String> selectPermsByUserId(Long userId);

    /** 查某用户的角色标识 role_key */
    @Select("""
            SELECT r.role_key
            FROM sys_user_role ur
            JOIN sys_role r ON r.id = ur.role_id AND r.deleted = 0
            WHERE ur.user_id = #{userId} AND ur.deleted = 0
            """)
    List<String> selectRoleKeysByUserId(Long userId);
}
