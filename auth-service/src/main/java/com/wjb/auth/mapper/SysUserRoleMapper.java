package com.wjb.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wjb.auth.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId} AND deleted = 0")
    List<Long> selectRoleIdsByUserId(Long userId);

    @Select("SELECT COUNT(1) FROM sys_user_role WHERE role_id = #{roleId} AND deleted = 0")
    long countByRoleId(Long roleId);
}
